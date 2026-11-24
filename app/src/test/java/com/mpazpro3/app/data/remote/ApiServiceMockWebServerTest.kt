package com.mpazpro3.app.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/**
 * TODO(E3-T1) Pruebas del ApiService REAL (Retrofit + OkHttp + Gson) contra un servidor simulado (MockWebServer).
 * Se completan en la Parte 4 de la guía, después de E3-2 y E3-3. Para activarlas, quita el @Ignore de la clase.
 *
 * Cada caso: encolar una respuesta en [servidor], llamar con [llamar] y comprobar el ApiResult y, cuando
 * corresponda, la petición recibida (servidor.takeRequest(): método, ruta, encabezados y cuerpo).
 *
 * Casos que debe cubrir (mínimo 15 entre este archivo y los demás de capa remota y repositorio):
 *  1.  salud: 200 {"estado":"ok",...} -> Exito y la ruta pedida es /health.
 *  2.  login: envía el cuerpo {"usuario":..,"clave":..} por POST /auth/login y lee token y usuario.
 *  3.  contenidos con filtros: ?asignatura=MATEMATICA llega en la ruta; lista vacía [] es Exito.
 *  4.  asignar: el curso va codificado en la ruta (PUT /cursos/3%C2%B0A/asignaciones) y el cuerpo lleva contenidoIds y maxIntentos.
 *  5.  con sesion.token = "abc" la petición lleva "Authorization: Bearer abc"; sin sesión no lleva el encabezado.
 *  6.  400 con {"error":"..."} -> Error(400, mensaje del servidor).
 *  7.  401 -> Error(401, ...) (sesión inválida).
 *  8.  403 sin cuerpo -> Error(403, ErroresApi.MSG_SIN_PERMISO).
 *  9.  404 -> Error(404, ...).
 *  10. 409 con {"error":"...","campo":"..."} -> Error(409, mensaje, campo) (reglas RN-02/03/04).
 *  11. 422 con campo -> Error(422, mensaje, campo).
 *  12. 500 con cuerpo HTML -> Error(500, ErroresApi.MSG_SERVIDOR) sin lanzar excepciones.
 *  13. 200 con JSON mal formado -> Error(0, ErroresApi.MSG_RESPUESTA_INVALIDA).
 *  14. timeout: respuesta con SocketPolicy.NO_RESPONSE y lecturaSeg = 1 -> SinConexion(ErroresApi.MSG_TIEMPO_AGOTADO).
 *  15. servidor apagado (servidor.shutdown()) -> SinConexion(ErroresApi.MSG_SIN_CONEXION).
 *  16. resultadosCsv devuelve el texto tal cual (ResponseBody.string()).
 *  17. http:// hacia un servidor público es rechazado por ProteccionHttpInterceptor sin enviar nada.
 */
@Ignore("E3: completar")
class ApiServiceMockWebServerTest {

    private lateinit var servidor: MockWebServer
    private lateinit var sesion: SesionApi
    private lateinit var api: ApiService

    @Before
    fun preparar() {
        servidor = MockWebServer()
        servidor.start()
        sesion = SesionApi()
        // Tiempos cortos para que la prueba de timeout no se demore.
        api = ApiClient.crearServicio(servidor.url("/").toString(), sesion, conexionSeg = 2, lecturaSeg = 1)
    }

    @After
    fun cerrar() {
        servidor.shutdown()
    }

    private fun json(codigo: Int, cuerpo: String) = MockResponse()
        .setResponseCode(codigo)
        .setHeader("Content-Type", "application/json; charset=utf-8")
        .setBody(cuerpo)

    /** Ejecuta la llamada igual que lo hará el repositorio: sin excepciones, siempre un ApiResult. */
    private fun <T> llamar(bloque: suspend (ApiService) -> T): ApiResult<T> =
        runBlocking { llamarApi { bloque(api) } }

    @Test
    fun salud_leeElJson() {
        // TODO(E3-T1): servidor.enqueue(json(200, ...)); val r = llamar { it.salud() }; comprobar r y la ruta "/health"
    }

    @Test
    fun login_enviaElCuerpoYDevuelveElToken() {
        // TODO(E3-T1): POST /auth/login con {"usuario":"utp","clave":"1234"}; leer token y perfil
    }

    @Test
    fun contenidos_conFiltro_yListaVacia() {
        // TODO(E3-T1): /contenidos?asignatura=MATEMATICA y lista vacía como Exito
    }

    @Test
    fun asignar_codificaElCursoYEnviaElCuerpo() {
        // TODO(E3-T1): PUT /cursos/{curso}/asignaciones con AsignarRequest
    }

    @Test
    fun conSesion_agregaElEncabezadoBearer_ySinSesionNo() {
        // TODO(E3-T1): servidor.takeRequest().getHeader("Authorization")
    }

    @Test
    fun error400_401_403_404_usanElMensajeDelServidorOElGenerico() {
        // TODO(E3-T1): un caso por código (puedes separarlos en funciones distintas)
    }

    @Test
    fun error409_traeMensajeYCampo() {
        // TODO(E3-T1): {"error":"Ya hay una unidad aprobada","campo":"asignatura"}
    }

    @Test
    fun error422_conCampo() {
        // TODO(E3-T1)
    }

    @Test
    fun error500_conHtml_usaElMensajeGenerico() {
        // TODO(E3-T1)
    }

    @Test
    fun cuerpoMalFormado_esRespuestaInvalida() {
        // TODO(E3-T1): json(200, "{no es json")
    }

    @Test
    fun timeout_esSinConexionConMensajeDeTiempoAgotado() {
        // TODO(E3-T1): MockResponse().setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.NO_RESPONSE)
    }

    @Test
    fun servidorApagado_esSinConexion() {
        // TODO(E3-T1): servidor.shutdown() antes de llamar
    }

    @Test
    fun resultadosCsv_devuelveElTexto() {
        // TODO(E3-T1)
    }

    @Test
    fun httpHaciaServidorPublico_seRechazaSinEnviar() {
        // TODO(E3-T1): ApiClient.crearServicio("http://ejemplo.com/", sesion) y comprobar el ApiResult.Error
    }
}
