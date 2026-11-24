package com.mpazpro3.app.data.local

import com.mpazpro3.app.data.CacheLocal
import com.mpazpro3.app.data.Instantanea
import com.mpazpro3.app.data.RespuestaLocal
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.InformeDocente
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario

/**
 * Copia local de los datos del servidor en Room. Sirve de caché de lectura (la interfaz observa
 * estas tablas) y de respaldo cuando no hay conexión.
 *
 * Ya existen, para que los uses: los DAO con las consultas de borrado (borrarTodos, borrarTodas,
 * borrarAlternativas, borrarActividades, borrarDelCurso, borrarDe), los mapeos entidad <-> modelo
 * (aEntidad, aDominio, alternativasAEntidad, respuestasAEntidad) y db.withTransaction { }.
 *
 * Cuidado con las claves foráneas: borrar o reemplazar una fila de "contenidos" borra en cascada sus
 * recursos, actividades, asignaciones y seguimientos. Por eso los contenidos ya existentes se actualizan
 * con actualizarEstado (UPDATE) y no con insertarTodos (REPLACE).
 */
class CacheRoom(private val db: AppDatabase) : CacheLocal {

    /**
     * TODO(E3-6a) Reemplaza todo el contenido de la copia, de forma atómica.
     *
     * Archivo: este. Qué hacer, dentro de UNA transacción (db.withTransaction): borrar primero los hijos
     * (respuestas, seguimientos, informes, asignaciones, alternativas, actividades, recursos) y luego contenidos
     * y estudiantes; después insertar, en este orden: estudiantes, contenidos, recursos, actividades, alternativas,
     * asignaciones, seguimientos, respuestas e informes de la [instantanea].
     * Aceptación: si un insert falla a la mitad, la copia anterior queda intacta (transacción); después de
     * reemplazarTodo las tablas contienen exactamente lo recibido (ni más ni menos).
     */
    override suspend fun reemplazarTodo(instantanea: Instantanea) {
        TODO("E3-6a: borrar e insertar todas las tablas dentro de una transacción")
    }

    /**
     * TODO(E3-6b) Actualiza estado y observación de un contenido sin tocar lo que depende de él.
     *
     * Qué hacer: dentro de una transacción, si el contenido no existe en Room insertarlo; si existe,
     * db.contenidoDao().actualizarEstado(id, estado.name, observacionUtp).
     * Aceptación: aprobar un contenido asignado no borra sus recursos, actividades ni asignaciones.
     */
    override suspend fun guardarContenido(contenido: Contenido) {
        TODO("E3-6b: actualizar el estado del contenido con UPDATE (no REPLACE)")
    }

    /**
     * TODO(E3-6c) Reemplaza las asignaciones de un curso.
     *
     * Qué hacer: en una transacción, borrar las del curso (borrarDelCurso) e insertar las recibidas cuyo
     * contenido exista en Room (si no, la clave foránea fallaría).
     * Aceptación: reasignar con otro máximo de intentos deja una sola fila por contenido y curso.
     */
    override suspend fun guardarAsignaciones(curso: String, asignaciones: List<Asignacion>) {
        TODO("E3-6c: reemplazar las asignaciones del curso")
    }

    /**
     * TODO(E3-6d) Reemplaza el seguimiento de un estudiante en un contenido junto con todas sus respuestas.
     *
     * Qué hacer: en una transacción, si existen el estudiante y el contenido: guardar el seguimiento, borrar sus
     * respuestas (respuestaDao().borrarDe) e insertar las recibidas cuya actividad exista.
     * Aceptación: guardar dos veces el mismo seguimiento no duplica respuestas.
     */
    override suspend fun guardarSeguimiento(seguimiento: SeguimientoAvance) {
        TODO("E3-6d: reemplazar seguimiento y respuestas")
    }

    /**
     * TODO(E3-6e) Guarda (inserta o reemplaza) un informe si existe el estudiante al que pertenece.
     * Aceptación: validar un informe cambia su estado en Room sin crear un segundo informe.
     */
    override suspend fun guardarInforme(informe: InformeDocente) {
        TODO("E3-6e: guardar el informe en Room")
    }

    /**
     * TODO(E3-8a) Guarda al usuario que ingresó, para poder ingresar sin conexión la próxima vez.
     * Qué hacer: db.usuarioDao().insertarTodos(listOf(usuario.aEntidad())).
     */
    override suspend fun guardarUsuario(usuario: Usuario) {
        TODO("E3-8a: guardar el usuario en Room")
    }

    /**
     * TODO(E3-8b) Ingreso sin conexión: usuario ya normalizado (minúsculas, sin espacios) y SHA-256 de la clave.
     * Qué hacer: db.usuarioDao().buscarPorCredenciales(usuarioNormalizado, claveHash)?.aDominio().
     * Aceptación: un usuario con clave equivocada devuelve null.
     */
    override suspend fun autenticarLocal(usuarioNormalizado: String, claveHash: String): Usuario? =
        TODO("E3-8b: buscar el usuario por credenciales en Room")

    // ---- Respaldo sin conexión (opcional: se reenvía al servidor cuando vuelve la red) ----

    /**
     * TODO(E3-8c) (avanzado) Marca el material como revisado de forma provisional; false si el contenido no está
     * asignado al estudiante (o ya no está APROBADO). Reglas: igual que MpazProRepositorio.confirmarMaterialRevisado.
     */
    override suspend fun marcarMaterialLocal(estudianteId: String, contenidoId: String): Boolean =
        TODO("E3-8c: (avanzado) marcar el material como revisado en Room")

    /**
     * TODO(E3-8d) (avanzado) Registro provisional de una respuesta a un desafío hecha sin conexión; null si la
     * actividad, la alternativa o la asignación no son válidas. Reglas: igual que MpazProRepositorio.responderDesafio.
     */
    override suspend fun registrarRespuestaLocal(estudianteId: String, actividadId: String, alternativa: Int): RespuestaLocal? =
        TODO("E3-8d: (avanzado) registrar la respuesta en Room")
}
