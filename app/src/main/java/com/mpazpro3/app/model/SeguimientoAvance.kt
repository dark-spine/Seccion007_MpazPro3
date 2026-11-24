package com.mpazpro3.app.model

/**
 * Respuesta de un estudiante a una actividad.
 *
 * numeroIntento:
 *  - en un desafío es el número de veces que el estudiante respondió ese mismo desafío (1, 2, 3…);
 *  - en la evaluación final es el número del intento de la evaluación (1 hasta el máximo del docente).
 */
data class RespuestaEstudiante(
    val actividadId: String,
    val alternativaElegida: Int,
    val esCorrecta: Boolean,
    val numeroIntento: Int,
    val fecha: Long = 0L
)

enum class EstadoAvance(val etiqueta: String) {
    NO_INICIADO("No iniciado"),
    EN_PROGRESO("En progreso"),
    FINALIZADO("Finalizado")
}

/**
 * Seguimiento de avance de un estudiante en un contenido (entidad del caso): material revisado,
 * respuestas, intentos usados en la evaluación final y estado de avance.
 */
data class SeguimientoAvance(
    val estudianteId: String,
    val contenidoId: String,
    val respuestas: List<RespuestaEstudiante> = emptyList(),
    val intentosEvaluacionUsados: Int = 0,
    val estado: EstadoAvance = EstadoAvance.NO_INICIADO,
    val materialRevisado: Boolean = false
)
