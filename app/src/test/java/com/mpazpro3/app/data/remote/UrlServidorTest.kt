package com.mpazpro3.app.data.remote

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

/** Parte ya hecha: qué hosts pueden usar http (sin cifrar). Esta prueba pasa desde el inicio. */
class HostPermiteHttpTest {

    @Test
    fun permiteEmuladorLocalhostYRedesPrivadas() {
        assertTrue(UrlServidor.hostPermiteHttp("10.0.2.2"))
        assertTrue(UrlServidor.hostPermiteHttp("localhost"))
        assertTrue(UrlServidor.hostPermiteHttp("192.168.1.20"))
        assertTrue(UrlServidor.hostPermiteHttp("172.16.0.5"))
        assertTrue(UrlServidor.hostPermiteHttp("mi-pc.local"))
    }

    @Test
    fun rechazaServidoresPublicos() {
        assertFalse(UrlServidor.hostPermiteHttp("miservidor.dev"))
        assertFalse(UrlServidor.hostPermiteHttp("8.8.8.8"))
        assertFalse(UrlServidor.hostPermiteHttp("172.32.0.1"))
    }
}

/**
 * TODO(E3-T4) Pruebas de UrlServidor.validar. Se completan después de E3-1.
 *
 * Casos que debe cubrir:
 *  1. "" y "   " -> Invalida(MSG_VACIA).
 *  2. "192.168.1.20:3000" (sin esquema) -> Valida("http://192.168.1.20:3000/").
 *  3. "https://MiServidor.dev/api" -> Valida("https://miservidor.dev/api/") (minúsculas y barra final).
 *  4. "http://miservidor.dev" -> Invalida(MSG_HTTP_PUBLICO); "http://10.0.2.2:3000" -> Valida.
 *  5. "ftp://x" -> Invalida(MSG_ESQUEMA).
 *  6. "http://usuario@10.0.2.2/", "http://10.0.2.2/?a=1" y "http://10.0.2.2/#x" -> Invalida(MSG_EXTRAS).
 *  7. Puerto fuera de rango ("http://10.0.2.2:70000/") -> Invalida(MSG_INVALIDA).
 *  8. normalizarOEmulador(null) y normalizarOEmulador("???") -> URL_EMULADOR.
 */
@Ignore("E3: completar")
class UrlServidorTest {

    @Test
    fun vacia_esInvalida() {
        // TODO(E3-T4)
    }

    @Test
    fun sinEsquema_seCompletaSegunElHost() {
        // TODO(E3-T4)
    }

    @Test
    fun https_seNormaliza() {
        // TODO(E3-T4)
    }

    @Test
    fun httpPublico_seRechaza_yHttpLocal_seAcepta() {
        // TODO(E3-T4)
    }

    @Test
    fun esquemaYExtras_seRechazan() {
        // TODO(E3-T4)
    }

    @Test
    fun puertoFueraDeRango_yFallback() {
        // TODO(E3-T4)
    }
}
