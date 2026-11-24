package com.mpazpro3.app.data.remote

import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * TODO(E3-2a) Agrega "Authorization: Bearer <token>" a cada petición cuando hay una sesión activa.
 *
 * Archivo: este. Qué hacer: leer sesion.token; si no está vacío y la petición todavía no trae el
 * encabezado Authorization, copiar la petición (request.newBuilder().header(...).build()) y continuar con
 * chain.proceed(...). Sin token, enviar la petición tal cual (el login y /health no llevan token).
 * Aceptación: con sesion.token = "abc" la petición sale con "Authorization: Bearer abc"; sin token no
 * lleva el encabezado; un encabezado Authorization ya presente (logout) no se pisa.
 */
class TokenInterceptor(private val sesion: SesionApi) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        TODO("E3-2a: agregar el encabezado Authorization con el token de la sesión")
}

/**
 * Impide enviar tráfico sin cifrar (http) hacia servidores que no sean locales. Complementa a
 * network_security_config, que no puede expresar rangos de red privados. (Ya está hecho.)
 */
class ProteccionHttpInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url
        if (!url.isHttps && !UrlServidor.hostPermiteHttp(url.host)) {
            throw ConexionNoPermitidaException(UrlServidor.MSG_HTTP_PUBLICO)
        }
        return chain.proceed(chain.request())
    }
}

/** Construye el ApiService de Retrofit para una URL base. */
object ApiClient {

    const val TIEMPO_CONEXION_SEG = 10L
    const val TIEMPO_LECTURA_SEG = 20L

    /**
     * TODO(E3-2b) Cliente OkHttp de la app.
     *
     * Qué hacer: OkHttpClient.Builder() con connectTimeout = conexionSeg, readTimeout = lecturaSeg y
     * writeTimeout = lecturaSeg (TimeUnit.SECONDS); agregar, en este orden, ProteccionHttpInterceptor(),
     * TokenInterceptor(sesion) y después los [extras] (en debug traen el registro de peticiones).
     * Aceptación: client.connectTimeoutMillis == conexionSeg * 1000; con un servidor que no responde, la
     * llamada falla por tiempo agotado y no se queda esperando para siempre.
     */
    fun crearOkHttp(
        sesion: SesionApi,
        extras: List<Interceptor> = emptyList(),
        conexionSeg: Long = TIEMPO_CONEXION_SEG,
        lecturaSeg: Long = TIEMPO_LECTURA_SEG
    ): OkHttpClient = TODO("E3-2b: OkHttpClient con timeouts e interceptores")

    /** baseUrl debe estar normalizada (termina en "/"); ver UrlServidor.normalizar. (Ya está hecho.) */
    fun crearServicio(
        baseUrl: String,
        sesion: SesionApi,
        extras: List<Interceptor> = emptyList(),
        conexionSeg: Long = TIEMPO_CONEXION_SEG,
        lecturaSeg: Long = TIEMPO_LECTURA_SEG
    ): ApiService = crearServicio(baseUrl, crearOkHttp(sesion, extras, conexionSeg, lecturaSeg))

    /**
     * TODO(E3-2c) Instancia de Retrofit y de ApiService.
     *
     * Qué hacer: Retrofit.Builder().baseUrl(baseUrl).client(cliente).addConverterFactory(GsonConverterFactory.create())
     * .build().create(ApiService::class.java).
     * Aceptación: con MockWebServer, api.salud() hace GET /health y lee el JSON {"estado":"ok",...}.
     */
    fun crearServicio(baseUrl: String, cliente: OkHttpClient): ApiService =
        TODO("E3-2c: construir Retrofit con Gson y crear el ApiService")
}

/**
 * Entrega el ApiService para la URL configurada en este momento. Si el usuario cambia el servidor
 * desde la pantalla "Servidor", la siguiente llamada ya usa la nueva dirección (sin recompilar).
 * (Ya está hecho.)
 */
class ProveedorApi(
    private val urlActual: () -> String,
    private val fabrica: (String) -> ApiService
) {
    private var urlCreada: String? = null
    private var servicio: ApiService? = null

    @Synchronized
    fun obtener(): ApiService {
        val url = UrlServidor.normalizarOEmulador(urlActual())
        val existente = servicio
        if (existente != null && url == urlCreada) return existente
        return fabrica(url).also {
            servicio = it
            urlCreada = url
        }
    }
}
