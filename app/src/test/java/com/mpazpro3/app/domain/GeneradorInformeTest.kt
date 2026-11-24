package com.mpazpro3.app.domain

import com.mpazpro3.app.data.DatosIniciales
import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Alternativa
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.Dificultad
import com.mpazpro3.app.model.RespuestaEstudiante
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneradorInformeTest {

    private val contenido = Contenido(
        id = "C", asignatura = Asignatura.MATEMATICA, unidad = "Números hasta 1000",
        objetivoAprendizaje = "Representar números", ejeCurricular = "Números", recursoReferencia = "Ref"
    )

    private fun actividad(id: String) = Actividad(
        id = id, contenidoId = "C", enunciado = "¿Pregunta $id?",
        alternativas = listOf(Alternativa("a", true), Alternativa("b", false)),
        dificultad = Dificultad.FACIL, orden = 1
    )

    private fun respuesta(id: String, correcta: Boolean) = RespuestaEstudiante(id, 0, correcta, 1)

    private fun resumen(vararg correctas: Boolean): ResumenContenido {
        val actividades = correctas.indices.map { actividad("A$it") }
        val respuestas = correctas.mapIndexed { i, ok -> respuesta("A$i", ok) }
        return ResumenContenido(contenido, actividades, respuestas)
    }

    @Test
    fun sinRespuestasNoSeGeneraInforme() {
        assertNull(GeneradorInforme.generar(emptyList()))
        assertNull(GeneradorInforme.generar(listOf(ResumenContenido(contenido, emptyList(), emptyList()))))
    }

    @Test
    fun textoAciertos_incluyePorcentaje() {
        assertEquals("3 de 4 correctas (75 %)", GeneradorInforme.textoAciertos(3, 4))
        assertEquals("0 de 0 correctas (0 %)", GeneradorInforme.textoAciertos(0, 0))
    }

    @Test
    fun altoDesempeno_sePresentaComoFortaleza() {
        val borrador = GeneradorInforme.generar(listOf(resumen(true, true, true, false)))!!
        assertTrue(borrador.rendimiento.startsWith("3 de 4 correctas (75 %)"))
        assertTrue(borrador.fortalezas.single().contains("Números hasta 1000"))
        assertTrue(borrador.aspectosAReforzar.any { it.contains("Repasar preguntas") })
    }

    @Test
    fun bajoDesempeno_seIndicaComoAspectoAReforzar() {
        val borrador = GeneradorInforme.generar(listOf(resumen(false, false, true)))!!
        assertTrue(borrador.aspectosAReforzar.first().contains("Representar números"))
        assertEquals(1, borrador.fortalezas.size)
        assertTrue(borrador.fortalezas.first().startsWith("Todavía no hay"))
    }

    @Test
    fun todoCorrecto_noTieneAspectosAReforzar() {
        val borrador = GeneradorInforme.generar(listOf(resumen(true, true)))!!
        assertTrue(borrador.aspectosAReforzar.single().startsWith("No se observan"))
    }

    @Test
    fun contenidoSinRespuestas_seMencionaComoPendiente() {
        val sinDatos = ResumenContenido(contenido.copy(id = "C2", unidad = "Otra unidad"), emptyList(), emptyList())
        val borrador = GeneradorInforme.generar(listOf(resumen(true), sinDatos))!!
        assertTrue(borrador.aspectosAReforzar.any { it.contains("Otra unidad") })
    }

    @Test
    fun elTextoSoloDescribe_noDiagnosticaNiCalifica() {
        val prohibidas = listOf("diagnóstic", "trastorno", "déficit", "nota", "reprob", "promov", "castigo", "repit")
        val borradores = listOf(
            GeneradorInforme.generar(listOf(resumen(true, true, true, false))),
            GeneradorInforme.generar(listOf(resumen(false, false, false))),
            GeneradorInforme.generar(listOf(resumen(true), resumen(false)))
        )
        borradores.forEach { borrador ->
            assertNotNull(borrador)
            val texto = (listOf(borrador!!.rendimiento) + borrador.fortalezas + borrador.aspectosAReforzar)
                .joinToString(" ").lowercase()
            prohibidas.forEach { assertFalse("No debe contener «$it»", texto.contains(it)) }
        }
    }

    @Test
    fun conVariosContenidos_elRendimientoDetallaCadaAsignatura() {
        val borrador = GeneradorInforme.generar(listOf(resumen(true, false), resumen(true)))!!
        assertTrue(borrador.rendimiento.contains("Matemática"))
        assertEquals(3, borrador.rendimiento.lines().size)
    }

    @Test
    fun conLosDatosIniciales_elInformeDeUnEstudianteConRespuestasSeGenera() {
        val seguimiento = DatosIniciales.seguimientos.first { it.estudianteId == "EST-002" }
        val contenidoC2 = DatosIniciales.contenidos.first { it.id == "C2" }
        val actividadesC2 = DatosIniciales.actividades.filter { it.contenidoId == "C2" }
        val vigentes = ReglasProgreso.respuestasVigentes(seguimiento.respuestas, actividadesC2)
        val borrador = GeneradorInforme.generar(listOf(ResumenContenido(contenidoC2, actividadesC2, vigentes)))
        assertNotNull(borrador)
    }
}
