package cat.copernic.fpshare.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.fpshare.adapters.PubliAdapter
import cat.copernic.fpshare.databinding.FragmentMenuApuntesBinding
import cat.copernic.fpshare.modelo.Publicacion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

/**
 * Clase de la pantalla MenuApuntes
 *
 * @author FPShare
 */
class MenuApuntes : Fragment(), SearchView.OnQueryTextListener {
    private var _binding: FragmentMenuApuntesBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView : RecyclerView
    val bd = FirebaseFirestore.getInstance()
    private lateinit var adapter: PubliAdapter
    private lateinit var cicloList: Deferred<MutableList<Publicacion>>
    private lateinit var searchView: SearchView

    val args: MenuApuntesArgs by navArgs()

    /**
     * Con esta función mostraremos el diseño de la pantalla ,mediante un View
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuApuntesBinding.inflate(inflater, container, false)
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
        recyclerView = binding.recyclerView
        searchView = binding.searchView
        searchView.setOnQueryTextListener(this)
        /***
         * Iniciamos la corrutina con lifecycleScope.launch y llamamos a crearMenu como
         * una funcion asincrona con la opcion async.
         * Para realizar esto, tenemos que declarar nuestra variable cicloList como
         * Deferred<MutableList<Publicacion>>.
         */
        lifecycleScope.launch(Dispatchers.Main){
            cicloList = async{ crearMenu()}
        }
    }

    /**
     * Con esta función destruimos la vista del fragemnt y limpiamos recursos para que el sistema funcione correctamente
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun crearMenu(): MutableList<Publicacion>{
        var cicloList = mutableListOf<Publicacion>()


        var publicaciones = bd.collection("Ciclos").document(args.cicloId).collection("Modulos").document(args.moduloId).collection("UFs").document(args.ufId).collection("Publicaciones").get().await()
        /***
         * Añadimos las publicaciones en una MutableList<Publicacion>
         */
                for (document in publicaciones){
                    val idPubli = document.id
                    val checked = document["checked"].toString()
                    val publiDescr = document["descripcion"].toString()

                    val publiProfile = document["perfil"].toString()
                    val publiTitle = document["titulo"].toString()
                    val imgPubli = document["imgPubli"].toString()
                    val publiCiclo = document["idCiclo"].toString()
                    var publiModulo = document["idModulo"].toString()
                    val publiUf = document["idUf"].toString()
                    val publiPath = document["pathFile"].toString()
                    val publi = Publicacion(idPubli,publiProfile,publiTitle,publiDescr,checked, imgPubli, publiCiclo, publiModulo, publiUf,publiPath)
                    cicloList.add(publi)
                }
                /***
                 * Cargamos las UFs en el adapter y mostramos por pantalla.
                 */
                adapter= PubliAdapter(cicloList)
                binding.recyclerView.adapter = adapter
                binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())


        return cicloList
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        try {
            adapter.filter.filter(query)
        }catch (e: UninitializedPropertyAccessException){
            println("error")
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        try {
            adapter.filter.filter(newText)
        }catch (e: UninitializedPropertyAccessException){
            println("error")
        }

        return false
    }

}