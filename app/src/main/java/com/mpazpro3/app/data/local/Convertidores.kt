package com.mpazpro3.app.data.local

import androidx.room.TypeConverter
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.EstadoAvance
import com.mpazpro3.app.model.Perfil

/**
 * Room solo guarda tipos simples (texto, números, booleanos). Los enums del modelo
 * se traducen a texto al guardar y se reconstruyen al leer.
 *
 * Cada par de funciones es un convertidor: una de ida y otra de vuelta.
 * Se guarda enum.name (por ejemplo "APROBADO"), no la etiqueta legible,
 * porque la etiqueta puede cambiar sin que se rompa la base de datos.
 */
class Convertidores {

    @TypeConverter
    fun perfilATexto(valor: Perfil): String = valor.name

    @TypeConverter
    fun textoAPerfil(valor: String): Perfil = Perfil.valueOf(valor)

    @TypeConverter
    fun asignaturaATexto(valor: Asignatura): String = valor.name

    @TypeConverter
    fun textoAAsignatura(valor: String): Asignatura = Asignatura.valueOf(valor)

    @TypeConverter
    fun estadoATexto(valor: EstadoAprobacion): String = valor.name

    @TypeConverter
    fun textoAEstado(valor: String): EstadoAprobacion = EstadoAprobacion.valueOf(valor)

    @TypeConverter
    fun avanceATexto(valor: EstadoAvance): String = valor.name

    @TypeConverter
    fun textoAAvance(valor: String): EstadoAvance = EstadoAvance.valueOf(valor)
}