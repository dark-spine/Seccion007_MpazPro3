package com.mpazpro3.app.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Asignación que hace el docente a su curso: qué contenido (ya autorizado por UTP) trabajará el
 * curso y cuántos intentos tiene la evaluación final.
 *
 * Regla de negocio: el máximo de intentos lo define el docente y debe respetarse estrictamente.
 */
@Entity(
    tableName = "asignaciones",
    foreignKeys = [
        ForeignKey(
            entity = Contenido::class,
            parentColumns = ["id"],
            childColumns = ["contenidoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("contenidoId"),
        Index(value = ["curso", "contenidoId"], unique = true)
    ]
)
data class Asignacion(
    @PrimaryKey val id: String,
    val curso: String,
    val contenidoId: String,
    val maxIntentosEvaluacion: Int
)