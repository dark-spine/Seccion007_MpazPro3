package com.mpazpro3.app.data

import com.mpazpro3.app.domain.ReglasAcceso
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.Perfil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifica que los datos de ejemplo cumplan las reglas del caso y sean coherentes entre sí. */
class DatosInicialesTest {

    @Test
    fun hayUnUsuarioPorPerfilYLasClavesVanConHash() {
        assertEquals(1, DatosIniciales.usuarios.count { it.perfil == Perfil.UTP })
        assertEquals(1, DatosIniciales.usuarios.count { it.perfil == Perfil.DOCENTE })
        assertTrue(DatosIniciales.usuarios.count { it.perfil == Perfil.ESTUDIANTE } >= 3)
        assertTrue(DatosIniciales.usuarios.all { it.claveHash.length == 64 })
        assertEquals(DatosIniciales.usuarios.size, DatosIniciales.usuarios.map { it.usuario.lowercase() }.toSet().size)
    }

    @Test
    fun usuariosDeEjemploSePuedenAutenticar() {
        val utp = DatosIniciales.usuarios.first { it.perfil == Perfil.UTP }
        assertEquals(ReglasAcceso.hashClave("utp123"), utp.claveHash)
    }

    @Test
    fun unaUnidadPorAsignaturaYUnaSolaAprobada() {
        assertEquals(Asignatura.entries.toSet(), DatosIniciales.contenidos.map { it.asignatura }.toSet())
        assertEquals(Asignatura.entries.size, DatosIniciales.contenidos.size)
        assertEquals(1, DatosIniciales.contenidos.count { it.estado == EstadoAprobacion.APROBADO })
    }

    @Test
    fun elRechazadoTraeObservacion() {
        val rechazados = DatosIniciales.contenidos.filter { it.estado == EstadoAprobacion.RECHAZADO }
        assertTrue(rechazados.isNotEmpty())
        assertTrue(rechazados.all { it.observacionUtp.isNotBlank() })
    }

    @Test
    fun cadaContenidoTieneRecursoDesafiosYEvaluacion() {
        DatosIniciales.contenidos.forEach { contenido ->
            assertTrue(DatosIniciales.recursos.any { it.contenidoId == contenido.id })
            val propias = DatosIniciales.actividades.filter { it.contenidoId == contenido.id }
            assertTrue(propias.any { !it.esEvaluacionFinal })
            assertTrue(propias.any { it.esEvaluacionFinal })
        }
    }

    @Test
    fun cadaActividadTieneExactamenteUnaAlternativaCorrecta() {
        DatosIniciales.actividades.forEach { actividad ->
            assertTrue(actividad.id, actividad.alternativas.count { it.esCorrecta } == 1)
        }
    }

    @Test
    fun identificadoresUnicos() {
        assertEquals(DatosIniciales.actividades.size, DatosIniciales.actividades.map { it.id }.toSet().size)
        assertEquals(DatosIniciales.contenidos.size, DatosIniciales.contenidos.map { it.id }.toSet().size)
        assertEquals(DatosIniciales.recursos.size, DatosIniciales.recursos.map { it.id }.toSet().size)
    }

    @Test
    fun lasAsignacionesYSeguimientosApuntanAContenidosYEstudiantesExistentes() {
        val contenidos = DatosIniciales.contenidos.associateBy { it.id }
        DatosIniciales.asignaciones.forEach {
            assertEquals(EstadoAprobacion.APROBADO, contenidos.getValue(it.contenidoId).estado)
        }
        val estudiantes = DatosIniciales.estudiantes.map { it.id }.toSet()
        val actividades = DatosIniciales.actividades.map { it.id }.toSet()
        DatosIniciales.seguimientos.forEach { seguimiento ->
            assertTrue(seguimiento.estudianteId in estudiantes)
            assertTrue(contenidos.containsKey(seguimiento.contenidoId))
            assertTrue(seguimiento.respuestas.all { it.actividadId in actividades })
        }
    }

    @Test
    fun losDatosNoContienenInformacionRealDeMenores() {
        assertFalse(DatosIniciales.estudiantes.any { it.nombreFicticio.isBlank() })
        assertTrue(DatosIniciales.estudiantes.all { it.nombreFicticio.startsWith("Estudiante") })
    }
}
