package com.mpazpro3.app.data

import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.Estudiante
import com.mpazpro3.app.model.InformeDocente
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario

/** Lo que RepositorioLocal necesita leer para decidir (todo ya convertido al modelo del dominio). */
data class DatosLocales(
    val contenidos: List<Contenido> = emptyList(),
    val actividades: List<Actividad> = emptyList(),
    val asignaciones: List<Asignacion> = emptyList(),
    val estudiantes: List<Estudiante> = emptyList(),
    val seguimientos: List<SeguimientoAvance> = emptyList(),
    val informes: List<InformeDocente> = emptyList()
)

/**
 * Base del modo local (Room en la app; una copia en memoria en las pruebas). Solo sabe LEER y
 * GUARDAR: las decisiones (reglas de negocio) las toma RepositorioLocal con las funciones puras de domain/.
 */
interface AlmacenLocal {
    /** Ejecuta el bloque como una unidad atómica (leer, decidir y escribir sin que otra acción se intercale). */
    suspend fun <T> transaccion(bloque: suspend () -> T): T

    suspend fun leer(): DatosLocales

    /** Usuario cuyo nombre (ya normalizado) y SHA-256 de la clave coinciden; null si no hay. */
    suspend fun autenticar(usuarioNormalizado: String, claveHash: String): Usuario?

    /** Cambia estado y observación de UTP de un contenido sin tocar lo que depende de él. */
    suspend fun actualizarContenido(contenidoId: String, estado: EstadoAprobacion, observacion: String)

    /** Deja como asignaciones del curso exactamente las de la lista. */
    suspend fun reemplazarAsignacionesDelCurso(curso: String, asignaciones: List<Asignacion>)

    /** Guarda el seguimiento de un estudiante en un contenido junto con TODAS sus respuestas. */
    suspend fun guardarSeguimiento(seguimiento: SeguimientoAvance)

    /** Guarda (inserta o reemplaza) el informe de un estudiante. */
    suspend fun guardarInforme(informe: InformeDocente)

    /** Borra lo creado en modo local y vuelve a cargar los datos de ejemplo. */
    suspend fun restablecer()
}
