package com.mpazpro3.app.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.mpazpro3.app.data.*
import com.mpazpro3.app.data.remote.ConfiguracionServidor
import com.mpazpro3.app.domain.ReglasAcceso
import com.mpazpro3.app.model.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class MpazProViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val fuente = mockk<SelectorFuente>(relaxed = true)
    private lateinit var vm: MpazProViewModel

    @Before fun preparar() {
        Dispatchers.setMain(dispatcher)
        val contenedor = mockk<AppContenedor>()
        every { contenedor.fuente } returns fuente
        every { contenedor.preferencia } returns PreferenciaModoMemoria(true)
        val config = mockk<ConfiguracionServidor>()
        every { config.url } returns "http://10.0.2.2:3000/"
        every { contenedor.configuracion } returns config
        coEvery { contenedor.sembrarSiEstaVacia() } just Runs
        every { fuente.estado } returns flowOf(EstadoSync.Inactivo)
        every { fuente.pendientes } returns flowOf(0)
        every { fuente.aviso } returns flowOf(null)
        every { fuente.contenidos() } returns flowOf(emptyList())
        every { fuente.recursos() } returns flowOf(emptyList())
        every { fuente.actividades() } returns flowOf(emptyList())
        every { fuente.asignaciones() } returns flowOf(emptyList())
        every { fuente.estudiantes() } returns flowOf(emptyList())
        every { fuente.seguimientos() } returns flowOf(emptyList())
        every { fuente.informes() } returns flowOf(emptyList())
        vm = MpazProViewModel(mockk<Application>(relaxed = true), contenedor)
    }

    @After fun terminar() {
        vm.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test fun formatoInvalidoNoConsultaRepositorio() = runTest(dispatcher) {
        assertFalse(vm.login("a b", "1234"))
        assertEquals(ReglasAcceso.MENSAJE_FORMATO_USUARIO, vm.erroresLogin.value.usuario)
        coVerify(exactly = 0) { fuente.iniciarSesion(any(), any()) }
        vm.limpiarErroresLogin()
        assertFalse(vm.erroresLogin.value.hayErrores)
    }

    @Test fun credencialesIncorrectasMuestranErrorGeneral() = runTest(dispatcher) {
        coEvery { fuente.iniciarSesion("est001", "9999") } returns ResultadoLogin.Rechazado(ReglasAcceso.MENSAJE_CREDENCIALES, null)
        assertFalse(vm.login("est001", "9999"))
        assertEquals(ReglasAcceso.MENSAJE_CREDENCIALES, vm.erroresLogin.value.general)
        assertNull(vm.usuarioActivo.value)
    }

    @Test fun loginLocalCorrectoActivaUsuario() = runTest(dispatcher) {
        val usuario = Usuario("est001", "Estudiante", "est001", "hash", Perfil.ESTUDIANTE)
        coEvery { fuente.iniciarSesion("est001", "1234") } returns ResultadoLogin.Exito(usuario, false, true)
        assertTrue(vm.login("est001", "1234"))
        assertEquals(usuario, vm.usuarioActivo.value)
        assertFalse(vm.erroresLogin.value.hayErrores)
        coVerify(exactly = 0) { fuente.sincronizar(any()) }
    }

    @Test fun sinSesionNoPuedeAprobarContenido() = runTest(dispatcher) {
        assertFalse(vm.aprobarContenido("mat", "").exito)
        coVerify(exactly = 0) { fuente.aprobarContenido(any(), any()) }
    }
}
