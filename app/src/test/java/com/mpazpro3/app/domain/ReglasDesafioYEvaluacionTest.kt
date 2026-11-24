package com.mpazpro3.app.domain

import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Alternativa
import com.mpazpro3.app.model.Dificultad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasDesafioYEvaluacionTest {

    private fun actividad(id: String, dificultad: Dificultad, orden: Int, esFinal: Boolean = false, contenido: String = "C") =
        Actividad(
            id = id,
            contenidoId = contenido,
            enunciado = "Pregunta $id",
            alternativas = listOf(Alternativa("a", true), Alternativa("b", false)),
            dificultad = dificultad,
            orden = orden,
            esEvaluacionFinal = esFinal
        )

    private val dificil = actividad("D3", Dificultad.DIFICIL, 3)
    private val facil = actividad("D1", Dificultad.FACIL, 1)
    private val medio = actividad("D2", Dificultad.MEDIO, 2)
    private val final2 = actividad("E2", Dificultad.MEDIO, 2, esFinal = true)
    private val final1 = actividad("E1", Dificultad.MEDIO, 1, esFinal = true)
    private val ajena = actividad("X", Dificultad.FACIL, 1, contenido = "OTRO")
    private val todas = listOf(dificil, final2, facil, ajena, medio, final1)

    @Test
    fun desafios_vanDeFacilADificilYSinLaEvaluacion() {
        assertEquals(listOf("D1", "D2", "D3"), ReglasDesafio.desafiosDe(todas, "C").map { it.id })
    }

    @Test
    fun evaluacionFinal_soloSusPreguntasEnOrden() {
        assertEquals(listOf("E1", "E2"), ReglasDesafio.evaluacionFinalDe(todas, "C").map { it.id })
    }

    @Test
    fun contenidoNuloNoTieneActividades() {
        assertTrue(ReglasDesafio.desafiosDe(todas, null).isEmpty())
        assertTrue(ReglasDesafio.evaluacionFinalDe(todas, null).isEmpty())
    }

    @Test
    fun respuestaCorrecta_incorrectaOInexistente() {
        assertEquals(true, ReglasDesafio.esRespuestaCorrecta(facil, 0))
        assertEquals(false, ReglasDesafio.esRespuestaCorrecta(facil, 1))
        assertNull(ReglasDesafio.esRespuestaCorrecta(facil, 7))
        assertNull(ReglasDesafio.esRespuestaCorrecta(facil, -1))
    }

    @Test
    fun siguienteNumeroIntento_sumaUno() {
        assertEquals(1, ReglasDesafio.siguienteNumeroIntento(0))
        assertEquals(4, ReglasDesafio.siguienteNumeroIntento(3))
    }

    // ---- RN-03: intentos de la evaluación final ----

    private val preguntas = listOf(final1, final2)

    @Test
    fun intentosRestantes_maximoMenosUsadosNuncaNegativo() {
        assertEquals(2, ReglasEvaluacion.intentosRestantes(3, 1))
        assertEquals(0, ReglasEvaluacion.intentosRestantes(2, 2))
        assertEquals(0, ReglasEvaluacion.intentosRestantes(2, 5))
        assertNull(ReglasEvaluacion.intentosRestantes(null, 0))
    }

    @Test
    fun envio_sinIntentosSeRechaza() {
        val resultado = ReglasEvaluacion.validarEnvio(preguntas, mapOf("E1" to 0, "E2" to 0), 0)
        assertFalse(resultado.exito)
        assertEquals(ReglasEvaluacion.MENSAJE_SIN_INTENTOS, resultado.mensaje)
    }

    @Test
    fun envio_contenidoNoAsignado() {
        assertEquals(
            ReglasEvaluacion.MENSAJE_NO_ASIGNADO,
            ReglasEvaluacion.validarEnvio(preguntas, mapOf("E1" to 0, "E2" to 0), null).mensaje
        )
    }

    @Test
    fun envio_incompletoSeRechaza() {
        val resultado = ReglasEvaluacion.validarEnvio(preguntas, mapOf("E1" to 0), 2)
        assertFalse(resultado.exito)
        assertEquals(ReglasEvaluacion.MENSAJE_INCOMPLETA, resultado.mensaje)
    }

    @Test
    fun envio_alternativaInexistenteSeRechaza() {
        val resultado = ReglasEvaluacion.validarEnvio(preguntas, mapOf("E1" to 0, "E2" to 9), 2)
        assertFalse(resultado.exito)
        assertEquals(ReglasEvaluacion.MENSAJE_RESPUESTA_INVALIDA, resultado.mensaje)
    }

    @Test
    fun envio_sinPreguntas() {
        assertEquals(
            ReglasEvaluacion.MENSAJE_SIN_PREGUNTAS,
            ReglasEvaluacion.validarEnvio(emptyList(), emptyMap(), 2).mensaje
        )
    }

    @Test
    fun envio_completoYConIntentosEsValido() {
        assertTrue(ReglasEvaluacion.validarEnvio(preguntas, mapOf("E1" to 0, "E2" to 1), 1).exito)
    }
}
