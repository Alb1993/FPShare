package cat.copernic.fpshare.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.fragment.navArgs
import cat.copernic.fpshare.databinding.FragmentAdminModPostBinding
import cat.copernic.fpshare.modelo.Publicacion
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Fragment de pantalla de edición de Publicaciones
 *
 * @author FPShare
 */
class FragmentAdminModPost : Fragment() {
    private var _binding: FragmentAdminModPostBinding? = null
    private val binding get() = _binding!!
    val bd = FirebaseFirestore.getInstance()
    private lateinit var tituloEditText: EditText
    private lateinit var descripcionEditText: EditText
    private lateinit var enlaceEditText: EditText
    private lateinit var botonGuardar: Button
    private lateinit var botonEliminar: Button

    private val args: FragmentAdminModPostArgs by navArgs()

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
        _binding = FragmentAdminModPostBinding.inflate(inflater, container, false)
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
        inicializar() // Lectura de publicación
        botonGuardar.setOnClickListener {
            bd.collection("Ciclos").document(args.idCiclo).collection("Modulos")
                .document(args.idModulo)
                .collection("UFs").document(args.idUf)
                .collection("Publicaciones")
                .document(args.idPubli).update(
                    "titulo", tituloEditText.text.toString(),
                    "descripcion", descripcionEditText.text.toString(),
                    "enlace", enlaceEditText.text.toString()
                )
        }
        botonEliminar.setOnClickListener {
            bd.collection("Ciclos").document(args.idCiclo).collection("Modulos")
                .document(args.idModulo).collection("UFs").document(args.idUf)
                .collection("Publicaciones").document(args.idPubli).delete()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun inicializar() {
        tituloEditText = binding.textPost
        descripcionEditText = binding.textDescription
        enlaceEditText = binding.textLink

        botonGuardar = binding.btnPublish
        botonEliminar = binding.btnDelete
        /**
         * Leemos los datos de la publicación
         */
        bd.collection("Ciclos").document(args.idCiclo)
            .collection("Modulos").document(args.idModulo)
            .collection("UFs").document(args.idUf)
            .collection("Publicaciones").document(args.idPubli)
            .get().addOnSuccessListener {
                val publi = Publicacion(
                    it.id,
                    it["perfil"].toString(),
                    it["titulo"].toString(),
                    it["descripcion"].toString(),
                    it["checked"].toString(),
                    it["imgPubli"].toString()
                )
                tituloEditText.setText(publi.titulo) // Titulo introducido por usuario
                descripcionEditText.setText(publi.descripcion) // Descripcion introducida por usuario
            }
    }

}