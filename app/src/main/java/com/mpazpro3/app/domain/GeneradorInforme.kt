package com.mpazpro3.app.domain

import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.RespuestaEstudiante

/** Datos de un contenido asignado, con las respuestas vigentes del estudiante en él. */
data class ResumenContenido(
    val contenido: Contenido,
    val actividades: List<Actividad>,
    val vigentes: List<RespuestaEstudiante>
)

/** Texto del informe antes de guardarlo (siempre nace en BORRADOR). */
data class BorradorInforme(
    val rendimiento: String,
    val fortalezas: List<String>,
    val aspectosAReforzar: List<String>
)

/**
 * Genera el texto del informe pedagógico (RF-12, RF-14, RN-06) con reglas simples, sin IA.
 *
 * Solo DESCRIBE el desempeño: no etiqueta al estudiante, no calcula notas y no sugiere
 * decisiones académicas. Es una función PURA, probada en GeneradorInformeTest.
 */
object GeneradorInforme {

    const val MENSAJE_SIN_DATOS = "No hay datos suficientes para generar el informe."
    const val MENSAJE_YA_VALIDADO = "Este informe ya fue validado y no se puede regenerar."

    /** Identificador estable del informe de un estudiante (hay uno por estudiante). */
    fun idInforme(estudianteId: String): String = "INF-$estudianteId"

    /** Porcentaje de aciertos desde el cual se describe un contenido como fortaleza. */
    const val UMBRAL_FORTALEZA = 70

    private const val MAX_PREGUNTAS_A_REPASAR = 3

    /** Devuelve null si el estudiante no tiene respuestas en ninguno de los contenidos. */
    fun generar(resumenes: List<ResumenContenido>): BorradorInforme? {
        val conDatos = resumenes.filter { it.vigentes.isNotEmpty() }
        if (conDatos.isEmpty()) return null

        val totalCorrectas = conDatos.sumOf { it.vigentes.count { r -> r.esCorrecta } }
        val total = conDatos.sumOf { it.vigentes.size }

        val rendimiento = buildString {
            append(textoAciertos(totalCorrectas, total))
            if (conDatos.size > 1) {
                conDatos.forEach { resumen ->
                    val correctas = resumen.vigentes.count { it.esCorrecta }
                    append("\n• ${resumen.contenido.asignatura.etiqueta}: ${textoAciertos(correctas, resumen.vigentes.size)}")
                }
            }
        }

        val fortalezas = mutableListOf<String>()
        val aspectos = mutableListOf<String>()

        for (resumen in conDatos) {
            val correctas = resumen.vigentes.count { it.esCorrecta }
            val cantidad = resumen.vigentes.size
            val unidad = resumen.contenido.unidad
            if (porcentaje(correctas, cantidad) >= UMBRAL_FORTALEZA) {
                fortalezas += if (correctas == cantidad) {
                    "Respondió correctamente todas las preguntas de «$unidad» ($correctas de $cantidad)."
                } else {
                    "Respondió correctamente la mayoría de las preguntas de «$unidad» ($correctas de $cantidad)."
                }
            } else {
                aspectos += "Practicar el objetivo «${resumen.contenido.objetivoAprendizaje}» " +
                    "($correctas de $cantidad correctas)."
            }
            val porId = resumen.actividades.associateBy { it.id }
            resumen.vigentes
                .filter { !it.esCorrecta }
                .mapNotNull { porId[it.actividadId]?.enunciado }
                .take(MAX_PREGUNTAS_A_REPASAR)
                .forEach { aspectos += "Repasar preguntas como: «$it»." }
        }

        resumenes.filter { it.vigentes.isEmpty() }.forEach {
            aspectos += "Todavía no registra respuestas en «${it.contenido.unidad}»."
        }

        return BorradorInforme(
            rendimiento = rendimiento,
            fortalezas = fortalezas.ifEmpty {
                listOf("Todavía no hay contenidos con la mayoría de las respuestas correctas.")
            },
            aspectosAReforzar = aspectos.ifEmpty {
                listOf("No se observan aspectos a reforzar en las respuestas registradas.")
            }
        )
    }

    /** Ejemplo: "3 de 4 correctas (75 %)". */
    fun textoAciertos(correctas: Int, total: Int): String =
        "$correctas de $total correctas (${porcentaje(correctas, total)} %)"

    private fun porcentaje(correctas: Int, total: Int): Int =
        if (total == 0) 0 else correctas * 100 / total
}
