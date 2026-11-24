package com.mpazpro3.app.ui.components

import com.mpazpro3.app.R

/**
 * Relaciona cada recurso didáctico con su ilustración (res/drawable) y su audio (res/raw).
 *
 * Se resuelve aquí y no en la base de datos porque los identificadores R.* se generan al
 * compilar y no tiene sentido guardarlos en una tabla. Para agregar material nuevo basta con
 * sumar una línea al when correspondiente. Todo el material es propio del proyecto (RNF-05).
 *
 * Audio de apoyo: `res/raw/centenas_explicacion.wav` es una señal de ejemplo generada por código (tonos que
 * "cuentan" unidades, decenas y centenas), sin voz. Para usar tu propia explicación grabada, reemplaza el archivo
 * conservando el nombre base "centenas_explicacion" (puede ser .wav, .mp3, .ogg o .m4a) y asegúrate de que
 * en res/raw quede UN solo archivo con ese nombre: Android no admite dos recursos con el mismo nombre base.
 */
fun imagenDelRecurso(recursoId: String): Int? = when (recursoId) {
    "R1" -> R.drawable.bloques_base_diez
    "R2" -> R.drawable.ilustracion_lectura
    "R3" -> R.drawable.ilustracion_seres_vivos
    "R4" -> R.drawable.ilustracion_rosa_vientos
    else -> null
}

fun audioDelRecurso(recursoId: String): Int? = when (recursoId) {
    "R1" -> R.raw.centenas_explicacion
    else -> null
}
