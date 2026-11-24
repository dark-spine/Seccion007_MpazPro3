package com.mpazpro3.app.data

import androidx.room.withTransaction
import com.mpazpro3.app.data.local.AppDatabase
import com.mpazpro3.app.data.local.AsignacionEntity
import com.mpazpro3.app.data.local.RespuestaEntity
import com.mpazpro3.app.data.local.SeguimientoEntity
import com.mpazpro3.app.data.local.alternativasAEntidad
import com.mpazpro3.app.data.local.aDominio
import com.mpazpro3.app.data.local.aEntidad
import com.mpazpro3.app.domain.GeneradorInforme
import com.mpazpro3.app.domain.ReglasAcceso
import com.mpazpro3.app.domain.ReglasContenido
import com.mpazpro3.app.domain.ReglasDesafio
import com.mpazpro3.app.domain.ReglasEvaluacion
import com.mpazpro3.app.domain.ReglasPlanificacion
import com.mpazpro3.app.domain.ReglasProgreso
import com.mpazpro3.app.domain.ResumenContenido
import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.EstadoAvance
import com.mpazpro3.app.model.EstadoValidacion
import com.mpazpro3.app.model.Estudiante
import com.mpazpro3.app.model.InformeDocente
import com.mpazpro3.app.model.RecursoDidactico
import com.mpazpro3.app.model.Resultado
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lecturas observables (Flow) de una base Room, carga inicial de datos de ejemplo y las operaciones
 * atómicas de referencia con las que se construyó el modo local.
 *
 * Flujo de datos de la app: Pantalla -> ViewModel -> SelectorFuente -> (modo local) RepositorioLocal -> Room local
 *                                                                    -> (modo servidor) RepositorioSincronizado -> API + Room
 * Hay una instancia de esta clase por base Room (la local y la copia del servidor); las escrituras del modo
 * local las hace hoy RepositorioLocal con las mismas funciones puras. Las operaciones de abajo (aprobarContenido,
 * confirmarMaterialRevisado, responderDesafio…) se conservan como referencia de las reglas que deben cumplir
 * CacheRoom.marcarMaterialLocal y registrarRespuestaLocal (TODO E3-8c/d).
 *
 * Aquí viven las operaciones que deben ser atómicas: cada regla de negocio que lee y luego escribe
 * (máximo de intentos, una unidad aprobada por asignatura, informe validado) se ejecuta dentro de
 * una transacción de Room, de modo que dos acciones seguidas no puedan saltarse la regla. La
 * decisión en sí la toman las funciones puras de domain/.
 */
class MpazProRepositorio(private val db: AppDatabase) : LecturasObservables {

    // =====================================================================
    // Lecturas observables (Flow): se vuelven a emitir cuando cambia la tabla
    // =====================================================================

    override fun contenidos(): Flow<List<Contenido>> =
        db.contenidoDao().observarTodos().map { lista -> lista.map { it.aDominio() } }

    override fun recursos(): Flow<List<RecursoDidactico>> =
        db.recursoDao().observarTodos().map { lista -> lista.map { it.aDominio() } }

    override fun actividades(): Flow<List<Actividad>> =
        db.actividadDao().observarTodas().map { lista -> lista.map { it.aDominio() } }

    override fun asignaciones(): Flow<List<Asignacion>> =
        db.asignacionDao().observarTodas().map { lista -> lista.map { it.aDominio() } }

    override fun estudiantes(): Flow<List<Estudiante>> =
        db.estudianteDao().observarTodos().map { lista -> lista.map { it.aDominio() } }

    override fun informes(): Flow<List<InformeDocente>> =
        db.informeDao().observarTodos().map { lista -> lista.map { it.aDominio() } }

    /** Seguimientos con sus respuestas ya unidas (las respuestas viven en su propia tabla). */
    override fun seguimientos(): Flow<List<SeguimientoAvance>> =
        combine(db.seguimientoDao().observarTodos(), db.respuestaDao().observarTodas()) { seguimientos, respuestas ->
            val porClave = respuestas.groupBy { it.estudianteId to it.contenidoId }
            seguimientos.map { seguimiento ->
                val propias = porClave[seguimiento.estudianteId to seguimiento.contenidoId].orEmpty()
                seguimiento.aDominio(propias.map { it.aDominio() })
            }
        }

    // =====================================================================
    // Acceso (RF-00)
    // =====================================================================

    /** Devuelve el usuario si el usuario y la clave coinciden; null si no. */
    suspend fun autenticar(usuario: String, clave: String): Usuario? =
        db.usuarioDao().buscarPorCredenciales(
            ReglasAcceso.normalizarUsuario(usuario),
            ReglasAcceso.hashClave(clave)
        )?.aDominio()

    // =====================================================================
    // Autorización de contenidos (RF-01, RF-02, RN-02, RN-04)
    // =====================================================================

    suspend fun aprobarContenido(contenidoId: String, observacion: String): Resultado =
        db.withTransaction {
            val todos = db.contenidoDao().todos().map { it.aDominio() }
            val validacion = ReglasContenido.validarAprobacion(todos.find { it.id == contenidoId }, todos)
            if (validacion.exito) {
                db.contenidoDao().actualizarEstado(contenidoId, EstadoAprobacion.APROBADO.name, observacion)
            }
            validacion
        }

    suspend fun rechazarContenido(contenidoId: String, observacion: String): Resultado =
        db.withTransaction {
            val contenido = db.contenidoDao().porId(contenidoId)?.aDominio()
            val asignaciones = db.asignacionDao().todas().map { it.aDominio() }
            val validacion = ReglasContenido.validarRechazo(contenido, observacion, asignaciones)
            if (validacion.exito) {
                db.contenidoDao().actualizarEstado(contenidoId, EstadoAprobacion.RECHAZADO.name, observacion)
            }
            validacion
        }

    // =====================================================================
    // Planificación docente (RF-04, RF-05, RN-02, RN-03)
    // =====================================================================

    /**
     * Asigna los contenidos al curso con el máximo de intentos. Si un contenido ya estaba
     * asignado al curso, actualiza el máximo en vez de duplicar la asignación. Es todo o nada.
     */
    suspend fun asignarAlCurso(curso: String, contenidoIds: List<String>, maxIntentos: Int): Resultado =
        db.withTransaction {
            val contenidos = db.contenidoDao().todos().map { it.aDominio() }
            val validacion = ReglasPlanificacion.validarAsignacion(curso, contenidoIds, maxIntentos, contenidos)
            if (!validacion.exito) {
                validacion
            } else {
                val ids = contenidoIds.distinct()
                val filas = ids.map { contenidoId ->
                    val existente = db.asignacionDao().buscar(curso, contenidoId)
                    AsignacionEntity(
                        id = existente?.id ?: ReglasPlanificacion.idAsignacion(curso, contenidoId),
                        curso = curso,
                        contenidoId = contenidoId,
                        maxIntentosEvaluacion = maxIntentos
                    )
                }
                db.asignacionDao().insertarTodas(filas)
                Resultado.ok("Asignación guardada: ${ids.size} contenido(s) con máximo de $maxIntentos intento(s).")
            }
        }

    // =====================================================================
    // Experiencia del estudiante (RF-07 a RF-11, RN-03)
    // =====================================================================

    /**
     * Asignación del contenido al curso del estudiante, solo si el contenido sigue APROBADO por
     * UTP (RN-02). null = el estudiante no puede trabajar ese contenido.
     */
    private suspend fun asignacionVigente(estudianteId: String, contenidoId: String): AsignacionEntity? {
        val estudiante = db.estudianteDao().porId(estudianteId) ?: return null
        val contenido = db.contenidoDao().porId(contenidoId) ?: return null
        if (contenido.estado != EstadoAprobacion.APROBADO.name) return null
        return db.asignacionDao().buscar(estudiante.curso, contenidoId)
    }

    private suspend fun seguimientoActual(estudianteId: String, contenidoId: String): SeguimientoEntity =
        db.seguimientoDao().buscar(estudianteId, contenidoId)
            ?: SeguimientoEntity(
                estudianteId = estudianteId,
                contenidoId = contenidoId,
                materialRevisado = false,
                intentosEvaluacionUsados = 0,
                estado = EstadoAvance.NO_INICIADO.name
            )

    /** Crea o actualiza el seguimiento: el material queda revisado y el estado, EN_PROGRESO. */
    suspend fun confirmarMaterialRevisado(estudianteId: String, contenidoId: String): Resultado =
        db.withTransaction {
            if (asignacionVigente(estudianteId, contenidoId) == null) {
                Resultado.error(ReglasEvaluacion.MENSAJE_NO_ASIGNADO)
            } else {
                val actual = seguimientoActual(estudianteId, contenidoId)
                val estado = if (actual.estado == EstadoAvance.NO_INICIADO.name) EstadoAvance.EN_PROGRESO.name else actual.estado
                db.seguimientoDao().guardar(actual.copy(materialRevisado = true, estado = estado))
                Resultado.ok("Material revisado")
            }
        }

    /**
     * Registra la respuesta a un desafío (cada intento queda guardado con numeroIntento + 1) y
     * devuelve si fue correcta. null = la actividad o la alternativa no son válidas, o el
     * contenido no está asignado al estudiante: en ese caso no se registra nada.
     */
    suspend fun responderDesafio(estudianteId: String, actividadId: String, alternativa: Int): Boolean? =
        db.withTransaction {
            val actividad = db.actividadDao().porId(actividadId)?.aDominio()
            val correcta = actividad?.let { ReglasDesafio.esRespuestaCorrecta(it, alternativa) }
            if (actividad == null || actividad.esEvaluacionFinal || correcta == null ||
                asignacionVigente(estudianteId, actividad.contenidoId) == null
            ) {
                null
            } else {
                val previos = db.respuestaDao().ultimoIntento(estudianteId, actividadId)
                db.respuestaDao().insertar(
                    RespuestaEntity(
                        estudianteId = estudianteId,
                        actividadId = actividadId,
                        contenidoId = actividad.contenidoId,
                        alternativaElegida = alternativa,
                        esCorrecta = correcta,
                        numeroIntento = ReglasDesafio.siguienteNumeroIntento(previos),
                        fecha = System.currentTimeMillis()
                    )
                )
                val actual = seguimientoActual(estudianteId, actividad.contenidoId)
                val estado = if (actual.estado == EstadoAvance.NO_INICIADO.name) EstadoAvance.EN_PROGRESO.name else actual.estado
                db.seguimientoDao().guardar(actual.copy(estado = estado))
                correcta
            }
        }

    /**
     * RN-03: lee el máximo y los intentos usados e inserta el intento dentro de la MISMA
     * transacción, así dos envíos seguidos no pueden superar el límite.
     */
    suspend fun enviarEvaluacion(estudianteId: String, contenidoId: String, respuestas: Map<String, Int>): Resultado =
        db.withTransaction {
            val asignacion = asignacionVigente(estudianteId, contenidoId)
            val seguimiento = seguimientoActual(estudianteId, contenidoId)
            val actividades = db.actividadDao().deContenido(contenidoId).map { it.aDominio() }
            val preguntas = ReglasDesafio.evaluacionFinalDe(actividades, contenidoId)
            val restantes = ReglasEvaluacion.intentosRestantes(
                asignacion?.maxIntentosEvaluacion, seguimiento.intentosEvaluacionUsados
            )

            val validacion = ReglasEvaluacion.validarEnvio(preguntas, respuestas, restantes)
            if (!validacion.exito) {
                validacion
            } else {
                val intento = seguimiento.intentosEvaluacionUsados + 1
                val ahora = System.currentTimeMillis()
                val filas = preguntas.map { pregunta ->
                    val elegida = respuestas.getValue(pregunta.id)
                    RespuestaEntity(
                        estudianteId = estudianteId,
                        actividadId = pregunta.id,
                        contenidoId = contenidoId,
                        alternativaElegida = elegida,
                        esCorrecta = ReglasDesafio.esRespuestaCorrecta(pregunta, elegida) == true,
                        numeroIntento = intento,
                        fecha = ahora
                    )
                }
                db.respuestaDao().insertarTodas(filas)
                db.seguimientoDao().guardar(
                    seguimiento.copy(
                        intentosEvaluacionUsados = intento,
                        estado = EstadoAvance.FINALIZADO.name
                    )
                )
                val correctas = filas.count { it.esCorrecta }
                val quedan = (restantes ?: 1) - 1
                Resultado.ok(
                    "¡Terminaste la evaluación! Respondiste bien $correctas de ${filas.size}. " +
                        "Intentos restantes: $quedan."
                )
            }
        }

    // =====================================================================
    // Informe pedagógico (RF-12, RF-13, RN-05, RN-06)
    // =====================================================================

    /** Genera (o regenera) el informe en BORRADOR. Un informe VALIDADO no se sobrescribe. */
    suspend fun generarInforme(estudianteId: String): Resultado =
        db.withTransaction {
            val estudiante = db.estudianteDao().porId(estudianteId)
            val existente = db.informeDao().deEstudiante(estudianteId)

            if (estudiante == null) {
                Resultado.error("No se encontró al estudiante.")
            } else if (existente?.estado == EstadoValidacion.VALIDADO.name) {
                Resultado.error(MENSAJE_INFORME_YA_VALIDADO)
            } else {
                val contenidos = db.contenidoDao().todos().map { it.aDominio() }.associateBy { it.id }
                val respuestas = db.respuestaDao().deEstudiante(estudianteId)
                val resumenes = db.asignacionDao().delCurso(estudiante.curso)
                    .mapNotNull { contenidos[it.contenidoId] }
                    .filter { it.estado == EstadoAprobacion.APROBADO }
                    .sortedBy { it.asignatura.ordinal }
                    .map { contenido ->
                        val actividades = db.actividadDao().deContenido(contenido.id).map { it.aDominio() }
                        val propias = respuestas.filter { it.contenidoId == contenido.id }.map { it.aDominio() }
                        ResumenContenido(contenido, actividades, ReglasProgreso.respuestasVigentes(propias, actividades))
                    }

                val borrador = GeneradorInforme.generar(resumenes)
                if (borrador == null) {
                    Resultado.error(GeneradorInforme.MENSAJE_SIN_DATOS)
                } else {
                    db.informeDao().guardar(
                        InformeDocente(
                            id = "INF-$estudianteId",
                            estudianteId = estudianteId,
                            rendimiento = borrador.rendimiento,
                            fortalezas = borrador.fortalezas,
                            aspectosAReforzar = borrador.aspectosAReforzar,
                            fecha = fechaDeHoy(),
                            estado = EstadoValidacion.BORRADOR
                        ).aEntidad()
                    )
                    Resultado.ok("Informe generado en estado borrador.")
                }
            }
        }

    /** BORRADOR -> VALIDADO. Validar un informe ya validado no cambia nada. */
    suspend fun validarInforme(informeId: String): Resultado =
        db.withTransaction {
            val informe = db.informeDao().porId(informeId)
            when {
                informe == null -> Resultado.error("No se encontró el informe.")
                informe.estado == EstadoValidacion.VALIDADO.name -> Resultado.ok("El informe ya estaba validado.")
                else -> {
                    db.informeDao().validar(informeId)
                    Resultado.ok("Informe validado por el docente.")
                }
            }
        }

    private fun fechaDeHoy(): String =
        SimpleDateFormat("dd-MM-yyyy", Locale.forLanguageTag("es-CL")).format(Date())

    // =====================================================================
    // Carga inicial
    // =====================================================================

    /**
     * Siembra la base con los datos ficticios de DatosIniciales la primera vez que se abre la
     * app. En las ejecuciones siguientes no hace nada: las tablas ya tienen filas.
     *
     * El orden importa: primero las tablas de las que dependen otras por llave foránea.
     */
    suspend fun sembrarSiEstaVacia() {
        if (db.contenidoDao().contar() > 0) return
        db.withTransaction {
            db.estudianteDao().insertarTodos(DatosIniciales.estudiantes.map { it.aEntidad() })
            db.contenidoDao().insertarTodos(DatosIniciales.contenidos.map { it.aEntidad() })
            db.recursoDao().insertarTodos(DatosIniciales.recursos.map { it.aEntidad() })
            db.actividadDao().insertarActividades(DatosIniciales.actividades.map { it.aEntidad() })
            db.actividadDao().insertarAlternativas(DatosIniciales.actividades.flatMap { it.alternativasAEntidad() })
            db.asignacionDao().insertarTodas(DatosIniciales.asignaciones.map { it.aEntidad() })
            if (db.usuarioDao().contar() == 0) {
                db.usuarioDao().insertarTodos(DatosIniciales.usuarios.map { it.aEntidad() })
            }
            db.seguimientoDao().insertarTodos(DatosIniciales.seguimientos.map { it.aEntidad() })
            db.respuestaDao().insertarTodas(
                DatosIniciales.seguimientos.flatMap { seguimiento ->
                    seguimiento.respuestas.map { respuesta ->
                        RespuestaEntity(
                            estudianteId = seguimiento.estudianteId,
                            actividadId = respuesta.actividadId,
                            contenidoId = seguimiento.contenidoId,
                            alternativaElegida = respuesta.alternativaElegida,
                            esCorrecta = respuesta.esCorrecta,
                            numeroIntento = respuesta.numeroIntento,
                            fecha = respuesta.fecha
                        )
                    }
                }
            )
        }
    }

    companion object {
        const val MENSAJE_INFORME_YA_VALIDADO = GeneradorInforme.MENSAJE_YA_VALIDADO
    }
}
