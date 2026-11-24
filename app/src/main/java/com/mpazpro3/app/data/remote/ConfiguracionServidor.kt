package com.mpazpro3.app.data.remote

/** Dónde se guarda la dirección elegida por el usuario (SharedPreferences en la app; memoria en pruebas). */
interface AlmacenUrl {
    fun leer(): String?
    fun guardar(url: String?)
}

/**
 * Dirección del servidor: la que el usuario guardó en la pantalla "Servidor" o, si no hay,
 * la de API_BASE_URL (gradle.properties).
 */
class ConfiguracionServidor(
    private val almacen: AlmacenUrl,
    private val urlPorDefecto: String
) {
    /** URL por defecto normalizada (o la del emulador si API_BASE_URL no es válida). */
    val porDefecto: String get() = UrlServidor.normalizarOEmulador(urlPorDefecto)

    /** URL en uso (siempre normalizada y válida). */
    val url: String get() = UrlServidor.normalizar(almacen.leer().orEmpty()) ?: porDefecto

    /**
     * TODO(E3-10a) Valida y guarda la dirección que escribió el usuario.
     *
     * Archivo: este. Qué hacer: validar con UrlServidor.validar(entrada); si es Valida, guardarla en el
     * [almacen] (almacen.guardar(url normalizada)); devolver siempre la validación (Valida con la URL
     * normalizada o Invalida con el mensaje). Una dirección inválida NO se guarda.
     * Aceptación: guardar("192.168.1.20:3000") -> Valida("http://192.168.1.20:3000/") y url pasa a ser esa;
     * guardar("ftp://x") -> Invalida y url no cambia.
     */
    fun guardar(entrada: String): UrlServidor.Validacion =
        TODO("E3-10a: validar y guardar la URL del servidor")

    /**
     * TODO(E3-10b) Vuelve a la dirección de API_BASE_URL.
     *
     * Qué hacer: borrar lo guardado (almacen.guardar(null)) y devolver [porDefecto].
     * Aceptación: después de restablecer(), url == porDefecto.
     */
    fun restablecer(): String =
        TODO("E3-10b: borrar la URL guardada y devolver la dirección por defecto")
}
