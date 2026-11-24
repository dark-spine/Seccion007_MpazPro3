package com.mpazpro3.app.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken

/**
 * Operación que el estudiante hizo sin conexión y que se envía al servidor cuando vuelve la red.
 * Solo se encolan las que no ponen en riesgo una regla del servidor: marcar el material como
 * revisado y responder desafíos de práctica. La evaluación final (RN-03) siempre exige conexión.
 */
data class OperacionPendiente(
    val id: Long = 0L,
    val tipo: Tipo = Tipo.MATERIAL_REVISADO,
    val estudianteId: String = "",
    val contenidoId: String = "",
    val actividadId: String? = null,
    val alternativa: Int? = null
) {
    enum class Tipo { MATERIAL_REVISADO, RESPUESTA_DESAFIO }
}

interface ColaPendientes {
    fun todas(): List<OperacionPendiente>
    /** Agrega al final y devuelve la operación con su id asignado. */
    fun agregar(operacion: OperacionPendiente): OperacionPendiente
    fun quitar(id: Long)
}

/** Texto <-> lista de operaciones (JSON). Un texto dañado se interpreta como cola vacía. */
object SerializadorPendientes {
    private val gson = Gson()
    private val tipoLista = object : TypeToken<List<OperacionPendiente>>() {}.type

    fun aTexto(lista: List<OperacionPendiente>): String = gson.toJson(lista)

    fun deTexto(texto: String?): List<OperacionPendiente> {
        if (texto.isNullOrBlank()) return emptyList()
        return try {
            val leida: List<OperacionPendiente>? = gson.fromJson(texto, tipoLista)
            leida.orEmpty().filter { it.estudianteId.isNotBlank() && it.contenidoId.isNotBlank() }
        } catch (e: JsonParseException) {
            emptyList()
        }
    }
}

/** Cola en memoria (pruebas). */
class ColaPendientesMemoria : ColaPendientes {
    private val lista = mutableListOf<OperacionPendiente>()
    private var siguienteId = 1L

    @Synchronized
    override fun todas(): List<OperacionPendiente> = lista.toList()

    @Synchronized
    override fun agregar(operacion: OperacionPendiente): OperacionPendiente {
        val guardada = operacion.copy(id = siguienteId++)
        lista += guardada
        return guardada
    }

    @Synchronized
    override fun quitar(id: Long) {
        lista.removeAll { it.id == id }
    }
}

/** Cola persistente en SharedPreferences: sobrevive a cerrar la app. */
class ColaPendientesPrefs(contexto: Context) : ColaPendientes {
    private val prefs = contexto.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    @Synchronized
    override fun todas(): List<OperacionPendiente> = SerializadorPendientes.deTexto(prefs.getString(CLAVE, null))

    @Synchronized
    override fun agregar(operacion: OperacionPendiente): OperacionPendiente {
        val actuales = todas()
        val siguiente = (actuales.maxOfOrNull { it.id } ?: 0L) + 1L
        val guardada = operacion.copy(id = siguiente)
        prefs.edit().putString(CLAVE, SerializadorPendientes.aTexto(actuales + guardada)).apply()
        return guardada
    }

    @Synchronized
    override fun quitar(id: Long) {
        val restantes = todas().filter { it.id != id }
        prefs.edit().putString(CLAVE, SerializadorPendientes.aTexto(restantes)).apply()
    }

    private companion object {
        const val ARCHIVO = "mpazpro3_pendientes"
        const val CLAVE = "cola"
    }
}
