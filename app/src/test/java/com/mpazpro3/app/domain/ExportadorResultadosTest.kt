package com.mpazpro3.app.domain

import com.mpazpro3.app.data.DatosIniciales
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportadorResultadosTest {

    private fun fila(estudiante: String = "Estudiante 1") = FilaResultado(
        curso = "3° básico A", estudianteId = "EST-001", estudiante = estudiante,
        asignatura = "Matemática", contenido = "Unidad 2", avancePorcentaje = 60,
        correctas = 3, incorrectas = 1, intentosUsados = 1, maxIntentos = 2, estado = "En progreso"
    )

    @Test
    fun csv_tieneEncabezadoYUnaLineaPorFilaConSaltosCrLf() {
        val csv = ExportadorResultados.generarCsv(listOf(fila(), fila("Estudiante 2")))
        val lineas = csv.split("\r\n")
        assertEquals(4, lineas.size)
        assertEquals("", lineas.last())
        assertEquals(ExportadorResultados.ENCABEZADOS.joinToString(";"), lineas.first())
        assertEquals(
            "3° básico A;EST-001;Estudiante 1;Matemática;Unidad 2;60;3;1;1;2;En progreso",
            lineas[1]
        )
    }

    @Test
    fun csv_sinFilasSoloTrae() {
        assertEquals(ExportadorResultados.ENCABEZADOS.joinToString(";") + "\r\n", ExportadorResultados.generarCsv(emptyList()))
    }

    @Test
    fun escapar_camposConSeparadorOComillas() {
        assertEquals("\"a;b\"", ExportadorResultados.escapar("a;b"))
        assertEquals("\"dijo \"\"hola\"\"\"", ExportadorResultados.escapar("dijo \"hola\""))
        assertEquals("normal", ExportadorResultados.escapar("normal"))
    }

    @Test
    fun escapar_neutralizaFormulasDePlanilla() {
        listOf("=SUMA(A1)", "+1", "-1", "@cmd").forEach {
            assertTrue(ExportadorResultados.escapar(it).startsWith("'"))
        }
    }

    @Test
    fun nombreArchivo_sinCaracteresEspeciales() {
        assertEquals("resultados_3basicoA.csv", ExportadorResultados.nombreArchivo("3° básico A"))
        assertEquals("resultados_curso.csv", ExportadorResultados.nombreArchivo("°°"))
    }

    @Test
    fun construirFilas_unaPorEstudianteYContenidoAsignado() {
        val contenidoC2 = DatosIniciales.contenidos.filter { it.id == "C2" }
        val filas = ExportadorResultados.construirFilas(
            curso = DatosIniciales.CURSO,
            estudiantes = DatosIniciales.estudiantes,
            contenidosAsignados = contenidoC2,
            asignaciones = DatosIniciales.asignaciones,
            actividades = DatosIniciales.actividades,
            seguimientos = DatosIniciales.seguimientos
        )
        assertEquals(DatosIniciales.estudiantes.size, filas.size)

        val sinSeguimiento = filas.first { it.estudianteId == "EST-004" }
        assertEquals(0, sinSeguimiento.avancePorcentaje)
        assertEquals("No iniciado", sinSeguimiento.estado)
        assertEquals(2, sinSeguimiento.maxIntentos)

        val terminado = filas.first { it.estudianteId == "EST-002" }
        assertEquals(100, terminado.avancePorcentaje)
        assertEquals(1, terminado.intentosUsados)
        assertEquals("Finalizado", terminado.estado)
        assertEquals(5, terminado.correctas + terminado.incorrectas)
    }
}
