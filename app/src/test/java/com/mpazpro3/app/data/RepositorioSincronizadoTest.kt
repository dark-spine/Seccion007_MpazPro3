package com.mpazpro3.app.data

import com.mpazpro3.app.data.remote.ColaPendientesMemoria
import com.mpazpro3.app.data.remote.FuenteRemota
import com.mpazpro3.app.data.remote.SesionApi
import org.junit.Ignore
import org.junit.Test

/**
 * TODO(E3-T5) Pruebas del repositorio contra un ApiService falso: la API manda y Room (aquí una copia en
 * memoria) la sigue. Se completan en la Parte 4, después de E3-6 a E3-9. Las utilidades FakeApiService,
 * FakeCacheLocal, FakeConectividad y errorServidor(...) están en Fakes.kt (ya hechas).
 *
 * Patrón de cada caso:
 *   api.programar("contenidos") { listOf(...) }        // lo que responde el servidor (o lanza errorServidor(409, "...", "campo"))
 *   sesion.token = "tok-123"                           // sesión iniciada
 *   val r = repo.aprobarContenido("C1", "ok")          // acción
 *   assertEquals(...) y comprobar cache.contenidos / api.cantidad("aprobarContenido")
 *
 * Casos que debe cubrir:
 *  1.  sincronizar con el servidor encendido: la copia local queda con exactamente lo recibido (cache.reemplazos == 1) y estado Ok.
 *  2.  sincronizar sin red (red.hayRed = false): no llama a la API, no toca la copia, estado SinConexion.
 *  3.  sincronizar con un endpoint que responde 500: estado Error y la copia NO se reemplaza (nada a medias).
 *  4.  sincronizar como UTP no pide estudiantes ni informes; como estudiante no pide informes.
 *  5.  iniciarSesion correcto: guarda el token en la sesión y el usuario en la copia local; enLinea = true.
 *  6.  iniciarSesion con clave incorrecta (401): Rechazado con el mensaje del servidor y sin token.
 *  7.  iniciarSesion sin red y usuario ya guardado: ingresa con enLinea = false y sin token; sin usuario guardado: Falla.
 *  8.  aprobarContenido que el servidor rechaza con 409 (RN-04): Resultado.error con el mensaje del servidor y la copia no cambia.
 *  9.  aprobarContenido correcto: Resultado.ok y la copia refleja el nuevo estado.
 *  10. rechazarContenido sin motivo: error inmediato y api.cantidad("rechazarContenido") == 0.
 *  11. asignarAlCurso con un contenido no autorizado (409): error con el mensaje y sin cambios en la copia.
 *  12. enviarEvaluacion sin intentos restantes (409, RN-03): error con el mensaje del servidor; sin red: nunca se registra el intento.
 *  13. responderDesafio correcto devuelve esCorrecta del servidor; con error del servidor devuelve null y el mensaje.
 *  14. generarInforme de un informe ya validado (409): error con el mensaje; validarInforme correcto refleja el estado VALIDADO.
 *  15. cerrarSesion limpia el token aunque el servidor no responda.
 *  16. (avanzado) material revisado sin red: queda en la cola de pendientes (cola.todas().size == 1) y se informa.
 */
@Ignore("E3: completar")
class RepositorioSincronizadoTest {

    private val api = FakeApiService()
    private val cache = FakeCacheLocal()
    private val cola = ColaPendientesMemoria()
    private val red = FakeConectividad()
    private val sesion = SesionApi()
    private val repo = RepositorioSincronizado(FuenteRemota({ api }, red, sesion), cache, cola, red, sesion)

    @Test
    fun sincronizar_conServidor_reemplazaLaCopia() {
        // TODO(E3-T5): runTest { ... } con kotlinx.coroutines.test
    }

    @Test
    fun sincronizar_sinRed_conservaLaCopia() {
        // TODO(E3-T5)
    }

    @Test
    fun sincronizar_conError500_noDejaCopiaAMedias() {
        // TODO(E3-T5)
    }

    @Test
    fun sincronizar_pideSoloLoQueElPerfilPuedeVer() {
        // TODO(E3-T5)
    }

    @Test
    fun iniciarSesion_correcto_guardaTokenYUsuario() {
        // TODO(E3-T5)
    }

    @Test
    fun iniciarSesion_claveIncorrecta_esRechazado() {
        // TODO(E3-T5)
    }

    @Test
    fun iniciarSesion_sinRed_usaLosDatosGuardados() {
        // TODO(E3-T5)
    }

    @Test
    fun aprobarContenido_conflicto409_muestraElMensajeDelServidor() {
        // TODO(E3-T5)
    }

    @Test
    fun aprobarContenido_correcto_reflejaEnLaCopia() {
        // TODO(E3-T5)
    }

    @Test
    fun rechazarContenido_sinMotivo_noLlamaAlServidor() {
        // TODO(E3-T5)
    }

    @Test
    fun asignarAlCurso_contenidoNoAutorizado_noCambiaLaCopia() {
        // TODO(E3-T5)
    }

    @Test
    fun enviarEvaluacion_sinIntentos_oSinRed_noRegistraIntento() {
        // TODO(E3-T5)
    }

    @Test
    fun responderDesafio_devuelveLoQueDiceElServidor() {
        // TODO(E3-T5)
    }

    @Test
    fun informe_yaValidado_esConflicto() {
        // TODO(E3-T5)
    }

    @Test
    fun cerrarSesion_limpiaElTokenAunqueFalleElServidor() {
        // TODO(E3-T5)
    }
}
