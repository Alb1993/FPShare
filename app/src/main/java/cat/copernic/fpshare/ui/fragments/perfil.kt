package cat.copernic.fpshare.ui.fragments

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import kotlinx.coroutines.*
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import cat.copernic.fpshare.R
import cat.copernic.fpshare.databinding.FragmentPerfilBinding
import cat.copernic.fpshare.modelo.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Clase de la pantalla perfil
 *
 * @author FPShare
 */
class perfil : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var nombreEditText: EditText
    private lateinit var  apellidosEditText: EditText
    private lateinit var numero : EditText
    private lateinit var insituto : EditText
    private lateinit var  botonGuardarCambios : Button
    private lateinit var emailEdittext : EditText
    private var storage = FirebaseStorage.getInstance()
    private var user = Firebase.auth.currentUser
    private var storageRef = storage.reference.child("Imagenes/" /* + user?.email.toString() */)
    private lateinit var imagen: ImageView
    private lateinit var progressBar: ProgressBar
    private var REQUEST_CODE = 123

    private var photoSelectedUri: Uri? = null

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            photoSelectedUri = it.data?.data //Assignem l'URI de la imatge
            uploadImage().execute()
        }
    }


    private var bd = FirebaseFirestore.getInstance()

    /**
     * Con esta función mostraremos el diseño de la pantalla ,mediante un View
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * En esta función iniciamos  los diferentes elementos de la pantalla y creamos los listener de los eventos de los
     * elementos  de la vista
     *
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val email = user?.email.toString()
        inicizalizar()

        imagen.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main){
                async{ subirArchivos() }
            }
        }

        botonGuardarCambios.setOnClickListener {
            /***
             * Primero comprobamos si en la coleccion Usuarios existe un documento con el email como
             * id del usuario.
             */
            bd.collection("Usuarios").document(email).get().addOnSuccessListener {
                /***
                 * Si el documento existe, actualizara los datos del documento con los datos nuevos
                 * introducidos, pero antes comprobamos que los campos como el nombre y el telefono
                 * son validos.
                 */
                if (!camposVacios(nombreEditText.text.toString(), emailEdittext.text.toString())) {
                    Snackbar.make(
                        binding.fragmentPerfil,
                        getString(R.string.errorNombreEmailVacio),
                        Snackbar.LENGTH_LONG
                    ).show()
                } /*else if (comprobarTelefono(numero.text.toString())) {
                    Snackbar.make(
                        binding.fragmentPerfil,
                        getString(R.string.telefonoInvalido),
                        Snackbar.LENGTH_LONG
                    ).show()
                }*/ else if (nombreLargo(nombreEditText.text.toString())) {
                    Snackbar.make(
                        binding.fragmentPerfil,
                        getString(R.string.nombreInvalido),
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {

                    bd.collection("Usuarios").document(email).update(
                        "email",
                        emailEdittext.text.toString(),
                        "nombre",
                        nombreEditText.text.toString(),
                        "apellidos",
                        apellidosEditText.text.toString(),
                        "telefono",
                        numero.text.toString(),
                        "instituto",
                        insituto.text.toString(),
                        "imgPerfil",
                        photoSelectedUri.toString()
                    ).addOnSuccessListener {
                        Snackbar.make(
                            binding.fragmentPerfil,
                            getString(R.string.perfilUpdateCorrecto),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
                /***
                 * Si el documento no existe, generara un documento nuevo con los datos introducidos
                 * en los campos de texto.
                 */
                .addOnFailureListener {
                    val user = User(
                        emailEdittext.text.toString(),
                        nombreEditText.text.toString(),
                        apellidosEditText.text.toString(),
                        numero.text.toString(),
                        insituto.text.toString(),
                        photoSelectedUri.toString(),
                        false
                    )
                    bd.collection("Usuarios").document(email).set(user)
                }
        }
    }


    private inner class uploadImage : AsyncTask<Void, Int, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Void?): String {
            // código para subir la imagen al servidor

            var progress = 0
            while (progress < 100) {
                progress += 10
                publishProgress(progress)
                Thread.sleep(1000)
            }
            photoSelectedUri?.let { uri ->
                /***
                 * Subimos la imagen seleccionada a Firestore con el metodo putFile y le pasamos como
                 * parametro la URI de la imagen.
                 */
                storageRef.putFile(uri).addOnSuccessListener {
                    lifecycleScope.launch(Dispatchers.Main){
                        async{ cargarImagen() }
                    }
                }

            }
            return getString(R.string.uploadResult)
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            progressBar.progress = values[0]!!
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), result, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Con esta función destruimos la vista del fragemnt y limpiamos recursos para que el sistema funcione correctamente
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun inicizalizar() {
        val email = user?.email.toString()
        nombreEditText = binding.edittextName
        apellidosEditText = binding.edittextApellidos
        numero = binding.edittextNumero
        insituto = binding.edittextInstitute
        botonGuardarCambios = binding.buttonSaveChangesProfile
        emailEdittext = binding.editextEmail
        imagen = binding.imageProfile
        progressBar = binding.progressBar!!
        progressBar.visibility = View.INVISIBLE

        val appContext = context
        /***
         * Para cargar los datos en el fragment perfil, comprobamos si el usuario existe en
         * la coleccion Usuarios.
         */
        bd.collection("Usuarios").document(email).get().addOnSuccessListener {
            /***
             * Si el usuario existe, creara un objeto de clase User donde guardara toda la
             * informacion de nuestro usuario actual. De lo contrario, mostrara los datos
             * introducidos en el registro, como el nombre y el email.
             */
            val user = User(
                it.id,
                it["nombre"].toString(),
                it["apellidos"].toString(),
                it["telefono"].toString(),
                it["instituto"].toString(),
                it["imgPerfil"].toString()
            )
            /***
             * Aqui insertara los datos del usuario en los camps de texto.
             */
            nombreEditText.setText(user.nombre)
            apellidosEditText.setText(user.apellidos)
            numero.setText(user.telefono)
            insituto.setText(user.instituto)
            emailEdittext.setText(user.email)
                lifecycleScope.launch(Dispatchers.Main){
                    async{ cargarImagen() }
                }
            }
    }

    suspend fun cargarImagen(){
        /***
         * I aqui cargara la imagen del usuario cuyo nombre sera el mismo que el del email
         * del usuario, puesto que nada mas puede tener un email.
         */
        val localfile = File.createTempFile("tempImage", "jpg")
        try {
            storageRef.getFile(localfile).await()
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            binding.imageProfile.setImageBitmap(bitmap)
        }catch (e: StorageException){
            println("Error")
        }

    }

    suspend fun subirArchivos() {
        val appContext = context
        val intent = Intent(ACTION_PICK, EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    //En tu actividad
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // El intent se ha realizado correctamente
                /***
                 * Añadimos la imagen en Firestore
                 */
                uploadImage().execute()

            } else {
                // El intent no se ha realizado correctamente
            }
        }
    }

    private fun camposVacios(nombre: String, correo: String): Boolean {
        return nombre.isNotEmpty() && nombre.isNotBlank() && correo.isNotEmpty() && correo.isNotBlank()
    }
/*
    private fun comprobarTelefono(telefono: String): Boolean {
        val comprobante = "(\\+34|0034|34)?[ -]*([67])[ -]*([0-9][ -]*){8}".toRegex()

        return if (telefono.isEmpty()) {
            false
        } else {
            comprobante.containsMatchIn(telefono)
        }
    }
*/
    private fun nombreLargo(nombre: String): Boolean {
        return nombre.length >= 30
    }
}

private fun <I> ActivityResultLauncher<I>.launch(intent: String, externalContentUri: Uri?) {

}
