package com.mpazpro3.app.model

enum class EstadoValidacion(val etiqueta: String) {
    BORRADOR("Borrador (sin validar)"),
    VALIDADO("Validado por el docente")
}

/**
 * Informe pedagógico de uso docente (entidad del caso): rendimiento, fortalezas, aspectos a
 * reforzar, fecha y estado de validación.
 *
 * Restricciones del caso: el estudiante NO tiene acceso a este informe, y el informe no emite
 * diagnósticos ni aplica consecuencias académicas automáticas.
 */
data class InformeDocente(
    val id: String,
    val estudianteId: String,
    val contenidoId: String,
    val rendimiento: String,
    val fortalezas: List<String>,
    val aspectosAReforzar: List<String>,
    val fecha: String,
    val estado: EstadoValidacion = EstadoValidacion.BORRADOR
)
