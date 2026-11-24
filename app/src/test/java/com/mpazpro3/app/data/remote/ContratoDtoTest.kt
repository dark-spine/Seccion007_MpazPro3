package com.mpazpro3.app.data.remote

import com.google.gson.Gson
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Estudiante
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Parte ya hecha: el contrato JSON del servidor (backend/docs/API.md) contra los DTO entregados.
 * Estas pruebas pasan desde el inicio y deben seguir pasando mientras completas la capa remota.
 */
class ContratoDtoTest {

    private val gson = Gson()

    @Test
    fun contenido_leeLosNombresDelServidor_yCodigoOAPuedeSerNulo() {
        val json = """{"id":"C1","nivel":"3° básico","asignatura":"MATEMATICA","unidad":"U","objetivoAprendizaje":"OA","codigoOA":null,
            "ejeCurricular":"E","recursoReferencia":"R","estado":"APROBADO","observacionUtp":""}"""
        val dto = gson.fromJson(json, ContenidoDto::class.java)
        assertEquals("C1", dto.id)
        assertEquals("MATEMATICA", dto.asignatura)
        assertEquals("APROBADO", dto.estado)
        assertNull(dto.codigoOA)
    }

    @Test
    fun actividad_conAlternativas() {
        val json = """{"id":"A1","contenidoId":"C2","enunciado":"¿?","alternativas":[{"texto":"3","esCorrecta":true},{"texto":"4","esCorrecta":false}],
            "dificultad":"FACIL","orden":1,"esEvaluacionFinal":false}"""
        val dto = gson.fromJson(json, ActividadDto::class.java)
        assertEquals(2, dto.alternativas.size)
        assertTrue(dto.alternativas[0].esCorrecta)
        assertEquals("FACIL", dto.dificultad)
    }

    @Test
    fun seguimiento_conRespuestas() {
        val json = """{"estudianteId":"EST-001","contenidoId":"C2","materialRevisado":true,"intentosEvaluacionUsados":1,"estado":"EN_PROGRESO",
            "respuestas":[{"actividadId":"A1","alternativaElegida":0,"esCorrecta":true,"numeroIntento":1,"fecha":1700000000000}]}"""
        val dto = gson.fromJson(json, SeguimientoDto::class.java)
        assertTrue(dto.materialRevisado)
        assertEquals(1, dto.respuestas.size)
        assertEquals(1700000000000L, dto.respuestas[0].fecha)
    }

    @Test
    fun errorDelServidor_traeMensajeYCampo() {
        val dto = gson.fromJson("""{"error":"Indica el motivo del rechazo","campo":"observacion"}""", ErrorDto::class.java)
        assertEquals("Indica el motivo del rechazo", dto.error)
        assertEquals("observacion", dto.campo)
    }

    @Test
    fun solicitudes_seSerializanConLosNombresDelContrato() {
        assertEquals("""{"usuario":"utp","clave":"1234"}""", gson.toJson(LoginRequest("utp", "1234")))
        assertEquals("""{"contenidoIds":["C1","C2"],"maxIntentos":3}""", gson.toJson(AsignarRequest(listOf("C1", "C2"), 3)))
        assertEquals("""{"respuestas":{"A5":2}}""", gson.toJson(EvaluacionRequest(mapOf("A5" to 2))))
    }

    @Test
    fun mapeadoresDeEjemplo_estudianteYAsignacion_idaYVuelta() {
        val estudiante = Estudiante("EST-001", "Estudiante 1 (demo)", "3° básico A")
        assertEquals(estudiante, estudiante.aDto().aDominio())
        val asignacion = Asignacion("AS-1", "3° básico A", "C2", 2)
        assertEquals(asignacion, asignacion.aDto().aDominio())
    }
}
