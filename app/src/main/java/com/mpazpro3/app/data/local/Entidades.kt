package com.mpazpro3.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * Entidades de Room: una por tabla del modelo relacional del caso.
 *
 * Los enums (perfil, asignatura, estado, dificultad) se guardan como texto con enum.name, no con
 * su etiqueta legible, para que cambiar una etiqueta no rompa la base de datos. Las listas del
 * dominio (alternativas, respuestas) van en su propia tabla.
 */

@Entity(tableName = "usuarios", indices = [Index(value = ["usuario"], unique = true)])
data class UsuarioEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val usuario: String,
    val claveHash: String,
    val perfil: String,
    val curso: String?
)

@Entity(tableName = "estudiantes")
data class EstudianteEntity(
    @PrimaryKey val id: String,
    val nombreFicticio: String,
    val curso: String
)

@Entity(tableName = "contenidos")
data class ContenidoEntity(
    @PrimaryKey val id: String,
    val nivel: String,
    val asignatura: String,
    val unidad: String,
    val objetivoAprendizaje: String,
    val codigoOA: String?,
    val ejeCurricular: String,
    val recursoReferencia: String,
    val estado: String,
    val observacionUtp: String
)

@Entity(
    tableName = "recursos",
    foreignKeys = [
        ForeignKey(
            entity = ContenidoEntity::class,
            parentColumns = ["id"],
            childColumns = ["contenidoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contenidoId")]
)
data class RecursoEntity(
    @PrimaryKey val id: String,
    val contenidoId: String,
    val titulo: String,
    val explicacion: String,
    val ejemplo: String,
    val instrucciones: String,
    val descripcionImagen: String?,
    val audioDisponible: Boolean
)

@Entity(
    tableName = "actividades",
    foreignKeys = [
        ForeignKey(
            entity = ContenidoEntity::class,
            parentColumns = ["id"],
            childColumns = ["contenidoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contenidoId")]
)
data class ActividadEntity(
    @PrimaryKey val id: String,
    val contenidoId: String,
    val enunciado: String,
    val dificultad: String,
    val orden: Int,
    val esEvaluacionFinal: Boolean
)

@Entity(
    tableName = "alternativas",
    foreignKeys = [
        ForeignKey(
            entity = ActividadEntity::class,
            parentColumns = ["id"],
            childColumns = ["actividadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("actividadId")]
)
data class AlternativaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actividadId: String,
    val texto: String,
    val esCorrecta: Boolean,
    val posicion: Int
)

/** Un contenido se asigna a un curso una sola vez: el par (curso, contenidoId) es único. */
@Entity(
    tableName = "asignaciones",
    foreignKeys = [
        ForeignKey(
            entity = ContenidoEntity::class,
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
data class AsignacionEntity(
    @PrimaryKey val id: String,
    val curso: String,
    val contenidoId: String,
    val maxIntentosEvaluacion: Int
)

/** Un estudiante tiene un único seguimiento por contenido: la clave primaria es compuesta. */
@Entity(
    tableName = "seguimientos",
    primaryKeys = ["estudianteId", "contenidoId"],
    foreignKeys = [
        ForeignKey(
            entity = EstudianteEntity::class,
            parentColumns = ["id"],
            childColumns = ["estudianteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ContenidoEntity::class,
            parentColumns = ["id"],
            childColumns = ["contenidoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contenidoId")]
)
data class SeguimientoEntity(
    val estudianteId: String,
    val contenidoId: String,
    val materialRevisado: Boolean,
    val intentosEvaluacionUsados: Int,
    val estado: String
)

/** contenidoId se guarda también aquí para consultar las respuestas de un contenido sin cruces. */
@Entity(
    tableName = "respuestas",
    foreignKeys = [
        ForeignKey(
            entity = EstudianteEntity::class,
            parentColumns = ["id"],
            childColumns = ["estudianteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ActividadEntity::class,
            parentColumns = ["id"],
            childColumns = ["actividadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["estudianteId", "contenidoId"]),
        Index("actividadId")
    ]
)
data class RespuestaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val estudianteId: String,
    val actividadId: String,
    val contenidoId: String,
    val alternativaElegida: Int,
    val esCorrecta: Boolean,
    val numeroIntento: Int,
    val fecha: Long
)

/** Hay un informe por estudiante. fortalezas y aspectosAReforzar se guardan separados por salto de línea. */
@Entity(
    tableName = "informes",
    foreignKeys = [
        ForeignKey(
            entity = EstudianteEntity::class,
            parentColumns = ["id"],
            childColumns = ["estudianteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["estudianteId"], unique = true)]
)
data class InformeEntity(
    @PrimaryKey val id: String,
    val estudianteId: String,
    val rendimiento: String,
    val fortalezas: String,
    val aspectosAReforzar: String,
    val fecha: String,
    val estado: String
)
