package com.mpazpro3.app.model

/** Dificultad de un desafío. Escala propuesta (pendiente de confirmar con el CITT). */
enum class Dificultad(val etiqueta: String) {
    FACIL("Fácil"),
    MEDIO("Medio"),
    DIFICIL("Difícil")
}

data class Alternativa(
    val texto: String,
    val esCorrecta: Boolean
)

/**
 * Actividad o pregunta (entidad "Actividad/prueba" del caso). Si esEvaluacionFinal es false es
 * un desafío de práctica; los desafíos se presentan con dificultad progresiva (orden).
 */
data class Actividad(
    val id: String,
    val contenidoId: String,
    val enunciado: String,
    val alternativas: List<Alternativa>,
    val dificultad: Dificultad,
    val orden: Int,
    val esEvaluacionFinal: Boolean = false
)
