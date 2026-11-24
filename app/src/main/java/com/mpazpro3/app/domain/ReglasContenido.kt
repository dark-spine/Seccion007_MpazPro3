package com.mpazpro3.app.domain

import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.Resultado

/**
 * Reglas de autorización de contenidos por UTP (RF-01, RF-02, RN-02, RN-04) como funciones
 * PURAS, sin Android ni Room.
 */
object ReglasContenido {

    const val MENSAJE_MOTIVO_OBLIGATORIO = "Indica el motivo del rechazo"
    const val MENSAJE_UNIDAD_YA_APROBADA = "Ya existe una unidad aprobada para esta asignatura."
    const val MENSAJE_CONTENIDO_NO_ENCONTRADO = "No se encontró el contenido."
    const val MENSAJE_YA_ASIGNADO =
        "Este contenido ya está asignado a un curso: no se puede rechazar mientras siga asignado."

    /** Error del campo "Observación de UTP" al rechazar (null = sin error). */
    fun errorObservacionAlRechazar(observacion: String): String? =
        if (observacion.isBlank()) MENSAJE_MOTIVO_OBLIGATORIO else null

    /**
     * RN-04: máximo una unidad aprobada por asignatura. Aprobar es posible si ningún OTRO
     * contenido de la misma asignatura está aprobado.
     */
    fun validarAprobacion(contenido: Contenido?, todos: List<Contenido>): Resultado {
        if (contenido == null) return Resultado.error(MENSAJE_CONTENIDO_NO_ENCONTRADO)
        val hayOtraAprobada = todos.any {
            it.id != contenido.id &&
                it.asignatura == contenido.asignatura &&
                it.estado == EstadoAprobacion.APROBADO
        }
        return if (hayOtraAprobada) Resultado.error(MENSAJE_UNIDAD_YA_APROBADA) else Resultado.ok("Contenido aprobado")
    }

    /**
     * RF-01 / RN-02: rechazar exige un motivo, y un contenido ya asignado a un curso no se
     * puede rechazar (el curso perdería material que ya estaba trabajando).
     */
    fun validarRechazo(contenido: Contenido?, observacion: String, asignaciones: List<Asignacion>): Resultado {
        if (contenido == null) return Resultado.error(MENSAJE_CONTENIDO_NO_ENCONTRADO)
        errorObservacionAlRechazar(observacion)?.let { return Resultado.error(it) }
        val estaAsignado = asignaciones.any { it.contenidoId == contenido.id }
        return if (estaAsignado) Resultado.error(MENSAJE_YA_ASIGNADO) else Resultado.ok("Contenido rechazado")
    }

    /**
     * RF-02: filtra por asignatura y por estado (null = todos) y deja primero los PENDIENTES,
     * luego por asignatura.
     */
    fun filtrar(
        contenidos: List<Contenido>,
        asignatura: Asignatura?,
        estado: EstadoAprobacion?
    ): List<Contenido> = contenidos
        .filter { (asignatura == null || it.asignatura == asignatura) && (estado == null || it.estado == estado) }
        .sortedWith(
            compareBy<Contenido>({ if (it.estado == EstadoAprobacion.PENDIENTE) 0 else 1 }, { it.asignatura.ordinal })
        )

    /** RF-03 y RN-02: solo los contenidos APROBADOS por UTP están disponibles para asignar. */
    fun disponiblesParaAsignar(contenidos: List<Contenido>): List<Contenido> =
        contenidos.filter { it.estado == EstadoAprobacion.APROBADO }
            .sortedBy { it.asignatura.ordinal }

    /**
     * RF-07 y RN-02: contenidos asignados al curso que siguen APROBADOS por UTP. Si UTP rechaza
     * un contenido después de asignado, deja de mostrarse al estudiante.
     */
    fun asignadosAlCurso(
        contenidos: List<Contenido>,
        asignaciones: List<Asignacion>,
        curso: String?
    ): List<Contenido> {
        if (curso.isNullOrBlank()) return emptyList()
        val ids = asignaciones.filter { it.curso == curso }.map { it.contenidoId }.toSet()
        return contenidos
            .filter { it.id in ids && it.estado == EstadoAprobacion.APROBADO }
            .sortedBy { it.asignatura.ordinal }
    }
}
