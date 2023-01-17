package cat.copernic.fpshare.adapters

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.fpshare.R
import cat.copernic.fpshare.databinding.ItemPubliBinding
import cat.copernic.fpshare.modelo.Publicacion
import com.google.firebase.storage.FirebaseStorage
import java.io.File

/**
 * Adaptador para la visualización de publicaciones en un recyclerView
 *
 * @author FPShare
 *
 * @param publicaciones
 */
class PubliAdapter(private val publicaciones: List<Publicacion>) : RecyclerView.Adapter<PubliAdapter
.PubliViewHolder>(), Filterable {

    private lateinit var contexto: Context
    var storage = FirebaseStorage.getInstance()

    /**
     * Cargamos en publiFilter la lista de publicaciones que entran por el Adapter.
     */
    var publiFilter: List<Publicacion> = publicaciones

    inner class PubliViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        val viewB = ItemPubliBinding.bind(view)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PubliViewHolder {
        contexto = viewGroup.context
        val view = LayoutInflater.from(contexto)
            .inflate(R.layout.item_publi, viewGroup, false)

        return PubliViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: PubliViewHolder, position: Int) {
        val publicacion = publiFilter.get(position)

        with(viewHolder) {
            /**
             * Ponemos los textos en cada recuadro del Item Publi.
             */
            viewB.txtProf.text = publicacion.perfil
            viewB.txtPubliTitle.text = publicacion.titulo
            viewB.txtDescr.text = publicacion.descripcion
            viewB.textLink.text = publicacion.enlace

            /*val db = FirebaseFirestore.getInstance()

            db.collection("Ciclos").document(publicacion.checked)
                .collection("Modulos").document(publicacion.idModulo)
                .get().addOnSuccessListener { document ->
                val nombreModulo = document["nombre"].toString()
                viewB.textModulo.setText(nombreModulo)
            }*/

            /**
             * Carga de la ruta del enlace a la imagen de la publicacion
             *
             * Colocamos la imagen en el ImageView del Item Publi.
             */

                val storageRef = storage.reference.child("Imagenes/" + publicacion.imgPubli)
                val localfile = File.createTempFile("tempImage", "jpg")
                storageRef.getFile(localfile).addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                    viewB.imgIcon.setImageBitmap(bitmap)
                }


            val path = publicacion.pathFile.toUri()
            /*
            val pdfRef = storageRef.child("pdfs/${path.lastPathSegment}")
            pdfRef.putFile(path).addOnSuccessListener { taskSnapshot ->
                val pdfName = taskSnapshot.metadata?.name
                viewB.textLink.text = pdfName
            }
*/
            viewB.txtDescarga.setOnClickListener {
                val queryUrl: Uri = Uri.parse(publicacion.pathFile)
                val intent = Intent(Intent.ACTION_VIEW, queryUrl)
                contexto.startActivity(intent)

            }
            /**
             * Inicializacion del enlace
             */
            viewB.textLink.setOnClickListener {
                val queryUrl: Uri = Uri.parse(publicacion.enlace)
                val intent = Intent(Intent.ACTION_VIEW, queryUrl)
                contexto.startActivity(intent)

            }
        }

    }

    override fun getItemCount(): Int {
        return publiFilter.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""
                /**
                 * Si no hay una query, el adapter devolvera todas las publicaciones. Para ello,
                 * las cargará en publiFilter, puesto que no hay ningun filtro.
                 */
                if (charString.isEmpty()) {
                    publiFilter = publicaciones
                } else {
                    /**
                     * Iniciamos una lista que contendrá los resultados filtrados.
                     */
                    val filteredList = mutableListOf<Publicacion>()
                    /**
                     * Definimos el filtro, donde comprobaremos si el titulo de la publicacion contiene la query escrita en el buscador.
                     */
                    publicaciones
                        .filter {
                            (it.titulo.toLowerCase()
                                .contains(constraint!!.toString().toLowerCase()))
                        }
                        /**
                         * Todos los resultados que contienen la query en el titulo de la
                         * publicacion seran añadidos en la lista para, despues, pasarlos a
                         * publiFilter.
                         */
                        .forEach { filteredList.add(it) }
                    publiFilter = filteredList

                }
                /**
                 * Retornamos todos los valores.
                 */
                return FilterResults().apply { values = publiFilter}
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                /**
                 * Si no hay un valor en values, publiFilter será una lista vacia.
                 */
                publiFilter = if (results?.values == null)
                    mutableListOf()
                /**
                 * Sino, añadira los valores a una Lista de publicaciones.
                 */
                else
                    results.values as List<Publicacion>
                notifyDataSetChanged()
            }
        }
    }
}

