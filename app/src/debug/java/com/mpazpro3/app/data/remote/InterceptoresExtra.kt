package com.mpazpro3.app.data.remote

import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Interceptores que solo existen en el build debug. Registra método, ruta y código de respuesta
 * (nivel BASIC: no escribe cuerpos, así que las claves no quedan en el Logcat) y oculta el token.
 */
object InterceptoresExtra {
    fun lista(): List<Interceptor> = listOf(
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
            redactHeader("Authorization")
        }
    )
}
