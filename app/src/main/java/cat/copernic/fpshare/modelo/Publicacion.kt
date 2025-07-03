package cat.copernic.fpshare.modelo

import com.google.firebase.firestore.Exclude

/**
 * Clase para crear objetos de tipo Publicación
 */
data class Publicacion(
    @get:Exclude var id: String = "",
    var perfil: String = "",
    var titulo: String = "",
    var descripcion: String = "",
    var checked: String = "",
    var imgPubli: String = "",
    var idCiclo: String = "",
    var idModulo: String = "",
    var idUf: String = "",
    var pathFile: String = ""
)