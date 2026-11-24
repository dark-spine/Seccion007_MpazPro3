package com.mpazpro3.app.data

import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.Estudiante
import com.mpazpro3.app.model.InformeDocente
import com.mpazpro3.app.model.RecursoDidactico
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario

/** Copia completa de lo que el servidor entrega al perfil activo (lo que se guarda en Room). */
data class Instantanea(
    val contenidos: List<Contenido> = emptyList(),
    val recursos: List<RecursoDidactico> = emptyList(),
    val actividades: List<Actividad> = emptyList(),
    val asignaciones: List<Asignacion> = emptyList(),
    val estudiantes: List<Estudiante> = emptyList(),
    val seguimientos: List<SeguimientoAvance> = emptyList(),
    val informes: List<InformeDocente> = emptyList()
) {
    /**
     * Descarta lo que haría fallar las claves foráneas de la base (por ejemplo, un seguimiento de un
     * contenido que este perfil no ve). Así guardar la copia nunca rompe por datos sueltos.
     */
    fun coherente(): Instantanea {
        val idsContenido = contenidos.map { it.id }.toSet()
        val idsEstudiante = estudiantes.map { it.id }.toSet()
        val idsActividad = actividades.filter { it.contenidoId in idsContenido }.map { it.id }.toSet()
        return copy(
            recursos = recursos.filter { it.contenidoId in idsContenido },
            actividades = actividades.filter { it.contenidoId in idsContenido },
            asignaciones = asignaciones.filter { it.contenidoId in idsContenido },
            seguimientos = seguimientos
                .filter { it.estudianteId in idsEstudiante && it.contenidoId in idsContenido }
                .map { s -> s.copy(respuestas = s.respuestas.filter { it.actividadId in idsActividad }) },
            informes = informes.filter { it.estudianteId in idsEstudiante }
        )
    }
}

/** Resultado de registrar localmente una respuesta: si fue correcta y a qué contenido pertenece. */
data class RespuestaLocal(val esCorrecta: Boolean, val contenidoId: String)

/**
 * Copia local de los datos (Room en la app). La API es la fuente de verdad: esta copia se
 * actualiza con cada lectura o escritura que sale bien y sirve de respaldo cuando no hay conexión.
 */
interface CacheLocal {
    /** Reemplaza todo el contenido de la copia, de forma atómica. */
    suspend fun reemplazarTodo(instantanea: Instantanea)

    /** Actualiza estado y observación de un contenido sin tocar lo que depende de él. */
    suspend fun guardarContenido(contenido: Contenido)

    /** Reemplaza las asignaciones de un curso. */
    suspend fun guardarAsignaciones(curso: String, asignaciones: List<Asignacion>)

    /** Reemplaza el seguimiento de un estudiante en un contenido junto con todas sus respuestas. */
    suspend fun guardarSeguimiento(seguimiento: SeguimientoAvance)

    suspend fun guardarInforme(informe: InformeDocente)

    /** Guarda al usuario que ingresó, para poder ingresar sin conexión la próxima vez. */
    suspend fun guardarUsuario(usuario: Usuario)

    /** Ingreso sin conexión: usuario ya normalizado y SHA-256 de la clave. */
    suspend fun autenticarLocal(usuarioNormalizado: String, claveHash: String): Usuario?

    /**
     * Registro provisional de una respuesta a un desafío hecha sin conexión. Devuelve null si la
     * actividad, la alternativa o la asignación no son válidas (en ese caso no se registra nada).
     */
    suspend fun registrarRespuestaLocal(estudianteId: String, actividadId: String, alternativa: Int): RespuestaLocal?

    /** Marca el material como revisado de forma provisional. Devuelve false si el contenido no está asignado. */
    suspend fun marcarMaterialLocal(estudianteId: String, contenidoId: String): Boolean
}
