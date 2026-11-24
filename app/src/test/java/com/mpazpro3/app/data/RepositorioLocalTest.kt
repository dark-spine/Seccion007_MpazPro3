package com.mpazpro3.app.data

import com.mpazpro3.app.data.remote.ApiResult
import com.mpazpro3.app.domain.GeneradorInforme
import com.mpazpro3.app.domain.ReglasAcceso
import com.mpazpro3.app.domain.ReglasContenido
import com.mpazpro3.app.domain.ReglasEvaluacion
import com.mpazpro3.app.domain.ReglasPlanificacion
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.EstadoAvance
import com.mpazpro3.app.model.EstadoValidacion
import com.mpazpro3.app.model.Perfil
import com.mpazpro3.app.model.Resultado
import com.mpazpro3.app.model.Usuario
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Modo local: RepositorioLocal con una base en memoria. Comprueba que cada acción con escritura aplica
 * las mismas reglas de negocio que el servidor (casos correctos y errores), con los mismos mensajes.
 */
class RepositorioLocalTest {

    private val almacen = AlmacenLocalMemoria()
    private val repo = RepositorioLocal(almacen, ahora = { 1_800_000_000_000L }, fechaDeHoy = { "15-11-2026" })

    private fun contenido(id: String) = almacen.datos.contenidos.first { it.id == id }

    private fun seguimiento(estudianteId: String, contenidoId: String) =
        almacen.datos.seguimientos.firstOrNull { it.estudianteId == estudianteId && it.contenidoId == contenidoId }

    // ------------------------------------------------------------------ acceso (RF-00)

    @Test
    fun login_conLaMismaSalQueLaApp_ingresaYMarcaModoLocal() = runTest {
        val r = repo.iniciarSesion("utp@mpazpro3.cl", "utp123")

        assertTrue(r is ResultadoLogin.Exito)
        r as ResultadoLogin.Exito
        assertEquals(Perfil.UTP, r.usuario.perfil)
        assertTrue(r.modoLocal)
        assertFalse(r.enLinea)
        assertEquals(ReglasAcceso.hashClave("utp123"), r.usuario.claveHash)
    }

    @Test
    fun login_ignoraMayusculasYEspaciosDelUsuario_peroLaClaveDistingue() = runTest {
        assertTrue(repo.iniciarSesion("  EST001 ", "1234") is ResultadoLogin.Exito)
        assertTrue(repo.iniciarSesion("docente.3basicoA@MPAZPRO3.CL", "docente123") is ResultadoLogin.Exito)
        assertTrue(repo.iniciarSesion("utp@mpazpro3.cl", "UTP123") is ResultadoLogin.Rechazado)
    }

    @Test
    fun login_credencialesIncorrectas_usaElMensajeGenerico() = runTest {
        val r = repo.iniciarSesion("est001", "9999")

        assertEquals(ResultadoLogin.Rechazado(ReglasAcceso.MENSAJE_CREDENCIALES, null), r)
    }

    @Test
    fun login_siLaBaseFalla_avisaSinCerrarLaApp() = runTest {
        val rota = object : AlmacenLocal by AlmacenLocalMemoria() {
            override suspend fun autenticar(usuarioNormalizado: String, claveHash: String): Usuario? =
                throw IllegalStateException("base cerrada")
        }

        val r = RepositorioLocal(rota).iniciarSesion("utp@mpazpro3.cl", "utp123")

        assertTrue(r is ResultadoLogin.Falla)
    }

    @Test
    fun sincronizar_enModoLocalNoHaceNada() = runTest {
        val usuario = (repo.iniciarSesion("utp@mpazpro3.cl", "utp123") as ResultadoLogin.Exito).usuario

        assertEquals(EstadoSync.Ok, repo.sincronizar(usuario))
    }

    // ------------------------------------------------------------------ autorización de contenidos (RF-01, RN-04)

    @Test
    fun aprobar_contenidoPendiente_quedaAprobadoConSuObservacion() = runTest {
        val r = repo.aprobarContenido("C1", "Listo para el curso")

        assertTrue(r.exito)
        assertEquals("Contenido aprobado", r.mensaje)
        assertEquals(EstadoAprobacion.APROBADO, contenido("C1").estado)
        assertEquals("Listo para el curso", contenido("C1").observacionUtp)
    }

    @Test
    fun aprobar_segundaUnidadDeLaMismaAsignatura_esRechazadaPorRN04() = runTest {
        val otraMatematica = Contenido(
            id = "C5", asignatura = Asignatura.MATEMATICA, unidad = "Unidad 2", objetivoAprendizaje = "o",
            ejeCurricular = "e", recursoReferencia = "r"
        )
        almacen.datos = almacen.datos.copy(contenidos = almacen.datos.contenidos + otraMatematica)

        val r = repo.aprobarContenido("C5", "")

        assertFalse(r.exito)
        assertEquals(ReglasContenido.MENSAJE_UNIDAD_YA_APROBADA, r.mensaje)
        assertEquals(EstadoAprobacion.PENDIENTE, contenido("C5").estado)
    }

    @Test
    fun aprobar_contenidoQueNoExiste_dice_noSeEncontro() = runTest {
        val r = repo.aprobarContenido("XX", "")

        assertEquals(Resultado.error(ReglasContenido.MENSAJE_CONTENIDO_NO_ENCONTRADO), r)
    }

    @Test
    fun rechazar_sinMotivo_noCambiaNada() = runTest {
        val r = repo.rechazarContenido("C3", "   ")

        assertFalse(r.exito)
        assertEquals(ReglasContenido.MENSAJE_MOTIVO_OBLIGATORIO, r.mensaje)
        assertEquals(EstadoAprobacion.PENDIENTE, contenido("C3").estado)
    }

    @Test
    fun rechazar_contenidoYaAsignadoAUnCurso_esRechazadoPorLaRegla() = runTest {
        val r = repo.rechazarContenido("C2", "Ya no corresponde")

        assertFalse(r.exito)
        assertEquals(ReglasContenido.MENSAJE_YA_ASIGNADO, r.mensaje)
        assertEquals(EstadoAprobacion.APROBADO, contenido("C2").estado)
    }

    @Test
    fun rechazar_conMotivo_guardaEstadoYObservacion() = runTest {
        val r = repo.rechazarContenido("C3", "Falta el recurso de referencia")

        assertTrue(r.exito)
        assertEquals(EstadoAprobacion.RECHAZADO, contenido("C3").estado)
        assertEquals("Falta el recurso de referencia", contenido("C3").observacionUtp)
    }

    // ------------------------------------------------------------------ planificación docente (RF-04, RF-05, RN-02)

    @Test
    fun asignar_contenidoAprobado_creaLaAsignacionConSuMaximo() = runTest {
        repo.aprobarContenido("C1", "")

        val r = repo.asignarAlCurso(DatosIniciales.CURSO, listOf("C1", "C1"), 3)

        assertTrue(r.exito)
        assertEquals("Asignación guardada: 1 contenido(s) con máximo de 3 intento(s).", r.mensaje)
        val nueva = almacen.datos.asignaciones.first { it.contenidoId == "C1" }
        assertEquals(3, nueva.maxIntentosEvaluacion)
        assertEquals(ReglasPlanificacion.idAsignacion(DatosIniciales.CURSO, "C1"), nueva.id)
    }

    @Test
    fun asignar_contenidoYaAsignado_actualizaElMaximoSinDuplicar() = runTest {
        val r = repo.asignarAlCurso(DatosIniciales.CURSO, listOf("C2"), 5)

        assertTrue(r.exito)
        val delC2 = almacen.datos.asignaciones.filter { it.contenidoId == "C2" }
        assertEquals(1, delC2.size)
        assertEquals(5, delC2.single().maxIntentosEvaluacion)
    }

    @Test
    fun asignar_conUnContenidoNoAutorizado_noAsignaNinguno_todoONada() = runTest {
        val antes = almacen.datos.asignaciones

        val r = repo.asignarAlCurso(DatosIniciales.CURSO, listOf("C2", "C1"), 2)

        assertFalse(r.exito)
        assertTrue(r.mensaje.startsWith(ReglasPlanificacion.MENSAJE_NO_AUTORIZADO))
        assertEquals(antes, almacen.datos.asignaciones)
    }

    @Test
    fun asignar_conMaximoFueraDeRangoOSinSeleccion_devuelveElMensajeDeLaRegla() = runTest {
        assertEquals(
            ReglasPlanificacion.errorMaxIntentos(11),
            repo.asignarAlCurso(DatosIniciales.CURSO, listOf("C2"), 11).mensaje
        )
        assertEquals(
            ReglasPlanificacion.MENSAJE_SIN_SELECCION,
            repo.asignarAlCurso(DatosIniciales.CURSO, emptyList(), 2).mensaje
        )
    }

    @Test
    fun asignar_unDocenteNoPuedeTrabajarConOtroCurso() = runTest {
        repo.iniciarSesion("docente.3basicoA@mpazpro3.cl", "docente123")

        val r = repo.asignarAlCurso("4° básico B", listOf("C2"), 2)

        assertFalse(r.exito)
        assertEquals(RepositorioLocal.MENSAJE_CURSO_AJENO, r.mensaje)
    }

    // ------------------------------------------------------------------ estudiante (RF-07 a RF-11, RN-02, RN-03)

    @Test
    fun material_revisado_pasaAEnProgresoYSumaAvance() = runTest {
        val r = repo.confirmarMaterialRevisado("EST-001", "C2")

        assertTrue(r.exito)
        assertEquals("Material revisado", r.mensaje)
        val s = seguimiento("EST-001", "C2")!!
        assertTrue(s.materialRevisado)
        assertEquals(EstadoAvance.EN_PROGRESO, s.estado)
    }

    @Test
    fun material_deUnContenidoNoAsignado_esRechazado() = runTest {
        val r = repo.confirmarMaterialRevisado("EST-001", "C3")

        assertFalse(r.exito)
        assertEquals(ReglasEvaluacion.MENSAJE_NO_ASIGNADO, r.mensaje)
        assertNull(seguimiento("EST-001", "C3"))
    }

    @Test
    fun material_siElContenidoYaNoEstaAprobado_noSePuedeTrabajar() = runTest {
        almacen.datos = almacen.datos.copy(
            contenidos = almacen.datos.contenidos.map {
                if (it.id == "C2") it.copy(estado = EstadoAprobacion.RECHAZADO) else it
            }
        )

        val r = repo.confirmarMaterialRevisado("EST-001", "C2")

        assertEquals(ReglasEvaluacion.MENSAJE_NO_ASIGNADO, r.mensaje)
    }

    @Test
    fun desafio_registraCorrectaEIncorrecta_yElNumeroDeIntentoSube() = runTest {
        val a = repo.responderDesafio("EST-001", "A2", 0)
        val b = repo.responderDesafio("EST-001", "A2", 1)

        assertEquals(false, a.esCorrecta)
        assertEquals(true, b.esCorrecta)
        val respuestas = seguimiento("EST-001", "C2")!!.respuestas.filter { it.actividadId == "A2" }
        assertEquals(listOf(1, 2), respuestas.map { it.numeroIntento })
        assertEquals(listOf(false, true), respuestas.map { it.esCorrecta })
        assertEquals(1_800_000_000_000L, respuestas.first().fecha)
        assertEquals(EstadoAvance.EN_PROGRESO, seguimiento("EST-001", "C2")!!.estado)
    }

    @Test
    fun desafio_conAlternativaInexistenteOContenidoNoAsignado_noRegistraNada() = runTest {
        val invalida = repo.responderDesafio("EST-001", "A1", 7)
        val noAsignado = repo.responderDesafio("EST-001", "A7", 0)
        val deEvaluacion = repo.responderDesafio("EST-001", "E1", 0)

        assertNull(invalida.esCorrecta)
        assertEquals(ReglasEvaluacion.MENSAJE_RESPUESTA_INVALIDA, invalida.mensaje)
        assertNull(noAsignado.esCorrecta)
        assertEquals(ReglasEvaluacion.MENSAJE_NO_ASIGNADO, noAsignado.mensaje)
        assertNull(deEvaluacion.esCorrecta)
        assertEquals(RepositorioLocal.MENSAJE_SOLO_EN_EVALUACION, deEvaluacion.mensaje)
        assertTrue(seguimiento("EST-001", "C2")!!.respuestas.isEmpty())
    }

    @Test
    fun evaluacion_correcta_guardaElIntentoYFinalizaElContenido() = runTest {
        val r = repo.enviarEvaluacion("EST-001", "C2", mapOf("E1" to 0, "E2" to 1))

        assertTrue(r.exito)
        assertEquals("¡Terminaste la evaluación! Respondiste bien 1 de 2. Intentos restantes: 1.", r.mensaje)
        val s = seguimiento("EST-001", "C2")!!
        assertEquals(1, s.intentosEvaluacionUsados)
        assertEquals(EstadoAvance.FINALIZADO, s.estado)
        assertEquals(setOf("E1", "E2"), s.respuestas.map { it.actividadId }.toSet())
    }

    @Test
    fun evaluacion_respetaElMaximoDeIntentosDelDocente_RN03() = runTest {
        // EST-002 ya usó 1 de 2 intentos.
        assertTrue(repo.enviarEvaluacion("EST-002", "C2", mapOf("E1" to 0, "E2" to 0)).exito)

        val tercero = repo.enviarEvaluacion("EST-002", "C2", mapOf("E1" to 0, "E2" to 0))

        assertFalse(tercero.exito)
        assertEquals(ReglasEvaluacion.MENSAJE_SIN_INTENTOS, tercero.mensaje)
        assertEquals(2, seguimiento("EST-002", "C2")!!.intentosEvaluacionUsados)
    }

    @Test
    fun evaluacion_incompletaONoAsignada_noGastaIntentos() = runTest {
        val incompleta = repo.enviarEvaluacion("EST-001", "C2", mapOf("E1" to 0))
        val noAsignada = repo.enviarEvaluacion("EST-001", "C3", mapOf("E5" to 1, "E6" to 2))
        val invalida = repo.enviarEvaluacion("EST-001", "C2", mapOf("E1" to 9, "E2" to 0))

        assertEquals(ReglasEvaluacion.MENSAJE_INCOMPLETA, incompleta.mensaje)
        assertEquals(ReglasEvaluacion.MENSAJE_NO_ASIGNADO, noAsignada.mensaje)
        assertEquals(ReglasEvaluacion.MENSAJE_RESPUESTA_INVALIDA, invalida.mensaje)
        assertEquals(0, seguimiento("EST-001", "C2")?.intentosEvaluacionUsados ?: 0)
    }

    @Test
    fun evaluacion_todoSeHaceDentroDeUnaTransaccion() = runTest {
        repo.enviarEvaluacion("EST-001", "C2", mapOf("E1" to 0, "E2" to 0))

        assertEquals(1, almacen.transacciones)
    }

    // ------------------------------------------------------------------ resultados (RF-06) e informe (RF-12, RF-13)

    @Test
    fun csv_traeEncabezadosYUnaFilaPorEstudiante() = runTest {
        val r = repo.descargarResultadosCsv(DatosIniciales.CURSO)

        assertTrue(r is ApiResult.Exito)
        val lineas = (r as ApiResult.Exito).datos.trim().split("\r\n")
        assertTrue(lineas.first().startsWith("Curso;Estudiante ID;Estudiante;Asignatura;Contenido;Avance (%)"))
        assertEquals(1 + DatosIniciales.estudiantes.size, lineas.size)
    }

    @Test
    fun csv_deUnCursoSinEstudiantes_devuelveElError404() = runTest {
        val r = repo.descargarResultadosCsv("Curso inexistente")

        assertEquals(ApiResult.Error(404, "No hay resultados para exportar."), r)
    }

    @Test
    fun informe_sinRespuestas_noSeGenera() = runTest {
        val r = repo.generarInforme("EST-001")

        assertFalse(r.exito)
        assertEquals(GeneradorInforme.MENSAJE_SIN_DATOS, r.mensaje)
        assertTrue(almacen.datos.informes.isEmpty())
    }

    @Test
    fun informe_nace_enBorrador_yAlValidarseYaNoSeRegenera() = runTest {
        val generado = repo.generarInforme("EST-002")

        assertTrue(generado.exito)
        assertEquals("Informe generado en estado borrador.", generado.mensaje)
        val informe = almacen.datos.informes.single()
        assertEquals("INF-EST-002", informe.id)
        assertEquals(EstadoValidacion.BORRADOR, informe.estado)
        assertEquals("15-11-2026", informe.fecha)

        assertEquals("Informe validado por el docente.", repo.validarInforme(informe.id).mensaje)
        assertEquals(EstadoValidacion.VALIDADO, almacen.datos.informes.single().estado)

        val regenerar = repo.generarInforme("EST-002")
        assertFalse(regenerar.exito)
        assertEquals(GeneradorInforme.MENSAJE_YA_VALIDADO, regenerar.mensaje)
        assertEquals("El informe ya estaba validado.", repo.validarInforme(informe.id).mensaje)
    }

    @Test
    fun informe_validarUnoInexistente_oGenerarParaUnEstudianteInexistente_dicenElMotivo() = runTest {
        assertEquals("No se encontró el informe.", repo.validarInforme("INF-XX").mensaje)
        assertEquals(RepositorioLocal.MENSAJE_ESTUDIANTE_NO_ENCONTRADO, repo.generarInforme("EST-999").mensaje)
    }

    // ------------------------------------------------------------------ restablecer datos locales

    @Test
    fun restablecer_devuelveLosDatosDeEjemploYBorraLoCreado() = runTest {
        repo.aprobarContenido("C1", "x")
        repo.confirmarMaterialRevisado("EST-001", "C2")

        val r = repo.restablecerDatos()

        assertTrue(r.exito)
        assertEquals(1, almacen.restablecimientos)
        assertEquals(EstadoAprobacion.PENDIENTE, contenido("C1").estado)
        assertNotNull(seguimiento("EST-001", "C2"))
        assertFalse(seguimiento("EST-001", "C2")!!.materialRevisado)
    }

    @Test
    fun restablecer_siLaBaseFalla_avisaDelError() = runTest {
        val rota = object : AlmacenLocal by AlmacenLocalMemoria() {
            override suspend fun restablecer(): Unit = throw IllegalStateException("disco lleno")
        }

        val r = RepositorioLocal(rota).restablecerDatos()

        assertFalse(r.exito)
    }
}
