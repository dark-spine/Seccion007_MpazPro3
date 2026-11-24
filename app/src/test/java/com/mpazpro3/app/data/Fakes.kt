package com.mpazpro3.app.data

import com.mpazpro3.app.data.remote.ActividadDto
import com.mpazpro3.app.data.remote.ApiService
import com.mpazpro3.app.data.remote.AsignacionDto
import com.mpazpro3.app.data.remote.AsignarRequest
import com.mpazpro3.app.data.remote.AsignarResponse
import com.mpazpro3.app.data.remote.Conectividad
import com.mpazpro3.app.data.remote.ContenidoDto
import com.mpazpro3.app.data.remote.ContenidoResponse
import com.mpazpro3.app.data.remote.DesafioResponse
import com.mpazpro3.app.data.remote.EstudianteDto
import com.mpazpro3.app.data.remote.EvaluacionRequest
import com.mpazpro3.app.data.remote.EvaluacionResponse
import com.mpazpro3.app.data.remote.InformeDto
import com.mpazpro3.app.data.remote.InformeResponse
import com.mpazpro3.app.data.remote.LoginRequest
import com.mpazpro3.app.data.remote.LoginResponse
import com.mpazpro3.app.data.remote.MaterialResponse
import com.mpazpro3.app.data.remote.ObservacionRequest
import com.mpazpro3.app.data.remote.RecursoDto
import com.mpazpro3.app.data.remote.RespuestaRequest
import com.mpazpro3.app.data.remote.SaludDto
import com.mpazpro3.app.data.remote.SeguimientoDto
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.InformeDocente
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

/*
 * Utilidades de prueba para la Experiencia 3 (ya hechas): ApiService, conectividad y copia local falsos.
 * Úsalas en RepositorioSincronizadoTest; para la capa remota real se usa MockWebServer.
 */

/** Error HTTP como el que lanza Retrofit, con el cuerpo JSON del servidor. */
fun errorHttp(codigo: Int, cuerpo: String): HttpException =
    HttpException(Response.error<Any>(codigo, cuerpo.toResponseBody("application/json".toMediaType())))

fun errorServidor(codigo: Int, mensaje: String, campo: String? = null): HttpException {
    val extra = if (campo == null) "" else ", \"campo\": \"$campo\""
    return errorHttp(codigo, "{\"error\": \"$mensaje\"$extra}")
}

/**
 * ApiService de prueba: cada ruta se programa con un lambda que devuelve la respuesta o lanza la
 * excepción deseada. Una ruta no programada hace fallar la prueba.
 */
class FakeApiService : ApiService {
    val llamadas = mutableListOf<String>()
    val rutas = mutableMapOf<String, () -> Any?>()

    fun programar(nombre: String, respuesta: () -> Any?) {
        rutas[nombre] = respuesta
    }

    fun cantidad(nombre: String) = llamadas.count { it == nombre }

    @Suppress("UNCHECKED_CAST")
    private fun <T> atender(nombre: String): T {
        llamadas += nombre
        val f = rutas[nombre] ?: throw IllegalStateException("Llamada no programada: $nombre")
        return f() as T
    }

    override suspend fun salud(): SaludDto = atender("salud")
    override suspend fun login(solicitud: LoginRequest): LoginResponse {
        ultimoLogin = solicitud
        return atender("login")
    }
    override suspend fun logout(autorizacion: String): Response<Unit> {
        ultimoLogout = autorizacion
        return atender("logout")
    }
    override suspend fun contenidos(asignatura: String?, estado: String?): List<ContenidoDto> = atender("contenidos")
    override suspend fun contenido(id: String): ContenidoDto = atender("contenido")
    override suspend fun aprobarContenido(id: String, solicitud: ObservacionRequest): ContenidoResponse {
        ultimaObservacion = solicitud
        return atender("aprobarContenido")
    }
    override suspend fun rechazarContenido(id: String, solicitud: ObservacionRequest): ContenidoResponse {
        ultimaObservacion = solicitud
        return atender("rechazarContenido")
    }
    override suspend fun recursos(contenidoId: String?): List<RecursoDto> = atender("recursos")
    override suspend fun actividades(contenidoId: String?, tipo: String?): List<ActividadDto> = atender("actividades")
    override suspend fun asignaciones(curso: String?): List<AsignacionDto> = atender("asignaciones")
    override suspend fun asignar(curso: String, solicitud: AsignarRequest): AsignarResponse {
        ultimaAsignacion = solicitud
        return atender("asignar")
    }
    override suspend fun estudiantes(curso: String?): List<EstudianteDto> = atender("estudiantes")
    override suspend fun seguimientos(estudianteId: String?, contenidoId: String?): List<SeguimientoDto> = atender("seguimientos")
    override suspend fun materialRevisado(estudianteId: String, contenidoId: String): MaterialResponse = atender("materialRevisado")
    override suspend fun responderDesafio(estudianteId: String, actividadId: String, solicitud: RespuestaRequest): DesafioResponse {
        respuestasEnviadas += actividadId to solicitud.alternativa
        return atender("responderDesafio")
    }
    override suspend fun enviarEvaluacion(estudianteId: String, contenidoId: String, solicitud: EvaluacionRequest): EvaluacionResponse =
        atender("enviarEvaluacion")
    override suspend fun resultadosCsv(curso: String): ResponseBody = atender("resultadosCsv")
    override suspend fun informes(estudianteId: String?): List<InformeDto> = atender("informes")
    override suspend fun generarInforme(estudianteId: String): InformeResponse = atender("generarInforme")
    override suspend fun validarInforme(id: String): InformeResponse = atender("validarInforme")

    var ultimoLogin: LoginRequest? = null
    var ultimoLogout: String? = null
    var ultimaObservacion: ObservacionRequest? = null
    var ultimaAsignacion: AsignarRequest? = null
    val respuestasEnviadas = mutableListOf<Pair<String, Int>>()
}

class FakeConectividad(var hayRed: Boolean = true) : Conectividad {
    override fun hayRed(): Boolean = hayRed
}

/** Copia local en memoria que registra lo que el repositorio le pide guardar. */
class FakeCacheLocal : CacheLocal {
    var instantanea: Instantanea? = null
    var reemplazos = 0
    val contenidos = mutableListOf<Contenido>()
    val asignaciones = mutableMapOf<String, List<Asignacion>>()
    val seguimientos = mutableListOf<SeguimientoAvance>()
    val informes = mutableListOf<InformeDocente>()
    val usuarios = mutableListOf<Usuario>()
    val respuestasLocales = mutableListOf<Triple<String, String, Int>>()
    val materialesLocales = mutableListOf<Pair<String, String>>()

    /** Lo que devuelve registrarRespuestaLocal (null = no válida). */
    var respuestaLocal: RespuestaLocal? = RespuestaLocal(true, "C2")
    var materialLocalValido = true
    var fallarAlGuardar = false

    override suspend fun reemplazarTodo(instantanea: Instantanea) {
        if (fallarAlGuardar) throw IllegalStateException("disco lleno")
        this.instantanea = instantanea
        reemplazos++
    }

    override suspend fun guardarContenido(contenido: Contenido) {
        if (fallarAlGuardar) throw IllegalStateException("disco lleno")
        contenidos += contenido
    }

    override suspend fun guardarAsignaciones(curso: String, asignaciones: List<Asignacion>) {
        this.asignaciones[curso] = asignaciones
    }

    override suspend fun guardarSeguimiento(seguimiento: SeguimientoAvance) {
        seguimientos += seguimiento
    }

    override suspend fun guardarInforme(informe: InformeDocente) {
        informes += informe
    }

    override suspend fun guardarUsuario(usuario: Usuario) {
        usuarios.removeAll { it.id == usuario.id }
        usuarios += usuario
    }

    override suspend fun autenticarLocal(usuarioNormalizado: String, claveHash: String): Usuario? =
        usuarios.firstOrNull { it.usuario.lowercase() == usuarioNormalizado && it.claveHash == claveHash }

    override suspend fun registrarRespuestaLocal(estudianteId: String, actividadId: String, alternativa: Int): RespuestaLocal? {
        respuestasLocales += Triple(estudianteId, actividadId, alternativa)
        return respuestaLocal
    }

    override suspend fun marcarMaterialLocal(estudianteId: String, contenidoId: String): Boolean {
        materialesLocales += estudianteId to contenidoId
        return materialLocalValido
    }
}
