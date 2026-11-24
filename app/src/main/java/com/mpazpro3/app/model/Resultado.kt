package com.mpazpro3.app.model

/**
 * Resultado de una acción de negocio: si salió bien o no, y el mensaje que ve el usuario.
 * Lo usan las reglas (domain/), el repositorio y el ViewModel; las pantallas solo lo muestran.
 */
data class Resultado(
    val exito: Boolean,
    val mensaje: String
) {
    companion object {
        fun ok(mensaje: String) = Resultado(true, mensaje)
        fun error(mensaje: String) = Resultado(false, mensaje)
    }
}
