package com.mpazpro3.app.data

import com.mpazpro3.app.data.remote.ApiResult
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.Perfil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El selector elige la fuente (servidor o local) según la preferencia "modo local", y la lee en CADA
 * operación: cambiar el interruptor surte efecto en la acción siguiente, sin recrear nada.
 */
class SelectorFuenteTest {

    private val preferencia = PreferenciaModoMemoria(local = false)
    private val servidor = FuenteFalsa("servidor")
    private val local = FuenteFalsa("local")
    private val lecturasServidor = LecturasFalsas("DESDE-SERVIDOR")
    private val lecturasLocal = LecturasFalsas("DESDE-LOCAL")
    private val selector = SelectorFuente(preferencia, servidor, local, lecturasServidor, lecturasLocal)

    private val usuario = DatosIniciales.usuarios.first { it.perfil == Perfil.DOCENTE }

    @Test
    fun modoServidor_lasOperacionesVanAlServidor() = runTest {
        preferencia.cambiar(false)

        assertEquals("servidor", selector.aprobarContenido("C1", "ok").mensaje)
        assertEquals("servidor", selector.asignarAlCurso("3° básico A", listOf("C2"), 2).mensaje)
        assertEquals("servidor", selector.enviarEvaluacion("EST-001", "C2", emptyMap()).mensaje)
        assertEquals(ResultadoLogin.Falla("servidor"), selector.iniciarSesion("utp", "x"))

        assertTrue(local.llamadas.isEmpty())
        assertEquals(
            listOf("aprobarContenido", "asignarAlCurso", "enviarEvaluacion", "iniciarSesion"),
            servidor.llamadas
        )
    }

    @Test
    fun modoLocal_lasOperacionesVanALaFuenteLocal() = runTest {
        preferencia.cambiar(true)

        assertEquals("local", selector.rechazarContenido("C1", "falta").mensaje)
        assertEquals("local", selector.generarInforme("EST-002").mensaje)
        assertEquals("local", selector.responderDesafio("EST-001", "A1", 0).mensaje)
        assertEquals(ApiResult.Exito("local"), selector.descargarResultadosCsv("3° básico A"))

        assertTrue(servidor.llamadas.isEmpty())
        assertEquals(4, local.llamadas.size)
    }

    @Test
    fun cambioDeModoEnCaliente_laSiguienteOperacionYaUsaOtraFuente() = runTest {
        preferencia.cambiar(false)
        assertEquals("servidor", selector.validarInforme("INF-EST-002").mensaje)

        preferencia.cambiar(true)
        assertEquals("local", selector.validarInforme("INF-EST-002").mensaje)

        preferencia.cambiar(false)
        assertEquals("servidor", selector.validarInforme("INF-EST-002").mensaje)

        assertEquals(2, servidor.llamadas.count { it == "validarInforme" })
        assertEquals(1, local.llamadas.count { it == "validarInforme" })
    }

    @Test
    fun actual_devuelveLaFuenteQueCorrespondeAhora() {
        preferencia.cambiar(true)
        assertSame(local, selector.actual())
        preferencia.cambiar(false)
        assertSame(servidor, selector.actual())
    }

    @Test
    fun lecturas_salenDeLaBaseDelModoActivo() = runTest {
        preferencia.cambiar(false)
        assertEquals("DESDE-SERVIDOR", selector.contenidos().first().single().id)

        preferencia.cambiar(true)
        assertEquals("DESDE-LOCAL", selector.contenidos().first().single().id)
    }

    @Test
    fun estadoYAviso_siguenAlModoActivo() = runTest {
        servidor.estadoFlujo.value = EstadoSync.SinConexion("sin red")
        servidor.avisoFlujo.value = "aviso del servidor"
        local.avisoFlujo.value = "aviso local"

        preferencia.cambiar(false)
        assertEquals(EstadoSync.SinConexion("sin red"), selector.estado.first())
        assertEquals("aviso del servidor", selector.aviso.first())

        preferencia.cambiar(true)
        assertEquals(EstadoSync.Inactivo, selector.estado.first())
        assertEquals("aviso local", selector.aviso.first())
    }

    @Test
    fun sincronizarYCerrarSesion_tambienRespetanElModo() = runTest {
        preferencia.cambiar(true)
        selector.sincronizar(usuario)
        selector.cerrarSesion()
        assertEquals(listOf("sincronizar", "cerrarSesion"), local.llamadas)
        assertTrue(servidor.llamadas.isEmpty())
    }

    @Test
    fun publicarAviso_vaALaFuenteActiva_yDescartarLimpiaAmbas() = runTest {
        preferencia.cambiar(true)
        selector.publicarAviso("hola")
        assertEquals("hola", local.avisoFlujo.value)
        assertEquals(null, servidor.avisoFlujo.value)

        servidor.avisoFlujo.value = "pendiente"
        selector.descartarAviso()
        assertEquals(null, local.avisoFlujo.value)
        assertEquals(null, servidor.avisoFlujo.value)
    }

    @Test
    fun conRepositorioLocalReal_escribirEnModoLocalNoTocaAlServidor() = runTest {
        val almacen = AlmacenLocalMemoria()
        val real = SelectorFuente(preferencia, servidor, RepositorioLocal(almacen), lecturasServidor, lecturasLocal)

        preferencia.cambiar(true)
        val r = real.aprobarContenido("C1", "Revisado")

        assertTrue(r.exito)
        assertEquals("Contenido aprobado", r.mensaje)
        assertTrue(servidor.llamadas.isEmpty())
        assertEquals(EstadoAprobacion.APROBADO, almacen.datos.contenidos.first { it.id == "C1" }.estado)
    }
}
