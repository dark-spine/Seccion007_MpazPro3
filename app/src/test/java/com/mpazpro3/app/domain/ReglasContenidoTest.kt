package com.mpazpro3.app.domain

import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAprobacion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasContenidoTest {

    private fun contenido(id: String, asignatura: Asignatura, estado: EstadoAprobacion) = Contenido(
        id = id,
        asignatura = asignatura,
        unidad = "Unidad $id",
        objetivoAprendizaje = "Objetivo $id",
        ejeCurricular = "Eje",
        recursoReferencia = "Referencia",
        estado = estado
    )

    private val lenguaje = contenido("L1", Asignatura.LENGUAJE, EstadoAprobacion.PENDIENTE)
    private val matematica = contenido("M1", Asignatura.MATEMATICA, EstadoAprobacion.APROBADO)
    private val matematica2 = contenido("M2", Asignatura.MATEMATICA, EstadoAprobacion.PENDIENTE)
    private val ciencias = contenido("C1", Asignatura.CIENCIAS_NATURALES, EstadoAprobacion.RECHAZADO)
    private val todos = listOf(ciencias, matematica, lenguaje, matematica2)

    @Test
    fun rechazar_sinMotivoDaErrorEnElCampo() {
        assertEquals(ReglasContenido.MENSAJE_MOTIVO_OBLIGATORIO, ReglasContenido.errorObservacionAlRechazar("   "))
        assertNull(ReglasContenido.errorObservacionAlRechazar("Falta el código del OA"))
    }

    @Test
    fun aprobar_unaUnidadPorAsignatura() {
        val resultado = ReglasContenido.validarAprobacion(matematica2, todos)
        assertFalse(resultado.exito)
        assertEquals(ReglasContenido.MENSAJE_UNIDAD_YA_APROBADA, resultado.mensaje)
    }

    @Test
    fun aprobar_asignaturaSinOtraAprobadaEsValido() {
        assertTrue(ReglasContenido.validarAprobacion(lenguaje, todos).exito)
    }

    @Test
    fun aprobar_elMismoContenidoYaAprobadoNoSeBloqueaASiMismo() {
        assertTrue(ReglasContenido.validarAprobacion(matematica, todos).exito)
    }

    @Test
    fun aprobar_contenidoInexistente() {
        val resultado = ReglasContenido.validarAprobacion(null, todos)
        assertFalse(resultado.exito)
        assertEquals(ReglasContenido.MENSAJE_CONTENIDO_NO_ENCONTRADO, resultado.mensaje)
    }

    @Test
    fun rechazar_exigeMotivo() {
        val resultado = ReglasContenido.validarRechazo(lenguaje, "", emptyList())
        assertFalse(resultado.exito)
        assertEquals(ReglasContenido.MENSAJE_MOTIVO_OBLIGATORIO, resultado.mensaje)
    }

    @Test
    fun rechazar_contenidoYaAsignadoNoSePermite() {
        val asignaciones = listOf(Asignacion("AS", "3° A", "M1", 2))
        val resultado = ReglasContenido.validarRechazo(matematica, "Motivo", asignaciones)
        assertFalse(resultado.exito)
        assertEquals(ReglasContenido.MENSAJE_YA_ASIGNADO, resultado.mensaje)
    }

    @Test
    fun rechazar_conMotivoYSinAsignacionEsValido() {
        assertTrue(ReglasContenido.validarRechazo(lenguaje, "Motivo", emptyList()).exito)
    }

    @Test
    fun filtrar_porAsignaturaYEstado() {
        assertEquals(listOf("M2", "M1"), ReglasContenido.filtrar(todos, Asignatura.MATEMATICA, null).map { it.id })
        assertEquals(listOf("C1"), ReglasContenido.filtrar(todos, null, EstadoAprobacion.RECHAZADO).map { it.id })
        assertTrue(ReglasContenido.filtrar(todos, Asignatura.HISTORIA, null).isEmpty())
    }

    @Test
    fun filtrar_losPendientesSalenPrimero() {
        val ids = ReglasContenido.filtrar(todos, null, null).map { it.id }
        assertEquals(listOf("L1", "M2", "M1", "C1"), ids)
    }

    @Test
    fun disponiblesParaAsignar_soloAprobados() {
        assertEquals(listOf("M1"), ReglasContenido.disponiblesParaAsignar(todos).map { it.id })
    }

    @Test
    fun asignadosAlCurso_soloAprobadosYDelCurso() {
        val asignaciones = listOf(
            Asignacion("a1", "3° A", "M1", 2),
            Asignacion("a2", "3° A", "C1", 2),
            Asignacion("a3", "3° B", "L1", 2)
        )
        assertEquals(listOf("M1"), ReglasContenido.asignadosAlCurso(todos, asignaciones, "3° A").map { it.id })
        assertTrue(ReglasContenido.asignadosAlCurso(todos, asignaciones, null).isEmpty())
    }
}
