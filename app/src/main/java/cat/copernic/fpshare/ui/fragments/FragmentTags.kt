package cat.copernic.fpshare.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.fpshare.adapters.CicleAdminAdapter
import cat.copernic.fpshare.adapters.ModulAdminAdapter
import cat.copernic.fpshare.adapters.UfAdminAdapter
import cat.copernic.fpshare.databinding.FragmentTagsBinding
import cat.copernic.fpshare.modelo.Cicle
import cat.copernic.fpshare.modelo.Foro
import cat.copernic.fpshare.modelo.Modul
import cat.copernic.fpshare.modelo.Uf

class FragmentTags : Fragment() {
    private var _binding: FragmentTagsBinding? = null
    private val binding get() = _binding!!

    // Botones
    private lateinit var botonAddCiclo: Button
    private lateinit var botonDeleteCiclo: Button
    private lateinit var botonAddModulo: Button
    private lateinit var botonDeleteModulo: Button
    private lateinit var botonAddUF: Button
    private lateinit var botonDeleteUF: Button

    // RecyclerViews
    private lateinit var recyclerViewCiclos: RecyclerView
    private lateinit var recyclerViewModulos: RecyclerView
    private lateinit var recyclerViewUFs: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTagsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        inicializadoresButton()
        inicializadoresRW()
        listeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun inicializadoresButton() {
        // inicializar botones de ciclo
        botonAddCiclo = binding.buttonAddCiclo
        botonDeleteCiclo = binding.buttonDeleteCicle

        // inicializar botones de modulos
        botonAddModulo = binding.buttonAddModulo
        botonDeleteModulo = binding.buttonDeleteModule

        // inicializar botones de UFs
        botonAddUF = binding.buttonAddUF
        botonDeleteUF = binding.buttonDeleteUF
    }

    fun inicializadoresRW() {
        // inicializar recyclerViews
        recyclerViewCiclos = binding.recyclerViewCiclos
        recyclerViewModulos = binding.recyclerViewModulos
        recyclerViewUFs = binding.recyclerViewUFs

        // recyclerView de Ciclos
        recyclerViewCiclos.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewCiclos.adapter = CicleAdminAdapter(obtenerCiclos())

        // recyclerView de Modulos
        recyclerViewModulos.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewModulos.adapter = ModulAdminAdapter(obtenerModulos())

        // recyclerView de UFs
        recyclerViewUFs.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewUFs.adapter = UfAdminAdapter(obtenerUFs())
    }

    fun listeners() {
        botonAddCiclo.setOnClickListener() {
            val action =
                FragmentTagsDirections.actionListaTagsAdministracionToCrearCiclo()
            view?.findNavController()?.navigate(action)
        }
        botonAddModulo.setOnClickListener() {
            val action =
                FragmentTagsDirections.actionListaTagsAdministracionToCrearModulo()
            view?.findNavController()?.navigate(action)
        }
        botonAddUF.setOnClickListener() {
            val action =
                FragmentTagsDirections.actionListaTagsAdministracionToCrearUF()
            view?.findNavController()?.navigate(action)
        }
    }

    fun obtenerCiclos(): MutableList<Cicle> {
        val ciclos = mutableListOf<Cicle>()

        for(num in 1..30){

            ciclos.add(Cicle("ID","Nombre Ciclo", emptyList()))

        }

        return ciclos
    }

    fun obtenerModulos(): MutableList<Modul> {
        val modulos = mutableListOf<Modul>()

        for(num in 1..30){

            modulos.add(Modul("ID","Nombre Modulo", emptyList()))

        }

        return modulos
    }

    fun obtenerUFs(): MutableList<Uf> {
        val UFs = mutableListOf<Uf>()

        for(num in 1..30){

            UFs.add(Uf("ID","Nombre UF"))

        }

        return UFs
    }
}