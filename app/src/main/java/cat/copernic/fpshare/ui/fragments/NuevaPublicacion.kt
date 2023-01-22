package cat.copernic.fpshare.ui.fragments

import android.R
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import cat.copernic.fpshare.databinding.FragmentNuevaPublicacionBinding
import cat.copernic.fpshare.modelo.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Fragment de la pantalla de nueva publicación
 *
 * @author FPShare
 */
class NuevaPublicacion : Fragment() {

    private var _binding: FragmentNuevaPublicacionBinding? = null
    private val binding get() = _binding!!
    private var bd = FirebaseFirestore.getInstance()
    private lateinit var titulo: EditText
    private lateinit var descripcion: TextInputEditText
    private lateinit var enlace: TextInputEditText
    private var user = Firebase.auth.currentUser
    private lateinit var botonPublicar: Button
    private lateinit var idModuloSpinner: Spinner
    private lateinit var idUfSpinner: Spinner
    private var ciclo: String = "0"
    private var modulo: String = "0"
    private var uf: String = "0"
    private lateinit var arrayIdModulo: ArrayList<String>
    private lateinit var arrayIdUf: ArrayList<String>
    private lateinit var btnAdd: Button
    private val READ_REQUEST_CODE = 42
    private var storage = FirebaseStorage.getInstance()
    public lateinit var path: String
    private var pdfUri: Uri? = null
    private lateinit var nombreArchivo: String
    val i: Int = 0

    private val resultat = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            nombreArchivo = System.currentTimeMillis().toString()
            var pdfRef = storage.reference.child("pdfs/${nombreArchivo}.pdf")
            pdfUri = it.data?.data //Assignem l'URI de la imatge
            pdfUri?.let{uri->
                //Toast.makeText(requireContext(), uri.toString(), Toast.LENGTH_LONG).show()
                pdfRef.putFile(uri).addOnSuccessListener {
                    Toast.makeText(requireContext(),"Doc Added", Toast.LENGTH_LONG).show()
                }.addOnFailureListener(){
                    Toast.makeText(requireContext(),"Doc no Added" , Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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
        _binding = FragmentNuevaPublicacionBinding.inflate(inflater, container, false)
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
        botonPublicar = binding.btnPublish
        titulo = binding.textPost
        descripcion = binding.textDescription
        enlace = binding.textLink
        idModuloSpinner = binding.spinnerModulesNewPost
        idUfSpinner = binding.spinnerUfsNewPost
        btnAdd = binding.btnAdd!!

        binding.tagsCicles.setOnCheckedChangeListener { group, checkedId ->
            if (binding.optionDam.isChecked) {
                cargarModulos("DAM")
                ciclo = "DAM"
            } else if (binding.optionDaw.isChecked) {
                cargarModulos("DAW")
                ciclo = "DAW"
            } else if (binding.optionSmix.isChecked) {
                cargarModulos("SMIR")
                ciclo = "SMIR"
            } else if (binding.optionAsix.isChecked) {
                cargarModulos("ASIR")
                ciclo = "ASIR"
            }
        }
        idModuloSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                // Un item ha sido seleccionado
                modulo = arrayIdModulo[position]
                // Hacer algo con el item seleccionado
                cargarUfs(modulo)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // No se ha seleccionado ningún item
            }
        }
        idUfSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                // Un item ha sido seleccionado
                uf = arrayIdUf[position]
                // Hacer algo con el item seleccionado
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No se ha seleccionado ningún item
            }
        }
        botonPublicar.setOnClickListener {
            llegirDades()
        }
        btnAdd.setOnClickListener {

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            resultat.launch(intent)
        }
    }

    /**
     * Función para cargar las UFs a través de una consulta de la base de datos
     *
     * @param idModulo
     */
    private fun cargarUfs(idModulo: String) {
        val listaUfs = ArrayList<String>()
        arrayIdUf = ArrayList()
        bd.collection("Ciclos").document(ciclo).collection("Modulos").document(idModulo)
            .collection("UFs").get().addOnSuccessListener { documents ->
                for (document in documents) {
                    listaUfs.add(document["nombre"].toString())
                    arrayIdUf.add(document.id)
                }

                val adapter = ArrayAdapter<String>(requireContext(), R.layout.simple_spinner_item)
                adapter.addAll(listaUfs)
                idUfSpinner.adapter = adapter

            }
    }

    /**
     * Con esta función destruimos la vista del fragemnt y limpiamos recursos para que el sistema funcione correctamente
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun llegirDades() {
        val publi = Publicacion()
        val usuario = User()
        val correo = user?.email.toString()
        /**
         * Leemos los datos del usuario actual.
         * NOTA: Si queremos tratar con datos de dos publicaciones, debe de ser en la misma
         * snapshot. No podemos tratar con datos que esten fuera de la snapshot, porque kotlin
         * no puede guardarlos fuera.
         */
        bd.collection("Usuarios").document(correo).get().addOnSuccessListener { doc ->
            //val usuario = doc.toObject(User::class.java)
            usuario.nombre = doc["nombre"].toString()
            usuario.email = doc["email"].toString()
            usuario.telefono = doc["telefono"].toString()
            usuario.instituto = doc["instituto"].toString()
            usuario.apellidos = doc["apellidos"].toString()
            usuario.imgPerfil = doc["imgPerfil"].toString()
            usuario.esAdmin = doc["esAdmin"] as Boolean


            /**
             * Creamos una publicacion y utilizamos los datos del usuario para generar la publicacion.
             */
            publi.id = "a"
            publi.imgPubli = usuario.email
            publi.perfil = usuario.nombre + " " + usuario.apellidos
            publi.titulo = titulo.text.toString()
            publi.descripcion = descripcion.text.toString()

            if (binding.optionDam.isChecked) {
                publi.checked = "DAM"

            } else if (binding.optionDaw.isChecked) {
                publi.checked = "DAW"

            } else if (binding.optionSmix.isChecked) {
                publi.checked = "SMIR"

            } else if (binding.optionAsix.isChecked) {
                publi.checked = "ASIR"
            }
            publi.pathFile = nombreArchivo
            publi.idCiclo =ciclo
            publi.idModulo = modulo
            publi.idUf = uf
            publi.enlace = enlace.text.toString()
            /**
             * Si la ID no esta vacia, añadiremos la publicacion en el Storage.
             */

            anadirPublicacion(publi)

        }
    }

    /**
     * Función para cargar los modulos a través de una consulta a la base de datos
     *
     * @param idCiclo
     */
    private fun cargarModulos(idCiclo: String) {
        val listaModulos = ArrayList<String>()
        arrayIdModulo = ArrayList()
        bd.collection("Ciclos").document(idCiclo).collection("Modulos").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    arrayIdModulo.add(document.id)
                    listaModulos.add(document["nombre"].toString())
                }

                val adapter = ArrayAdapter<String>(requireContext(), R.layout.simple_spinner_item)
                adapter.addAll(listaModulos)
                idModuloSpinner.adapter = adapter

            }
    }

    /**
     * Función para añadir publicación
     *
     * @param checked
     * @param idModulo
     * @param idUf
     */

    private fun anadirPublicacion(publi: Publicacion
    ) {

        val appContext = context
        /***
         * En añadir publicacion se define la ruta donde se guardara la publicacion.
         * La variable checked sera la id del Ciclo, el idModulo y idUf lo escribimos
         * en los EditText abajo.
         */

        if (!URLUtil.isValidUrl(publi.enlace)) {
            Snackbar.make(
                binding.constraintNuevaPublicacion,
                getString(cat.copernic.fpshare.R.string.invalidURL),
                Snackbar.LENGTH_LONG
            ).show()
        } else if (!algoVacio(publi.titulo, publi.descripcion, publi.enlace)) {
            Snackbar.make(
                binding.constraintNuevaPublicacion,
                getString(cat.copernic.fpshare.R.string.errorCamposVacios),
                Snackbar.LENGTH_LONG
            ).show()
        } else if (limiteCaracteresTitulo(publi.titulo)) {
            Snackbar.make(
                binding.constraintNuevaPublicacion,
                getString(cat.copernic.fpshare.R.string.errorTituloLargo),
                Snackbar.LENGTH_LONG
            ).show()
        } else if (limiteCaracteresDescripcion(publi.descripcion)) {
            Snackbar.make(
                binding.constraintNuevaPublicacion,
                getString(cat.copernic.fpshare.R.string.errorDescripcionLarga),
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            bd.collection("Ciclos").document(publi.idCiclo).collection("Modulos").document(publi.idModulo)
                .collection("UFs").document(publi.idUf).collection("Publicaciones").add(publi)
                .addOnSuccessListener { //S'ha afegit el departament...
                    val view = binding.root
                    val action =
                        NuevaPublicacionDirections.actionNuevaPublicacionToPantallaPrincipal()
                    view.findNavController().navigate(action)
                }.addOnFailureListener { //No s'ha afegit el departament...
                    Toast.makeText(appContext, "Documento no añadido", Toast.LENGTH_LONG).show()
                }
        }
    }


    private fun algoVacio(
        titulo: String, descripcion: String, enlace: String
    ): Boolean {
        return titulo.isNotEmpty() && titulo.isNotBlank()
                && descripcion.isNotEmpty() && descripcion.isNotBlank()
                && enlace.isNotEmpty() && enlace.isNotBlank()
    }

    /**
     * Función para comprobar errores
     *
     * @param titulo
     *
     * @return boolean
     */
    private fun limiteCaracteresTitulo(titulo: String): Boolean {
        return titulo.length > 20
    }

    /**
     * Función para comprobar errores
     *
     * @param descripcion
     *
     * @return boolean
     */
    private fun limiteCaracteresDescripcion(descripcion: String): Boolean {
        return descripcion.length > 248
    }
}
