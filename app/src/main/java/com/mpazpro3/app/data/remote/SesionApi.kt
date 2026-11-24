package com.mpazpro3.app.data.remote

/** Token de la sesión activa (solo en memoria: al cerrar la app hay que ingresar de nuevo). */
class SesionApi {
    @Volatile
    var token: String? = null

    val activa: Boolean get() = !token.isNullOrBlank()

    fun limpiar() {
        token = null
    }
}
