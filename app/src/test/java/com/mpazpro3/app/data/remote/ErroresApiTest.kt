package com.mpazpro3.app.data.remote

import org.junit.Ignore
import org.junit.Test

/**
 * TODO(E3-T2) Pruebas puras (sin red) de ErroresApi y llamarApi. Se completan después de E3-3.
 *
 * Casos que debe cubrir:
 *  1. mensajePorCodigo: 400, 401, 403, 404, 409, 422, 500 y un código raro (302) -> su constante MSG_*.
 *  2. errorDeCuerpo con {"error":"x","campo":"y"} -> Error(codigo, "x", "y").
 *  3. errorDeCuerpo con cuerpo vacío / null / HTML / JSON sin "error" -> mensaje genérico del código, sin lanzar.
 *  4. traducir(HttpException con cuerpo JSON) -> Error con el mensaje del servidor (usa errorHttp de Fakes.kt).
 *  5. traducir(SocketTimeoutException) -> SinConexion(MSG_TIEMPO_AGOTADO); traducir(IOException) / UnknownHostException -> SinConexion(MSG_SIN_CONEXION).
 *  6. traducir(JsonSyntaxException) -> Error(0, MSG_RESPUESTA_INVALIDA).
 *  7. traducir(ConexionNoPermitidaException("x")) -> Error(0, "x") y NO SinConexion.
 *  8. traducir(RuntimeException) -> Error(0, MSG_INESPERADO); traducir(CancellationException) la vuelve a lanzar.
 *  9. llamarApi { 5 } -> Exito(5); llamarApi { throw IOException() } -> SinConexion.
 */
@Ignore("E3: completar")
class ErroresApiTest {

    @Test
    fun mensajePorCodigo_cubreLosCodigosDelContrato() {
        // TODO(E3-T2)
    }

    @Test
    fun errorDeCuerpo_conJsonDelServidor_yConCuerposInvalidos() {
        // TODO(E3-T2)
    }

    @Test
    fun traducir_httpException_usaElCuerpoDelServidor() {
        // TODO(E3-T2)
    }

    @Test
    fun traducir_timeout_sinRed_yFormato() {
        // TODO(E3-T2)
    }

    @Test
    fun traducir_cancelacionSeRelanza_yLoInesperado() {
        // TODO(E3-T2)
    }

    @Test
    fun llamarApi_exitoYError() {
        // TODO(E3-T2)
    }
}
