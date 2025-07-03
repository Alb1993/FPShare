package cat.copernic.fpshare.ui.fragments

import android.app.AlertDialog
import android.provider.Contacts

import android.app.ProgressDialog.show
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.*
import android.widget.*
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.fpshare.R
import cat.copernic.fpshare.adapters.PubliAdapter
import cat.copernic.fpshare.databinding.FragmentPantallaPrincipalBinding
import cat.copernic.fpshare.modelo.Publicacion
import cat.copernic.fpshare.ui.activities.Login
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.IOException

/**
 * Clase de la pantalla pantalla principal
 *
 * @author FPShare
 */
class pantalla_principal() : Fragment(), SearchView.OnQueryTextListener{
    private var _binding: FragmentPantallaPrincipalBinding? = null
    private val binding get() = _binding!!

    /**
     * Muestreo Adapter con todos
     */
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PubliAdapter
    private lateinit var arrayList: ArrayList<String>
    private lateinit var cicloList: Deferred<MutableList<Publicacion>>
    private lateinit var searchView: SearchView

    val bd = FirebaseFirestore.getInstance()

    /**
     * Con esta función mostraremos el diseño de la pantalla ,mediante un View
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPantallaPrincipalBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    /**
     * En esta función iniciamos  los diferentes elementos de la pantalla y creamos los listener de los eventos de los
     * elementos  de la vista
     *
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //si el usuario se ha logueado dos veces
        if (Login.vecesIniciado == 2){
            crearAlert()
            Login.vecesIniciado++

        }

        recyclerView = binding.recyclerView

        /***
         * Iniciamos el SearchView y Iniciamos un Listener para detectar el momento en que se hace la query.
         *
         */
        searchView = binding.searchView

        searchView.setOnQueryTextListener(this)
            lifecycleScope.launch(Dispatchers.Main){
                    cicloList = async { crearMenu() }
        }
    }

    /**
     * Con esta función creamos una alerta que pedira al usuario valorar la app
     */
    private fun crearAlert() {
        //creamos la alerta
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.valorar_app))
        builder.setMessage(getString(R.string.descripcion_valorar_app))

        builder.setPositiveButton(getString(R.string.aceptat)) { dialog, which ->
            // Acción a realizar al presionar el botón "Aceptar"

            //llamamos  a la función
            abrirPlayStore()
        }

        builder.setNegativeButton(getString(R.string.cancelar)) { dialog, which ->
            // Acción a realizar al presionar el botón "Cancelar"
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()

    }

    /**
     * Con esta función abntiremos el  Play Store
     * @throws ActivityNotFoundException si no se ecuentra la aplicación Play Store se abrirá el navegador con
     * la pagina web de play store en la pagina de la app
     */
    private fun abrirPlayStore() {
        //obtenemos una refencia del contexto actual
        val contexto = context

        //creamos la uri con la dirección de la pagina de detalles de la aplición en Plaay Store
        val uri = Uri.parse("market://details?id=" + contexto?.packageName)

        //creamos un intent para abir la pagina web con la uri que hemos creado
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)

        //agremos unas flags al intent para evitar quwe se guarde en el historial y se abra en una nueva tarea
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

        //intentamoas abrir la aplicación Play Store
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            //si no lo conseguimos abriremos una pagina en el navegador
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + contexto?.packageName)))
        }

    }

    /**
     * Con esta función destruimos la vista del fragemnt y limpiamos recursos para que el sistema funcione correctamente
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    suspend fun crearMenu(): MutableList<Publicacion>{
        var cicloList = mutableListOf<Publicacion>()
        arrayList = ArrayList()
        /***
         * Guardamos la ruta en una variable
         */
        val ciclo = bd.collection("Ciclos").get().await()
        /***
         * Recorremos la variable ciclos.
         */
        for(doc1 in ciclo){
            /***
             * Una vez que estamos recorriendo la variable ciclo, podemos consultar la id
             * del documento que estamos recorriendo.
             */
            val modulo = bd.collection("Ciclos").document(doc1.id)
                .collection("Modulos").get().await()
            for(doc2 in modulo){
                val uf = bd.collection("Ciclos").document(doc1.id).collection("Modulos").document(doc2.id)
                    .collection("UFs").get().await()
                for(doc3 in uf){
                    val publi = bd.collection("Ciclos").document(doc1.id).collection("Modulos").document(doc2.id)
                        .collection("UFs").document(doc3.id).collection("Publicaciones").get().await()
                    for(doc4 in publi){
                        /***
                         * Hacemos esto consecutivamente hasta llegar a las publicaciones.
                         * Una vez que llegamos aqui, recogemos todos los valores y guardamos las
                         * publicaciones en una MutableList<Publicacion>.
                         */
                        val idPubli = doc4.id
                        val checked = doc4["checked"].toString()
                        val publiDescr = doc4["descripcion"].toString()
                        val publiProfile = doc4["perfil"].toString()
                        val publiTitle = doc4["titulo"].toString()
                        val publiCiclo = doc4["idCiclo"].toString()
                        var publiModulo = doc4["idModulo"].toString()
                        val publiUf = doc4["idUf"].toString()
                        val publiPath = doc4["pathFile"].toString()

                        /***
                         * Añadimos el titulo de la publicacion para poder encontrarlo en el buscador.
                         */
                        arrayList.add(publiTitle)
                        val imgPubli = doc4["imgPubli"].toString()
                            val publi = Publicacion(
                                idPubli,
                                publiProfile,
                                publiTitle,
                                publiDescr,
                                checked,
                                imgPubli,
                                publiCiclo,
                                publiModulo,
                                publiUf,
                                publiPath

                            )

                        /***
                         * Cargamos la lista en el adapter para mostrar todas las publicaciones.
                         */
                        adapter = PubliAdapter(cicloList)
                        try {
                            binding.recyclerView.adapter = adapter
                            binding.recyclerView.layoutManager =
                                LinearLayoutManager(requireContext())
                            cicloList.add(publi)
                        }catch (e: NullPointerException){
                            println("error")
                        }

                    }

                }
            }
        }
        return cicloList
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        /***
         * Si hacemos una query i aceptamos, el adapter se filtrará segun lo que hayamos puesto en el buscador.
         */
        try {
            adapter.filter.filter(query)
        }catch (e: UninitializedPropertyAccessException){
            println("error")
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        /***
         * Si modificamos el texto escrito en la query, se actualizaran los valores con el nuevo texto escrito.
         */
        try {
            adapter.filter.filter(newText)
        }catch (e: UninitializedPropertyAccessException){
            println("error")
        }

        return false
    }


}