package com.mpazpro3.app.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Recurso didáctico de un contenido (entidad del caso): título, explicación, ejemplo,
 * instrucciones, imagen y audio de apoyo.
 *
 * RNF: el audio es un complemento; no reemplaza la lectura autónoma.
 * Los contenidos deben ser propios, públicos o autorizados (no copiar material protegido).
 */
@Entity(
    tableName = "recursos",
    foreignKeys = [
        ForeignKey(
            entity = Contenido::class,
            parentColumns = ["id"],
            childColumns = ["contenidoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contenidoId")]
)
data class RecursoDidactico(
    @PrimaryKey val id: String,
    val contenidoId: String,
    val titulo: String,
    val explicacion: String,
    val ejemplo: String,
    val instrucciones: String,
    val descripcionImagen: String? = null,
    val audioDisponible: Boolean = false
)