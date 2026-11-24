package com.mpazpro3.app.data

import com.mpazpro3.app.data.remote.ApiResult
import com.mpazpro3.app.domain.ExportadorResultados
import com.mpazpro3.app.domain.GeneradorInforme
import com.mpazpro3.app.domain.ReglasAcceso
import com.mpazpro3.app.domain.ReglasContenido
import com.mpazpro3.app.domain.ReglasDesafio
import com.mpazpro3.app.domain.ReglasEvaluacion
import com.mpazpro3.app.domain.ReglasPlanificacion
import com.mpazpro3.app.domain.ReglasProgreso
import com.mpazpro3.app.domain.ResumenContenido
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.EstadoAvance
import com.mpazpro3.app.model.EstadoValidacion
import com.mpazpro3.app.model.InformeDocente
import com.mpazpro3.app.model.Perfil
import com.mpazpro3.app.model.Resultado
import com.mpazpro3.app.model.RespuestaEstudiante
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * MODO LOCAL: toda la app funciona solo con la base del dispositivo (Room), sin servidor. Es el
 * respaldo para demostrar cuando no hay acceso al servicio.
 *
 * Aplica las MISMAS reglas que el servidor y que la app: cada decisión la toma una función pura de
 * domain/ (ReglasContenido, ReglasPlanificacion, ReglasDesafio, ReglasEvaluacion, ReglasProgreso,
 * GeneradorInforme, ExportadorResultados, ReglasAcceso); aquí solo se leen los datos, se llama a la
 * regla y se guarda el resultado, todo dentro de una transacción (RN-03: leer los intentos y
 * registrar el nuevo no pueden separarse). Los mensajes son los mismos que entrega el servidor.
 *
 * Los datos de este modo son INDEPENDIENTES de los del servidor: no hay sincronización de lo creado
 * aquí cuando se vuelve al modo servidor.
 */
class RepositorioLocal(
    private val almacen: AlmacenLocal,
    private val ahora: () -> Long = { System.currentTimeMillis() },
    private val fechaDeHoy: () -> String = { fechaDeHoyEnChile() }
) : FuenteDatos {

    override val estado: Flow<EstadoSync> = MutableStateFlow<EstadoSync>(EstadoSync.Inactivo).asStateFlow()
    override val pendientes: Flow<Int> = MutableStateFlow(0).asStateFlow()

    private val _aviso = MutableStateFlow<String?>(null)
    override val aviso: Flow<String?> = _aviso.asStateFlow()

    override fun publicarAviso(texto: String?) {
        _aviso.value = texto
    }

    override fun descartarAviso() {
        _aviso.value = null
    }

    /** Usuario que ingresó en este modo: sirve para verificar que el docente trabaja solo con su curso. */
    @Volatile
    private var sesion: Usuario? = null

    // =====================================================================
    // Acceso (RF-00): hash local con la misma sal que usa el servidor
    // =====================================================================

    override suspend fun iniciarSesion(usuario: String, clave: String): ResultadoLogin {
        val encontrado = try {
            almacen.autenticar(ReglasAcceso.normalizarUsuario(usuario), ReglasAcceso.hashClave(clave))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return ResultadoLogin.Falla("No se pudo abrir la base de datos local. Inténtalo de nuevo.")
        }
        return if (encontrado == null) {
            ResultadoLogin.Rechazado(ReglasAcceso.MENSAJE_CREDENCIALES, null)
        } else {
            sesion = encontrado
            ResultadoLogin.Exito(encontrado, enLinea = false, modoLocal = true)
        }
    }

    override suspend fun cerrarSesion() {
        sesion = null
        _aviso.value = null
    }

    /** No hay nada que descargar: la base local ya es la fuente. */
    override suspend fun sincronizar(usuario: Usuario): EstadoSync = EstadoSync.Ok

    /** Borra lo creado en modo local y vuelve a los datos de ejemplo. */
    suspend fun restablecerDatos(): Resultado =
        try {
            almacen.restablecer()
            Resultado.ok("Datos locales restablecidos a los datos de ejemplo.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Resultado.error("No se pudieron restablecer los datos locales.")
        }

    // =====================================================================
    // Autorización de contenidos (RF-01, RN-02, RN-04)
    // =====================================================================

    override suspend fun aprobarContenido(contenidoId: String, observacion: String): Resultado =
        almacen.transaccion {
            val todos = almacen.leer().contenidos
            val contenido = todos.find { it.id == contenidoId }
            val validacion = ReglasContenido.validarAprobacion(contenido, todos)
            if (validacion.exito) {
                almacen.actualizarContenido(contenidoId, EstadoAprobacion.APROBADO, observacion)
            }
            validacion
        }

    override suspend fun rechazarContenido(contenidoId: String, observacion: String): Resultado =
        almacen.transaccion {
            val datos = almacen.leer()
            val validacion = ReglasContenido.validarRechazo(
                datos.contenidos.find { it.id == contenidoId }, observacion, datos.asignaciones
            )
            if (validacion.exito) {
                almacen.actualizarContenido(contenidoId, EstadoAprobacion.RECHAZADO, observacion)
            }
            validacion
        }

    // =====================================================================
    // Planificación docente (RF-04, RF-05, RN-02, RN-03)
    // =====================================================================

    override suspend fun asignarAlCurso(curso: String, contenidoIds: List<String>, maxIntentos: Int): Resultado =
        almacen.transaccion {
            val ajeno = errorSiCursoAjeno(curso)
            if (ajeno != null) return@transaccion ajeno
            val datos = almacen.leer()
            val validacion = ReglasPlanificacion.validarAsignacion(curso, contenidoIds, maxIntentos, datos.contenidos)
            if (!validacion.exito) return@transaccion validacion

            // Todo o nada: la validación revisó cada contenido antes de escribir. Si el contenido ya
            // estaba asignado al curso se actualiza su máximo en vez de duplicar la asignación.
            val ids = contenidoIds.distinct()
            val delCurso = datos.asignaciones.filter { it.curso == curso }
            val resultantes: List<Asignacion> = delCurso.map { existente ->
                if (existente.contenidoId in ids) existente.copy(maxIntentosEvaluacion = maxIntentos) else existente
            } + ids.filter { id -> delCurso.none { it.contenidoId == id } }.map { id ->
                Asignacion(ReglasPlanificacion.idAsignacion(curso, id), curso, id, maxIntentos)
            }
            almacen.reemplazarAsignacionesDelCurso(curso, resultantes)
            Resultado.ok("Asignación guardada: ${ids.size} contenido(s) con máximo de $maxIntentos intento(s).")
        }

    // =====================================================================
    // Experiencia del estudiante (RF-07 a RF-11, RN-02, RN-03)
    // =====================================================================

    /** RN-02: el contenido debe estar asignado al curso del estudiante y seguir APROBADO. */
    private fun asignadoYAprobado(datos: DatosLocales, estudianteId: String, contenidoId: String): Boolean {
        val curso = datos.estudiantes.find { it.id == estudianteId }?.curso
        return ReglasContenido.asignadosAlCurso(datos.contenidos, datos.asignaciones, curso).any { it.id == contenidoId }
    }

    private fun seguimientoDe(datos: DatosLocales, estudianteId: String, contenidoId: String): SeguimientoAvance =
        datos.seguimientos.find { it.estudianteId == estudianteId && it.contenidoId == contenidoId }
            ?: SeguimientoAvance(estudianteId = estudianteId, contenidoId = contenidoId)

    override suspend fun confirmarMaterialRevisado(estudianteId: String, contenidoId: String): Resultado =
        almacen.transaccion {
            val datos = almacen.leer()
            when {
                datos.contenidos.none { it.id == contenidoId } ->
                    Resultado.error(ReglasContenido.MENSAJE_CONTENIDO_NO_ENCONTRADO)
                !asignadoYAprobado(datos, estudianteId, contenidoId) ->
                    Resultado.error(ReglasEvaluacion.MENSAJE_NO_ASIGNADO)
                else -> {
                    val actual = seguimientoDe(datos, estudianteId, contenidoId)
                    almacen.guardarSeguimiento(
                        actual.copy(materialRevisado = true, estado = ReglasProgreso.estadoAlTrabajar(actual.estado))
                    )
                    Resultado.ok("Material revisado")
                }
            }
        }

    override suspend fun responderDesafio(estudianteId: String, actividadId: String, alternativa: Int): RespuestaDesafio =
        almacen.transaccion {
            val datos = almacen.leer()
            val actividad = datos.actividades.find { it.id == actividadId }
            if (actividad == null) return@transaccion RespuestaDesafio(null, MENSAJE_ACTIVIDAD_NO_ENCONTRADA)
            if (actividad.esEvaluacionFinal) return@transaccion RespuestaDesafio(null, MENSAJE_SOLO_EN_EVALUACION)
            val correcta = ReglasDesafio.esRespuestaCorrecta(actividad, alternativa)
                ?: return@transaccion RespuestaDesafio(null, ReglasEvaluacion.MENSAJE_RESPUESTA_INVALIDA)
            if (!asignadoYAprobado(datos, estudianteId, actividad.contenidoId)) {
                return@transaccion RespuestaDesafio(null, ReglasEvaluacion.MENSAJE_NO_ASIGNADO)
            }

            val actual = seguimientoDe(datos, estudianteId, actividad.contenidoId)
            val previos = actual.respuestas.filter { it.actividadId == actividadId }.maxOfOrNull { it.numeroIntento } ?: 0
            val nueva = RespuestaEstudiante(
                actividadId = actividadId,
                alternativaElegida = alternativa,
                esCorrecta = correcta,
                numeroIntento = ReglasDesafio.siguienteNumeroIntento(previos),
                fecha = ahora()
            )
            almacen.guardarSeguimiento(
                actual.copy(respuestas = actual.respuestas + nueva, estado = ReglasProgreso.estadoAlTrabajar(actual.estado))
            )
            RespuestaDesafio(correcta)
        }

    /**
     * RN-03: se leen el máximo y los intentos usados y se registra el intento dentro de la MISMA
     * transacción, así dos envíos seguidos no pueden superar el límite del docente.
     */
    override suspend fun enviarEvaluacion(estudianteId: String, contenidoId: String, respuestas: Map<String, Int>): Resultado =
        almacen.transaccion {
            val datos = almacen.leer()
            if (datos.contenidos.none { it.id == contenidoId }) {
                return@transaccion Resultado.error(ReglasContenido.MENSAJE_CONTENIDO_NO_ENCONTRADO)
            }
            val curso = datos.estudiantes.find { it.id == estudianteId }?.curso
            val maximo = datos.asignaciones
                .find { it.curso == curso && it.contenidoId == contenidoId }
                ?.maxIntentosEvaluacion
                ?.takeIf { asignadoYAprobado(datos, estudianteId, contenidoId) }
            val actual = seguimientoDe(datos, estudianteId, contenidoId)
            val preguntas = ReglasDesafio.evaluacionFinalDe(datos.actividades, contenidoId)
            val restantes = ReglasEvaluacion.intentosRestantes(maximo, actual.intentosEvaluacionUsados)

            val validacion = ReglasEvaluacion.validarEnvio(preguntas, respuestas, restantes)
            if (!validacion.exito) return@transaccion validacion

            val intento = actual.intentosEvaluacionUsados + 1
            val fecha = ahora()
            val nuevas = preguntas.map { pregunta ->
                val elegida = respuestas.getValue(pregunta.id)
                RespuestaEstudiante(
                    actividadId = pregunta.id,
                    alternativaElegida = elegida,
                    esCorrecta = ReglasDesafio.esRespuestaCorrecta(pregunta, elegida) == true,
                    numeroIntento = intento,
                    fecha = fecha
                )
            }
            almacen.guardarSeguimiento(
                actual.copy(
                    respuestas = actual.respuestas + nuevas,
                    intentosEvaluacionUsados = intento,
                    estado = EstadoAvance.FINALIZADO
                )
            )
            val correctas = nuevas.count { it.esCorrecta }
            val quedan = (restantes ?: 1) - 1
            Resultado.ok(
                "¡Terminaste la evaluación! Respondiste bien $correctas de ${nuevas.size}. Intentos restantes: $quedan."
            )
        }

    // =====================================================================
    // Resultados (RF-06) e informe pedagógico (RF-12, RF-13, RN-05, RN-06)
    // =====================================================================

    override suspend fun descargarResultadosCsv(curso: String): ApiResult<String> {
        errorSiCursoAjeno(curso)?.let { return ApiResult.Error(403, it.mensaje) }
        val datos = almacen.leer()
        val delCurso = datos.estudiantes.filter { it.curso == curso }
        if (delCurso.isEmpty()) return ApiResult.Error(404, "No hay resultados para exportar.")
        val filas = ExportadorResultados.construirFilas(
            curso = curso,
            estudiantes = delCurso,
            contenidosAsignados = ReglasContenido.asignadosAlCurso(datos.contenidos, datos.asignaciones, curso),
            asignaciones = datos.asignaciones,
            actividades = datos.actividades,
            seguimientos = datos.seguimientos
        )
        return ApiResult.Exito(ExportadorResultados.generarCsv(filas))
    }

    override suspend fun generarInforme(estudianteId: String): Resultado =
        almacen.transaccion {
            val datos = almacen.leer()
            val estudiante = datos.estudiantes.find { it.id == estudianteId }
                ?: return@transaccion Resultado.error(MENSAJE_ESTUDIANTE_NO_ENCONTRADO)
            errorSiCursoAjeno(estudiante.curso, MENSAJE_ESTUDIANTE_AJENO)?.let { return@transaccion it }
            val existente = datos.informes.find { it.estudianteId == estudianteId }
            if (existente?.estado == EstadoValidacion.VALIDADO) {
                return@transaccion Resultado.error(GeneradorInforme.MENSAJE_YA_VALIDADO)
            }

            val resumenes = ReglasContenido.asignadosAlCurso(datos.contenidos, datos.asignaciones, estudiante.curso)
                .map { contenido ->
                    val actividades = datos.actividades.filter { it.contenidoId == contenido.id }
                    val propias = datos.seguimientos
                        .find { it.estudianteId == estudianteId && it.contenidoId == contenido.id }
                        ?.respuestas.orEmpty()
                    ResumenContenido(contenido, actividades, ReglasProgreso.respuestasVigentes(propias, actividades))
                }
            val borrador = GeneradorInforme.generar(resumenes)
                ?: return@transaccion Resultado.error(GeneradorInforme.MENSAJE_SIN_DATOS)

            almacen.guardarInforme(
                InformeDocente(
                    id = GeneradorInforme.idInforme(estudianteId),
                    estudianteId = estudianteId,
                    rendimiento = borrador.rendimiento,
                    fortalezas = borrador.fortalezas,
                    aspectosAReforzar = borrador.aspectosAReforzar,
                    fecha = fechaDeHoy(),
                    estado = EstadoValidacion.BORRADOR
                )
            )
            Resultado.ok("Informe generado en estado borrador.")
        }

    override suspend fun validarInforme(informeId: String): Resultado =
        almacen.transaccion {
            val datos = almacen.leer()
            val informe = datos.informes.find { it.id == informeId }
                ?: return@transaccion Resultado.error("No se encontró el informe.")
            val estudiante = datos.estudiantes.find { it.id == informe.estudianteId }
            if (estudiante != null) {
                errorSiCursoAjeno(estudiante.curso, MENSAJE_ESTUDIANTE_AJENO)?.let { return@transaccion it }
            }
            if (informe.estado == EstadoValidacion.VALIDADO) {
                Resultado.ok("El informe ya estaba validado.")
            } else {
                almacen.guardarInforme(informe.copy(estado = EstadoValidacion.VALIDADO))
                Resultado.ok("Informe validado por el docente.")
            }
        }

    /** El docente solo trabaja con su propio curso (como exige el servidor). null = permitido. */
    private fun errorSiCursoAjeno(curso: String, mensaje: String = MENSAJE_CURSO_AJENO): Resultado? {
        val usuario = sesion
        return if (usuario?.perfil == Perfil.DOCENTE && usuario.curso != curso) Resultado.error(mensaje) else null
    }

    companion object {
        const val MENSAJE_ACTIVIDAD_NO_ENCONTRADA = "No se encontró la actividad."
        const val MENSAJE_SOLO_EN_EVALUACION = "Las preguntas de la evaluación final se envían juntas con la evaluación."
        const val MENSAJE_ESTUDIANTE_NO_ENCONTRADO = "No se encontró al estudiante."
        const val MENSAJE_ESTUDIANTE_AJENO = "Ese estudiante no pertenece a tu curso."
        const val MENSAJE_CURSO_AJENO = "Solo puedes trabajar con tu propio curso."

        /** Fecha en el mismo formato que usa el servidor para los informes (dd-MM-aaaa). */
        fun fechaDeHoyEnChile(): String =
            SimpleDateFormat("dd-MM-yyyy", Locale.forLanguageTag("es-CL")).format(Date())
    }
}
