package cat.copernic.fpshare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.fpshare.R
import cat.copernic.fpshare.databinding.ItemPubliBinding
import cat.copernic.fpshare.modelo.Publicacion
import com.squareup.picasso.Picasso

/**
 * Adaptador para ver las publicaciones en la pantalla de administración y en tus
 * propias publicaciones, con un listener para seleccionar una publicación
 *
 * @author FPShare
 *
 * @param publicaciones
 * @param listener
 */
class PubliAdminAdapter(
    private val publicaciones: List<Publicacion>, private val listener:
    OnItemClickListener
) : RecyclerView.Adapter<PubliAdminAdapter.PubliViewHolder>() {
    private lateinit var contexto: Context

    /**
     * Esta función es la que construye el aspecto de los items dentro del recyclerView a través
     * del archivo item (por ejemplo, item_tag)
     *
     * @param parent
     * @param viewType
     *
     * @return ViewHolder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PubliViewHolder {
        contexto = viewGroup.context
        val view = LayoutInflater.from(contexto)
            .inflate(R.layout.item_publi, viewGroup, false)

        return PubliViewHolder(view)
    }

    /**
     * Función que recoge las IDs de ufs y las muestra en el recyclerView
     *
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(viewHolder: PubliViewHolder, position: Int) {
        val publicacion = publicaciones.get(position)

        with(viewHolder) {
            viewB.txtProf.text = publicacion.perfil
            viewB.txtPubliTitle.text = publicacion.titulo
            viewB.txtDescr.text = publicacion.descripcion
            Picasso.get().load(publicacion.imgPubli.toUri()).into(viewB.imgIcon)
        }
    }

    /**
     * Clase interna para almacenar y enlazar los elementos de la vista
     *
     * @param view
     */
    inner class PubliViewHolder(var view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        val viewB = ItemPubliBinding.bind(view)

        init {
            view.setOnClickListener(this)
        }

        /**
         * funcion para manejar los eventos de clic en los elementos del recyclerview
         *
         * @param v
         */
        override fun onClick(v: View?) {
            val position: Int = adapterPosition
            val id = publicaciones.get(position).id
            val idCiclo = publicaciones.get(position).idCiclo
            val idModulo = publicaciones.get(position).idModulo
            val idUF = publicaciones.get(position).idUf
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(id, idCiclo, idModulo, idUF)
            }
        }

    }

    override fun getItemCount(): Int {
        return publicaciones.size
    }

    /**
     * Interfaz onItemClickListener para el click de selección en el recyclerView
     */
    interface OnItemClickListener {
        fun onItemClick(id: String, idCiclo: String, idModulo: String, idUF: String)
    }

}