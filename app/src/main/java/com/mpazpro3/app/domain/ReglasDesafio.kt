package com.mpazpro3.app.domain

import com.mpazpro3.app.model.Actividad

/**
 * Reglas de los desafíos de práctica (RF-08) como funciones PURAS: reciben datos y devuelven un
 * resultado, sin Room, sin pantallas y sin Android.
 */
object ReglasDesafio {

    /**
     * Desafíos de un contenido, SIN la evaluación final, en dificultad progresiva: primero por
     * dificultad (FACIL, MEDIO, DIFICIL) y, dentro de cada una, por "orden".
     */
    fun desafiosDe(actividades: List<Actividad>, contenidoId: String?): List<Actividad> =
        actividades
            .filter { it.contenidoId == contenidoId && !it.esEvaluacionFinal }
            .sortedWith(compareBy<Actividad>({ it.dificultad.ordinal }, { it.orden }))

    /** Preguntas de la evaluación final de un contenido, ordenadas (RF-09). */
    fun evaluacionFinalDe(actividades: List<Actividad>, contenidoId: String?): List<Actividad> =
        actividades
            .filter { it.contenidoId == contenidoId && it.esEvaluacionFinal }
            .sortedBy { it.orden }

    /**
     * ¿La alternativa elegida es la correcta?
     *  - true: correcta; false: incorrecta;
     *  - null: el índice no existe (por ejemplo 5 cuando hay 3 alternativas): no se debe registrar.
     */
    fun esRespuestaCorrecta(actividad: Actividad, indiceAlternativa: Int): Boolean? =
        actividad.alternativas.getOrNull(indiceAlternativa)?.esCorrecta

    /** Reintentos permitidos: el próximo intento es el número de respuestas previas más uno. */
    fun siguienteNumeroIntento(intentosPrevios: Int): Int = intentosPrevios + 1
}
