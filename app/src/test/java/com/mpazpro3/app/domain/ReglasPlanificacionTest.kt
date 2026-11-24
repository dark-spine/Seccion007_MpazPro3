package com.mpazpro3.app.domain

import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAprobacion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasPlanificacionTest {

    private fun contenido(id: String, estado: EstadoAprobacion) = Contenido(
        id = id,
        asignatura = Asignatura.MATEMATICA,
        unidad = "Unidad $id",
        objetivoAprendizaje = "Objetivo",
        ejeCurricular = "Eje",
        recursoReferencia = "Referencia",
        estado = estado
    )

    private val aprobado = contenido("A", EstadoAprobacion.APROBADO)
    private val otroAprobado = contenido("B", EstadoAprobacion.APROBADO)
    private val pendiente = contenido("P", EstadoAprobacion.PENDIENTE)
    private val todos = listOf(aprobado, otroAprobado, pendiente)

    @Test
    fun maxIntentos_limitesDeUnoADiez() {
        assertNotNull(ReglasPlanificacion.errorMaxIntentos(0))
        assertNull(ReglasPlanificacion.errorMaxIntentos(1))
        assertNull(ReglasPlanificacion.errorMaxIntentos(10))
        assertNotNull(ReglasPlanificacion.errorMaxIntentos(11))
    }

    @Test
    fun asignar_contenidosAprobadosEsValido() {
        assertTrue(ReglasPlanificacion.validarAsignacion("3° A", listOf("A", "B"), 3, todos).exito)
    }

    @Test
    fun asignar_contenidoNoAutorizadoBloqueaTodoElLote() {
        val resultado = ReglasPlanificacion.validarAsignacion("3° A", listOf("A", "P"), 3, todos)
        assertFalse(resultado.exito)
        assertTrue(resultado.mensaje.startsWith(ReglasPlanificacion.MENSAJE_NO_AUTORIZADO))
    }

    @Test
    fun asignar_sinSeleccionSinCursoOConIntentosInvalidos() {
        assertEquals(
            ReglasPlanificacion.MENSAJE_SIN_SELECCION,
            ReglasPlanificacion.validarAsignacion("3° A", emptyList(), 2, todos).mensaje
        )
        assertEquals(
            ReglasPlanificacion.MENSAJE_SIN_CURSO,
            ReglasPlanificacion.validarAsignacion(" ", listOf("A"), 2, todos).mensaje
        )
        assertFalse(ReglasPlanificacion.validarAsignacion("3° A", listOf("A"), 0, todos).exito)
    }

    @Test
    fun asignar_contenidoInexistente() {
        assertFalse(ReglasPlanificacion.validarAsignacion("3° A", listOf("Z"), 2, todos).exito)
    }

    @Test
    fun idAsignacion_esEstablePorCursoYContenido() {
        assertEquals("AS-3A-C2", ReglasPlanificacion.idAsignacion("3° A", "C2"))
        assertEquals(
            ReglasPlanificacion.idAsignacion("3° A", "C2"),
            ReglasPlanificacion.idAsignacion("3° A", "C2")
        )
    }
}
