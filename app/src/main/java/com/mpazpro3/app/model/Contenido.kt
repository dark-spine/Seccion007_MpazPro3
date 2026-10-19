package com.mpazpro3.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EstadoAprobacion(val etiqueta: String) {
    PENDIENTE("Pendiente de revisión"),
    APROBADO("Aprobado por UTP"),
    RECHAZADO("Rechazado por UTP")
}

/** Las cuatro asignaturas del caso (máximo una unidad por asignatura en el MVP). */
enum class Asignatura(val etiqueta: String) {
    LENGUAJE("Lenguaje y Comunicación"),
    MATEMATICA("Matemática"),
    CIENCIAS_NATURALES("Ciencias Naturales"),
    HISTORIA("Historia, Geografía y Cs. Sociales")
}

/**
 * Contenido curricular (entidad del caso): nivel, asignatura, unidad, Objetivo de Aprendizaje,
 * código oficial, eje curricular, recurso de referencia y estado de aprobación de UTP.
 *
 * Regla de negocio: un contenido que no está APROBADO no puede ser asignado por el docente.
 */
@Entity(tableName = "contenidos")
data class Contenido(
    @PrimaryKey val id: String,
    val nivel: String = "3° básico",
    val asignatura: Asignatura,
    val unidad: String,
    val objetivoAprendizaje: String,
    val codigoOA: String? = null,
    val ejeCurricular: String,
    val recursoReferencia: String,
    val estado: EstadoAprobacion = EstadoAprobacion.PENDIENTE,
    val observacionUtp: String = ""
)