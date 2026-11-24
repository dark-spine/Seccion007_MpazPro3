package com.mpazpro3.app.data.remote

/**
 * Validación y normalización de la dirección del servidor (pantalla "Servidor").
 *
 * Regla de seguridad: https siempre se acepta; http (sin cifrar) solo hacia el emulador
 * (10.0.2.2), localhost o una red privada. Las mismas reglas las aplica ProteccionHttpInterceptor
 * en cada petición, porque Android no permite expresar rangos de red en network_security_config.
 */
object UrlServidor {

    sealed class Validacion {
        data class Valida(val url: String) : Validacion()
        data class Invalida(val mensaje: String) : Validacion()
    }

    const val MSG_VACIA = "Escribe la dirección del servidor."
    const val MSG_ESQUEMA = "La dirección debe empezar con http:// o https://."
    const val MSG_INVALIDA = "La dirección no es válida. Ejemplo: http://192.168.1.20:3000/"
    const val MSG_HTTP_PUBLICO =
        "Una dirección http:// solo se permite para el emulador (10.0.2.2), localhost o una red local. " +
            "Para un servidor público usa https://."
    const val MSG_EXTRAS = "La dirección no debe llevar usuario, parámetros ni #."

    /** Dirección usada si la configurada no es válida. */
    const val URL_EMULADOR = "http://10.0.2.2:3000/"

    /**
     * TODO(E3-1) Valida y normaliza lo que el usuario escribió en la pantalla "Servidor".
     *
     * Archivo: este. Qué hacer (en este orden, devolviendo Invalida con el mensaje constante indicado):
     *  1. Quitar espacios al inicio y al final; si queda vacío -> MSG_VACIA.
     *  2. Si no trae "://", agregar el esquema: "http://" cuando hostPermiteHttp(host) y "https://" en otro caso.
     *  3. Interpretarla con java.net.URI; si falla (URISyntaxException) -> MSG_INVALIDA.
     *  4. El esquema debe ser http o https -> si no, MSG_ESQUEMA. Debe tener host -> si no, MSG_INVALIDA.
     *  5. No debe llevar usuario, parámetros (?) ni fragmento (#) -> MSG_EXTRAS.
     *  6. Puerto válido (1..65535 si viene) -> si no, MSG_INVALIDA.
     *  7. Con http, el host debe cumplir hostPermiteHttp(host) -> si no, MSG_HTTP_PUBLICO.
     *  8. Valida(url) con esquema y host en minúsculas, el puerto si venía, la ruta sin "/" repetidas al final y
     *     SIEMPRE terminada en "/".
     * Aceptación: validar("192.168.1.20:3000") -> Valida("http://192.168.1.20:3000/");
     * validar("https://MiServidor.dev/api") -> Valida("https://miservidor.dev/api/");
     * validar("http://miservidor.dev") -> Invalida(MSG_HTTP_PUBLICO); validar("ftp://x") -> Invalida(MSG_ESQUEMA);
     * validar("   ") -> Invalida(MSG_VACIA).
     */
    fun validar(entrada: String): Validacion =
        TODO("E3-1: validar y normalizar la URL del servidor")

    /** La dirección normalizada, o null si no es válida. */
    fun normalizar(entrada: String): String? = (validar(entrada) as? Validacion.Valida)?.url

    /** Dirección normalizada o, si no sirve, la del emulador. */
    fun normalizarOEmulador(entrada: String?): String = entrada?.let { normalizar(it) } ?: URL_EMULADOR

    /**
     * ¿Se puede usar http (sin cifrar) con este host? Sí para localhost, el emulador (10.0.2.2),
     * direcciones de bucle y rangos privados (10.x, 172.16-31.x, 192.168.x, 169.254.x), IPv6 local y
     * nombres terminados en ".local". (Ya está hecho: úsalo desde validar.)
     */
    fun hostPermiteHttp(hostEntrada: String): Boolean {
        val host = hostEntrada.trim().lowercase().removePrefix("[").removeSuffix("]")
        if (host == "localhost" || host.endsWith(".localhost") || host.endsWith(".local")) return true
        if (host == "::1" || host.startsWith("fe80:") || host.startsWith("fc") && host.contains(':') ||
            host.startsWith("fd") && host.contains(':')
        ) return true
        val partes = host.split('.')
        if (partes.size != 4) return false
        val n = partes.map { it.toIntOrNull() ?: return false }
        if (n.any { it !in 0..255 }) return false
        return n[0] == 127 || n[0] == 10 ||
            (n[0] == 172 && n[1] in 16..31) ||
            (n[0] == 192 && n[1] == 168) ||
            (n[0] == 169 && n[1] == 254)
    }
}
