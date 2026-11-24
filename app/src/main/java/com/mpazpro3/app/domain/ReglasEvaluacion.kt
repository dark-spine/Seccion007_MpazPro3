package com.mpazpro3.app.domain

import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Resultado

/**
 * Reglas de la evaluación final (RF-09, RN-03) como funciones PURAS.
 */
object ReglasEvaluacion {

    const val MENSAJE_SIN_INTENTOS = "Ya usaste todos tus intentos de esta evaluación."
    const val MENSAJE_SIN_PREGUNTAS = "Esta evaluación todavía no tiene preguntas."
    const val MENSAJE_INCOMPLETA = "Responde todas las preguntas antes de enviar."
    const val MENSAJE_RESPUESTA_INVALIDA = "Hay una respuesta que no es válida. Revísala e inténtalo de nuevo."
    const val MENSAJE_NO_ASIGNADO = "Este contenido no está asignado a tu curso."

    /**
     * Intentos que le quedan al estudiante: máximo definido por el docente menos los usados,
     * nunca negativo. null = el contenido no está asignado a su curso.
     */
    fun intentosRestantes(maxIntentos: Int?, usados: Int): Int? =
        maxIntentos?.let { (it - usados).coerceAtLeast(0) }

    /**
     * Valida el envío de la evaluación ANTES de registrar nada (no se confía solo en que la
     * pantalla deshabilite el botón).
     *
     * @param respuestas mapa idActividad -> índice de la alternativa elegida.
     */
    fun validarEnvio(preguntas: List<Actividad>, respuestas: Map<String, Int>, restantes: Int?): Resultado {
        if (restantes == null) return Resultado.error(MENSAJE_NO_ASIGNADO)
        if (restantes <= 0) return Resultado.error(MENSAJE_SIN_INTENTOS)
        if (preguntas.isEmpty()) return Resultado.error(MENSAJE_SIN_PREGUNTAS)
        if (preguntas.any { it.id !in respuestas }) return Resultado.error(MENSAJE_INCOMPLETA)
        val todasValidas = preguntas.all { pregunta ->
            ReglasDesafio.esRespuestaCorrecta(pregunta, respuestas.getValue(pregunta.id)) != null
        }
        return if (todasValidas) Resultado.ok("Evaluación enviada") else Resultado.error(MENSAJE_RESPUESTA_INVALIDA)
    }
}
