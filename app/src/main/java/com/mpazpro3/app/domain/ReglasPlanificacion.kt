package com.mpazpro3.app.domain

import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.Resultado

/**
 * Reglas de la planificación docente (RF-04, RF-05, RN-02, RN-03) como funciones PURAS.
 */
object ReglasPlanificacion {

    const val MIN_INTENTOS = 1
    const val MAX_INTENTOS = 10

    const val MENSAJE_SIN_CURSO = "Tu usuario no tiene un curso asociado: no se puede asignar."
    const val MENSAJE_SIN_SELECCION = "Selecciona al menos un contenido."
    const val MENSAJE_NO_AUTORIZADO = "Este contenido no está autorizado por UTP y no se puede asignar"

    /** Error del selector de intentos (null = válido). */
    fun errorMaxIntentos(maxIntentos: Int): String? = when {
        maxIntentos < MIN_INTENTOS -> "El máximo de intentos debe ser al menos $MIN_INTENTOS."
        maxIntentos > MAX_INTENTOS -> "El máximo de intentos no puede superar $MAX_INTENTOS."
        else -> null
    }

    /**
     * Valida un lote de asignación. Es todo o nada: si un solo contenido no está autorizado,
     * no se asigna ninguno (RN-02).
     */
    fun validarAsignacion(
        curso: String,
        contenidoIds: List<String>,
        maxIntentos: Int,
        contenidos: List<Contenido>
    ): Resultado {
        if (curso.isBlank()) return Resultado.error(MENSAJE_SIN_CURSO)
        if (contenidoIds.isEmpty()) return Resultado.error(MENSAJE_SIN_SELECCION)
        errorMaxIntentos(maxIntentos)?.let { return Resultado.error(it) }

        for (id in contenidoIds.distinct()) {
            val contenido = contenidos.find { it.id == id }
                ?: return Resultado.error("El contenido $id no existe.")
            if (contenido.estado != EstadoAprobacion.APROBADO) {
                return Resultado.error("$MENSAJE_NO_AUTORIZADO (${contenido.unidad}).")
            }
        }
        return Resultado.ok("Asignación guardada")
    }

    /** Identificador estable de la asignación de un contenido a un curso. */
    fun idAsignacion(curso: String, contenidoId: String): String =
        "AS-" + curso.filter { it.isLetterOrDigit() } + "-" + contenidoId
}
