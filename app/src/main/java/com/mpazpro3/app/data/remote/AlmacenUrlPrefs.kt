package com.mpazpro3.app.data.remote

import android.content.Context

/** Guarda la dirección del servidor en SharedPreferences (sobrevive a cerrar la app). */
class AlmacenUrlPrefs(contexto: Context) : AlmacenUrl {
    private val prefs = contexto.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    override fun leer(): String? = prefs.getString(CLAVE, null)

    override fun guardar(url: String?) {
        if (url == null) prefs.edit().remove(CLAVE).apply() else prefs.edit().putString(CLAVE, url).apply()
    }

    private companion object {
        const val ARCHIVO = "mpazpro3_servidor"
        const val CLAVE = "url"
    }
}
