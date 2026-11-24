package com.mpazpro3.app.data.remote

import java.io.IOException

/** El destino no está permitido (por ejemplo, http:// hacia un servidor público). No es un problema de red. */
class ConexionNoPermitidaException(mensaje: String) : IOException(mensaje)

/**
 * Traduce los fallos de red, HTTP y de formato a mensajes en español para el usuario.
 *
 * Los textos ya están definidos (son el contrato que verifican las pruebas); tu trabajo es decidir
 * cuál corresponde a cada fallo.
 */
object ErroresApi {

    const val MSG_SIN_CONEXION = "No se pudo conectar con el servidor. Revisa tu conexión y la dirección del servidor."
    const val MSG_TIEMPO_AGOTADO = "El servidor tardó demasiado en responder. Inténtalo de nuevo."
    const val MSG_RESPUESTA_INVALIDA = "El servidor respondió con datos que la app no entiende."
    const val MSG_SESION_EXPIRADA = "Tu sesión expiró. Cierra sesión y vuelve a ingresar."
    const val MSG_SIN_PERMISO = "No tienes permiso para realizar esta acción."
    const val MSG_NO_ENCONTRADO = "No se encontró lo que se pidió."
    const val MSG_CONFLICTO = "La acción no se puede realizar con el estado actual de los datos."
    const val MSG_DATOS_INVALIDOS = "Hay datos que el servidor no aceptó. Revísalos."
    const val MSG_SOLICITUD = "La solicitud no es válida."
    const val MSG_SERVIDOR = "El servidor tuvo un problema. Inténtalo de nuevo en un momento."
    const val MSG_INESPERADO = "Ocurrió un error inesperado. Inténtalo de nuevo."
    const val MSG_SIN_SESION_REMOTA =
        "Ingresaste sin conexión: cierra sesión y vuelve a ingresar con conexión para sincronizar."

    /**
     * TODO(E3-3a) Mensaje genérico para un código HTTP, usado si el servidor no envió uno propio.
     *
     * Archivo: este. Qué hacer: devolver MSG_SOLICITUD (400), MSG_SESION_EXPIRADA (401),
     * MSG_SIN_PERMISO (403), MSG_NO_ENCONTRADO (404), MSG_CONFLICTO (409), MSG_DATOS_INVALIDOS (422),
     * MSG_SERVIDOR (500..599) y MSG_INESPERADO para cualquier otro código.
     * Aceptación: mensajePorCodigo(404) == MSG_NO_ENCONTRADO y mensajePorCodigo(503) == MSG_SERVIDOR.
     */
    fun mensajePorCodigo(codigo: Int): String =
        TODO("E3-3a: devolver el mensaje que corresponde a cada código HTTP")

    /**
     * TODO(E3-3b) Lee el cuerpo de error del servidor, que tiene la forma { "error": "...", "campo": "..." }
     * (ver ErrorDto y docs/API.md del backend).
     *
     * Qué hacer: con Gson, convertir [cuerpo] a ErrorDto. Si "error" viene con texto, ese es el mensaje;
     * si el cuerpo está vacío, no es JSON o no trae "error", usar mensajePorCodigo(codigo). El campo
     * "campo" (si viene) se copia al resultado.
     * Aceptación: errorDeCuerpo(409, """{"error":"Sin intentos"}""") -> Error(409, "Sin intentos", null);
     * errorDeCuerpo(500, "<html>") -> Error(500, MSG_SERVIDOR, null) (sin lanzar excepciones).
     */
    fun errorDeCuerpo(codigo: Int, cuerpo: String?): ApiResult.Error =
        TODO("E3-3b: interpretar el cuerpo { error, campo } del servidor")

    /**
     * TODO(E3-3c) Convierte cualquier excepción de una llamada en el ApiResult que corresponde.
     *
     * Qué hacer (cada caso con su mensaje en español):
     *  - CancellationException: volver a lanzarla (nunca se traga).
     *  - retrofit2.HttpException: leer t.response()?.errorBody()?.string() y usar errorDeCuerpo(t.code(), cuerpo).
     *  - ConexionNoPermitidaException: ApiResult.Error(0, mensaje de la excepción).
     *  - JsonParseException / MalformedJsonException / EOFException (cuerpo mal formado): Error(0, MSG_RESPUESTA_INVALIDA).
     *  - InterruptedIOException (tiempo agotado, SocketTimeoutException): SinConexion(MSG_TIEMPO_AGOTADO).
     *  - cualquier otra IOException (sin red, servidor caído): SinConexion(MSG_SIN_CONEXION).
     *  - lo demás: Error(0, MSG_INESPERADO).
     * Aceptación: traducir(SocketTimeoutException()) es SinConexion con MSG_TIEMPO_AGOTADO (y NO el de
     * IOException: SocketTimeoutException hereda de InterruptedIOException, que hereda de IOException; el orden de
     * los casos importa).
     */
    fun traducir(t: Throwable): ApiResult<Nothing> =
        TODO("E3-3c: traducir excepciones a ApiResult con mensajes en español")
}

/**
 * TODO(E3-3d) Ejecuta una llamada a la API y devuelve su resultado sin lanzar excepciones
 * (salvo la cancelación de la corrutina).
 *
 * Qué hacer: try { ApiResult.Exito(bloque()) } catch (CancellationException) { throw } catch (Exception) { ErroresApi.traducir(e) }.
 * Aceptación: llamarApi { 5 } == Exito(5); llamarApi { throw IOException() } es SinConexion; cancelar la
 * corrutina no devuelve un resultado.
 */
suspend fun <T> llamarApi(bloque: suspend () -> T): ApiResult<T> =
    TODO("E3-3d: envolver la llamada en try/catch y traducir los errores")
