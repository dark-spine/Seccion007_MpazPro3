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
import kotlinx.coroutines.flow.combine

/**
 * La ÚNICA fuente de datos que ve el ViewModel. Según la preferencia "modo local" elige, en cada
 * operación, entre:
 *  - modo servidor: [servidor] (API + copia en Room) y la base Room de la copia del servidor;
 *  - modo local: [local] (solo Room, mismas reglas de dominio) y la base Room local.
 *
 * Las dos bases son INDEPENDIENTES: lo que se crea en modo local no se envía al servidor ni se
 * mezcla con lo que descarga el servidor.
 */
class SelectorFuente(
    private val preferencia: PreferenciaModo,
    private val servidor: FuenteDatos,
    private val local: FuenteDatos,
    private val lecturasServidor: LecturasObservables,
    private val lecturasLocal: LecturasObservables
) : FuenteDatos, LecturasObservables {

    /** La fuente que corresponde AHORA (se consulta la preferencia cada vez que se llama). */
    fun actual(): FuenteDatos = if (preferencia.modoLocal) local else servidor

    private fun <T> segunModo(deLocal: Flow<T>, deServidor: Flow<T>): Flow<T> =
        combine(preferencia.modoLocalFlujo, deLocal, deServidor) { esLocal, a, b -> if (esLocal) a else b }

    // ---- estado de la sincronización ----

    override val estado: Flow<EstadoSync> = segunModo(local.estado, servidor.estado)
    override val pendientes: Flow<Int> = segunModo(local.pendientes, servidor.pendientes)
    override val aviso: Flow<String?> = segunModo(local.aviso, servidor.aviso)

    override fun publicarAviso(texto: String?) = actual().publicarAviso(texto)
    override fun descartarAviso() {
        // Se limpian los dos: así no queda un aviso del otro modo esperando para reaparecer.
        local.descartarAviso()
        servidor.descartarAviso()
    }

    // ---- operaciones (cada una consulta el modo en el momento de ejecutarse) ----

    override suspend fun iniciarSesion(usuario: String, clave: String): ResultadoLogin =
        actual().iniciarSesion(usuario, clave)

    override suspend fun cerrarSesion() = actual().cerrarSesion()

    override suspend fun sincronizar(usuario: Usuario): EstadoSync = actual().sincronizar(usuario)

    override suspend fun aprobarContenido(contenidoId: String, observacion: String): Resultado =
        actual().aprobarContenido(contenidoId, observacion)

    override suspend fun rechazarContenido(contenidoId: String, observacion: String): Resultado =
        actual().rechazarContenido(contenidoId, observacion)

    override suspend fun asignarAlCurso(curso: String, contenidoIds: List<String>, maxIntentos: Int): Resultado =
        actual().asignarAlCurso(curso, contenidoIds, maxIntentos)

    override suspend fun confirmarMaterialRevisado(estudianteId: String, contenidoId: String): Resultado =
        actual().confirmarMaterialRevisado(estudianteId, contenidoId)

    override suspend fun responderDesafio(estudianteId: String, actividadId: String, alternativa: Int): RespuestaDesafio =
        actual().responderDesafio(estudianteId, actividadId, alternativa)

    override suspend fun enviarEvaluacion(estudianteId: String, contenidoId: String, respuestas: Map<String, Int>): Resultado =
        actual().enviarEvaluacion(estudianteId, contenidoId, respuestas)

    override suspend fun descargarResultadosCsv(curso: String): ApiResult<String> =
        actual().descargarResultadosCsv(curso)

    override suspend fun generarInforme(estudianteId: String): Resultado = actual().generarInforme(estudianteId)

    override suspend fun validarInforme(informeId: String): Resultado = actual().validarInforme(informeId)

    // ---- lecturas observables: la base que corresponde al modo ----

    override fun contenidos(): Flow<List<Contenido>> = segunModo(lecturasLocal.contenidos(), lecturasServidor.contenidos())
    override fun recursos(): Flow<List<RecursoDidactico>> = segunModo(lecturasLocal.recursos(), lecturasServidor.recursos())
    override fun actividades(): Flow<List<Actividad>> = segunModo(lecturasLocal.actividades(), lecturasServidor.actividades())
    override fun asignaciones(): Flow<List<Asignacion>> = segunModo(lecturasLocal.asignaciones(), lecturasServidor.asignaciones())
    override fun estudiantes(): Flow<List<Estudiante>> = segunModo(lecturasLocal.estudiantes(), lecturasServidor.estudiantes())
    override fun informes(): Flow<List<InformeDocente>> = segunModo(lecturasLocal.informes(), lecturasServidor.informes())
    override fun seguimientos(): Flow<List<SeguimientoAvance>> =
        segunModo(lecturasLocal.seguimientos(), lecturasServidor.seguimientos())
}
