package com.mpazpro3.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Interruptor "Modo local / Modo servidor". Es una preferencia PERSISTENTE que se lee en cada
 * operación (no solo al arrancar), así que cambiarla surte efecto en la acción siguiente.
 */
interface PreferenciaModo {
    /** true = modo local (solo Room, sin servidor). Se consulta en cada operación. */
    val modoLocal: Boolean

    /** El mismo valor como flujo, para que la interfaz (franja, diálogo) se actualice sola. */
    val modoLocalFlujo: StateFlow<Boolean>

    fun cambiar(local: Boolean)
}

/**
 * Guarda el modo en SharedPreferences. [inicialLocal] solo vale mientras el usuario no haya elegido
 * un modo: después manda lo guardado.
 */
class PreferenciaModoPrefs(contexto: Context, private val inicialLocal: Boolean) : PreferenciaModo {
    private val prefs = contexto.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    override val modoLocal: Boolean
        get() = prefs.getBoolean(CLAVE, inicialLocal)

    private val _flujo = MutableStateFlow(modoLocal)
    override val modoLocalFlujo: StateFlow<Boolean> = _flujo.asStateFlow()

    override fun cambiar(local: Boolean) {
        prefs.edit().putBoolean(CLAVE, local).apply()
        _flujo.value = local
    }

    private companion object {
        const val ARCHIVO = "mpazpro3_modo"
        const val CLAVE = "modoLocal"
    }
}
