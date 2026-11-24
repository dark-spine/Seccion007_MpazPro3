package com.mpazpro3.app.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** ¿Hay alguna red con salida a internet o a la red local? Permite avisar "sin conexión" sin esperar un tiempo de espera. */
interface Conectividad {
    fun hayRed(): Boolean
}

/** Usa ACCESS_NETWORK_STATE. No exige que la red tenga internet validado: una red local sin salida también sirve. */
class ConectividadAndroid(private val contexto: Context) : Conectividad {
    override fun hayRed(): Boolean {
        val gestor = contexto.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
        val red = gestor.activeNetwork ?: return false
        val capacidades = gestor.getNetworkCapabilities(red) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
