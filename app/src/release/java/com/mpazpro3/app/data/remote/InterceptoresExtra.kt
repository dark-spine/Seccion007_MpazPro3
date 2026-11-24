package com.mpazpro3.app.data.remote

import okhttp3.Interceptor

/** En el build release no se agrega ningún registro de peticiones. */
object InterceptoresExtra {
    fun lista(): List<Interceptor> = emptyList()
}
