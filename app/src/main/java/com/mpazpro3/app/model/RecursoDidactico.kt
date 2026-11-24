package com.mpazpro3.app.model

/**
 * Recurso didáctico de un contenido (entidad del caso): título, explicación, ejemplo,
 * instrucciones, imagen y audio de apoyo.
 *
 * RNF-02: el audio es un complemento; no reemplaza la lectura autónoma (todo recurso con audio
 * trae también su explicación escrita). RNF-05: los recursos son propios del proyecto.
 */
data class RecursoDidactico(
    val id: String,
    val contenidoId: String,
    val titulo: String,
    val explicacion: String,
    val ejemplo: String,
    val instrucciones: String,
    val descripcionImagen: String? = null,
    val audioDisponible: Boolean = false
)
