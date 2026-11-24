package com.mpazpro3.app.data.remote

/**
 * Resultado de una llamada a la API:
 *  - Exito: llegó una respuesta correcta;
 *  - Error: el servidor respondió con un error (negocio, permisos, datos inválidos) o con algo
 *    que la app no entiende; trae un mensaje en español para el usuario;
 *  - SinConexion: no se pudo hablar con el servidor (sin red, caído, tiempo agotado).
 */
sealed class ApiResult<out T> {

    data class Exito<out T>(val datos: T) : ApiResult<T>()

    /** codigo: código HTTP (0 si la respuesta no se pudo interpretar). campo: campo del formulario afectado. */
    data class Error(val codigo: Int, val mensaje: String, val campo: String? = null) : ApiResult<Nothing>()

    data class SinConexion(val mensaje: String) : ApiResult<Nothing>()

    val esExito: Boolean get() = this is Exito

    fun datosONull(): T? = (this as? Exito)?.datos

    /** Transforma el dato de un Exito; si la transformación falla por datos inesperados, devuelve Error. */
    inline fun <R> transformar(f: (T) -> R): ApiResult<R> = when (this) {
        is Exito -> try {
            Exito(f(datos))
        } catch (e: IllegalArgumentException) {
            Error(0, ErroresApi.MSG_RESPUESTA_INVALIDA)
        } catch (e: IllegalStateException) {
            Error(0, ErroresApi.MSG_RESPUESTA_INVALIDA)
        }
        is Error -> this
        is SinConexion -> this
    }
}
