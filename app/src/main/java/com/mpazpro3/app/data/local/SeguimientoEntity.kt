package com.mpazpro3.app.data.local

import androidx.room.Entity
import com.mpazpro3.app.model.EstadoAvance

/**
 * Seguimiento del avance de un estudiante en un contenido (RF-11).
 *
 * Por qué es una entidad aparte y no se anota el modelo SeguimientoAvance:
 * ese modelo contiene una List<RespuestaEstudiante> adentro, y Room no puede
 * guardar una lista en una columna. Las respuestas irán en su propia tabla
 * cuando se implementen los desafíos y la evaluación (RF-08 y RF-09).
 *
 * La clave primaria es compuesta: un estudiante tiene un único seguimiento por
 * contenido, así que la combinación (estudianteId, contenidoId) no se repite.
 */
@Entity(
    tableName = "seguimientos",
    primaryKeys = ["estudianteId", "contenidoId"]
)
data class SeguimientoEntity(
    val estudianteId: String,
    val contenidoId: String,
    val intentosEvaluacionUsados: Int = 0,
    val estado: EstadoAvance = EstadoAvance.NO_INICIADO
)