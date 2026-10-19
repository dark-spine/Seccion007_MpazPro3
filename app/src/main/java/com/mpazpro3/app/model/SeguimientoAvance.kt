package com.mpazpro3.app.model

data class RespuestaEstudiante(
    val actividadId: String,
    val alternativaElegida: Int,
    val esCorrecta: Boolean,
    val numeroIntento: Int
)

enum class EstadoAvance(val etiqueta: String) {
    NO_INICIADO("No iniciado"),
    EN_PROGRESO("En progreso"),
    FINALIZADO("Finalizado")
}

/**
 * Seguimiento de avance de un estudiante en un contenido (entidad del caso): respuestas,
 * intentos usados en la evaluación final y estado de avance.
 */
data class SeguimientoAvance(
    val estudianteId: String,
    val contenidoId: String,
    val respuestas: List<RespuestaEstudiante> = emptyList(),
    val intentosEvaluacionUsados: Int = 0,
    val estado: EstadoAvance = EstadoAvance.NO_INICIADO
)
