package com.mpazpro3.app.data

import com.mpazpro3.app.data.remote.ApiResult
import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.Estudiante
import com.mpazpro3.app.model.InformeDocente
import com.mpazpro3.app.model.RecursoDidactico
import com.mpazpro3.app.model.Resultado
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario
import kotlinx.coroutines.flow.Flow

/** Estado de la última sincronización con el servidor (lo muestra el aviso de conexión). */
sealed class EstadoSync {
    object Inactivo : EstadoSync()
    object Cargando : EstadoSync()
    object Ok : EstadoSync()
    data class SinConexion(val mensaje: String) : EstadoSync()
    data class Error(val mensaje: String) : EstadoSync()
}

sealed class ResultadoLogin {
    /**
     * enLinea = false: ingresó sin hablar con el servidor. modoLocal = true: fue a propósito, porque la app
     * está en modo local (el usuario se validó contra la base del dispositivo); modoLocal = false con
     * enLinea = false: ingresó con los datos guardados porque no hubo conexión.
     */
    data class Exito(val usuario: Usuario, val enLinea: Boolean, val modoLocal: Boolean = false) : ResultadoLogin()

    /** El servidor rechazó los datos (campo vacío, usuario o clave incorrectos). */
    data class Rechazado(val mensaje: String, val campo: String?) : ResultadoLogin()

    /** No se pudo ingresar por un problema de conexión o del servidor. */
    data class Falla(val mensaje: String) : ResultadoLogin()
}

/** esCorrecta null = no se registró (mensaje explica por qué). guardadaSinConexion = quedó en la cola de pendientes. */
data class RespuestaDesafio(
    val esCorrecta: Boolean?,
    val mensaje: String? = null,
    val guardadaSinConexion: Boolean = false
)

/**
 * Lo que el ViewModel necesita de una fuente de datos para ESCRIBIR y para sincronizar. Hay dos
 * implementaciones y el ViewModel no sabe cuál usa (lo decide SelectorFuente según el interruptor
 * "Modo local / Modo servidor"):
 *  - RepositorioSincronizado: modo servidor (API como fuente de verdad + copia en Room + cola de pendientes);
 *  - RepositorioLocal: modo local (solo Room, con las mismas funciones puras de domain/).
 */
interface FuenteDatos {
    /** Estado de la última sincronización (en modo local siempre Inactivo). */
    val estado: Flow<EstadoSync>

    /** Acciones guardadas sin conexión que aún no se envían (en modo local siempre 0). */
    val pendientes: Flow<Int>

    /** Mensaje informativo para la pantalla (se muestra hasta cerrarlo). */
    val aviso: Flow<String?>

    fun publicarAviso(texto: String?)
    fun descartarAviso()

    suspend fun iniciarSesion(usuario: String, clave: String): ResultadoLogin
    suspend fun cerrarSesion()
    suspend fun sincronizar(usuario: Usuario): EstadoSync

    suspend fun aprobarContenido(contenidoId: String, observacion: String): Resultado
    suspend fun rechazarContenido(contenidoId: String, observacion: String): Resultado
    suspend fun asignarAlCurso(curso: String, contenidoIds: List<String>, maxIntentos: Int): Resultado

    suspend fun confirmarMaterialRevisado(estudianteId: String, contenidoId: String): Resultado
    suspend fun responderDesafio(estudianteId: String, actividadId: String, alternativa: Int): RespuestaDesafio
    suspend fun enviarEvaluacion(estudianteId: String, contenidoId: String, respuestas: Map<String, Int>): Resultado

    suspend fun descargarResultadosCsv(curso: String): ApiResult<String>
    suspend fun generarInforme(estudianteId: String): Resultado
    suspend fun validarInforme(informeId: String): Resultado
}

/** Lecturas observables (Flow) de una base Room: las pantallas dibujan siempre lo que hay aquí. */
interface LecturasObservables {
    fun contenidos(): Flow<List<Contenido>>
    fun recursos(): Flow<List<RecursoDidactico>>
    fun actividades(): Flow<List<Actividad>>
    fun asignaciones(): Flow<List<Asignacion>>
    fun estudiantes(): Flow<List<Estudiante>>
    fun informes(): Flow<List<InformeDocente>>
    fun seguimientos(): Flow<List<SeguimientoAvance>>
}
