package com.mpazpro3.app.domain

import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.EstadoAvance
import com.mpazpro3.app.model.RespuestaEstudiante
import com.mpazpro3.app.model.SeguimientoAvance

/**
 * Reglas de progreso y de respuestas vigentes (RF-06, RF-10, RF-11) como funciones PURAS.
 *
 * Fórmula del avance de un contenido (queda documentada también en el README):
 *
 *   avance = (material revisado + desafíos distintos respondidos + evaluación rendida)
 *            / (1 + cantidad de desafíos + 1)
 *
 * Responder de nuevo un desafío NO suma otra vez; el valor se limita entre 0.0 y 1.0.
 * El avance general de un estudiante es el promedio del avance de los contenidos asignados a su curso.
 */
object ReglasProgreso {

    /**
     * Respuestas vigentes de un estudiante en un contenido:
     *  - de cada desafío, la última respuesta (el mayor numeroIntento);
     *  - de la evaluación final, las respuestas del ÚLTIMO intento rendido.
     * Salen primero los desafíos (por dificultad y orden) y luego la evaluación (por orden).
     */
    fun respuestasVigentes(
        respuestas: List<RespuestaEstudiante>,
        actividades: List<Actividad>
    ): List<RespuestaEstudiante> {
        val porId = actividades.associateBy { it.id }

        val desafios = respuestas
            .filter { porId[it.actividadId]?.esEvaluacionFinal == false }
            .groupBy { it.actividadId }
            .map { (_, intentos) -> intentos.maxByOrNull { it.numeroIntento }!! }
            .sortedWith(
                compareBy<RespuestaEstudiante>(
                    { porId.getValue(it.actividadId).dificultad.ordinal },
                    { porId.getValue(it.actividadId).orden }
                )
            )

        val deEvaluacion = respuestas.filter { porId[it.actividadId]?.esEvaluacionFinal == true }
        val ultimoIntento = deEvaluacion.maxOfOrNull { it.numeroIntento }
        val evaluacion = deEvaluacion
            .filter { it.numeroIntento == ultimoIntento }
            .groupBy { it.actividadId }
            .map { (_, repetidas) -> repetidas.last() }
            .sortedBy { porId.getValue(it.actividadId).orden }

        return desafios + evaluacion
    }

    /** Avance (0.0 a 1.0) del estudiante en un contenido. */
    fun progresoContenido(seguimiento: SeguimientoAvance?, actividadesDelContenido: List<Actividad>): Float {
        if (seguimiento == null) return 0f
        val desafios = actividadesDelContenido.filter { !it.esEvaluacionFinal }
        val hayEvaluacion = actividadesDelContenido.any { it.esEvaluacionFinal }

        val pasosTotales = 1 + desafios.size + (if (hayEvaluacion) 1 else 0)
        val idsDesafios = desafios.map { it.id }.toSet()
        val desafiosRespondidos = seguimiento.respuestas
            .map { it.actividadId }
            .filter { it in idsDesafios }
            .toSet()
            .size
        val material = if (seguimiento.materialRevisado) 1 else 0
        val evaluacion = if (hayEvaluacion && seguimiento.intentosEvaluacionUsados > 0) 1 else 0

        return ((material + desafiosRespondidos + evaluacion).toFloat() / pasosTotales).coerceIn(0f, 1f)
    }

    /** Avance general: promedio de los avances por contenido (0 si no hay contenidos). */
    fun progresoGeneral(progresosPorContenido: List<Float>): Float =
        if (progresosPorContenido.isEmpty()) 0f else progresosPorContenido.average().toFloat().coerceIn(0f, 1f)

    /** Al trabajar un contenido por primera vez el estado pasa de NO_INICIADO a EN_PROGRESO; los demás no cambian. */
    fun estadoAlTrabajar(estado: EstadoAvance): EstadoAvance =
        if (estado == EstadoAvance.NO_INICIADO) EstadoAvance.EN_PROGRESO else estado

    /** Porcentaje entero (0 a 100) para mostrar el avance. */
    fun porcentaje(avance: Float): Int = (avance.coerceIn(0f, 1f) * 100f + 0.5f).toInt()

    /** Cantidad de respuestas correctas entre las vigentes. */
    fun contarCorrectas(vigentes: List<RespuestaEstudiante>): Int = vigentes.count { it.esCorrecta }
}
