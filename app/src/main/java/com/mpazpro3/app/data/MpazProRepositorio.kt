package com.mpazpro3.app.data

import com.mpazpro3.app.data.local.AppDatabase
import com.mpazpro3.app.data.local.SeguimientoEntity
import com.mpazpro3.app.model.EstadoAvance
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.RecursoDidactico
import com.mpazpro3.app.model.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Capa intermedia entre el ViewModel y la base de datos.
 *
 * Por qué existe: el ViewModel no debería saber si los datos vienen de Room, de un
 * servicio web o de una lista en memoria. Le pide datos al repositorio y el repositorio
 * decide de dónde salen. Si mañana se agrega un backend (Experiencia 3), solo cambia
 * esta clase.
 *
 * Flujo completo: Pantalla -> ViewModel -> Repositorio -> DAO -> SQLite.
 */
class MpazProRepositorio(private val db: AppDatabase) {

    // ----- Autenticación (RF-00) -----

    suspend fun autenticar(usuario: String, clave: String): Usuario? =
        db.usuarioDao().buscarPorCredenciales(usuario, clave)

    /** Todos los usuarios, para validar el login sin bloquear la pantalla. */
    fun usuarios(): Flow<List<Usuario>> =
        db.usuarioDao().observarTodos()

    // ----- Contenidos -----

    fun contenidos(): Flow<List<Contenido>> =
        db.contenidoDao().observarTodos()

    /** RF-07: contenidos asignados al curso y aprobados por UTP. */
    fun contenidosDelCurso(curso: String): Flow<List<Contenido>> =
        db.contenidoDao().observarAsignadosAlCurso(curso)

    suspend fun contenidoPorId(id: String): Contenido? =
        db.contenidoDao().porId(id)

    // ----- Recursos didácticos (RF-07) -----

    fun recursos(): Flow<List<RecursoDidactico>> =
        db.recursoDao().observarTodos()

    fun recursosDe(contenidoId: String): Flow<List<RecursoDidactico>> =
        db.recursoDao().observarDeContenido(contenidoId)

    // ----- Asignaciones -----

    fun asignaciones(): Flow<List<Asignacion>> =
        db.asignacionDao().observarTodas()

    fun asignacionesDelCurso(curso: String): Flow<List<Asignacion>> =
        db.asignacionDao().observarDelCurso(curso)

    // ----- Seguimiento de avance (RF-11) -----

    fun seguimientos(): Flow<List<SeguimientoEntity>> =
        db.seguimientoDao().observarTodos()

    /**
     * Marca que el estudiante terminó de revisar el material del contenido.
     *
     * Si no existía seguimiento, lo crea en EN_PROGRESO. Si ya existía, conserva
     * los intentos usados y no retrocede un avance que ya estaba FINALIZADO.
     */
    suspend fun marcarMaterialRevisado(estudianteId: String, contenidoId: String) {
        val actual = db.seguimientoDao().porEstudianteYContenido(estudianteId, contenidoId)

        val actualizado = actual?.copy(
            estado = if (actual.estado == EstadoAvance.FINALIZADO) {
                EstadoAvance.FINALIZADO
            } else {
                EstadoAvance.EN_PROGRESO
            }
        ) ?: SeguimientoEntity(
            estudianteId = estudianteId,
            contenidoId = contenidoId,
            estado = EstadoAvance.EN_PROGRESO
        )

        db.seguimientoDao().guardar(actualizado)
    }

    // ----- Carga inicial -----

    /**
     * Siembra la base con los datos ficticios de MockData la primera vez que se abre la app.
     * En ejecuciones siguientes no hace nada, porque las tablas ya tienen filas.
     *
     * El orden importa: primero los contenidos, porque recursos y asignaciones
     * tienen llave foránea hacia ellos y SQLite rechazaría la inserción al revés.
     */
    suspend fun sembrarSiEstaVacia() {
        if (db.contenidoDao().contar() > 0) return

        db.contenidoDao().insertarTodos(MockData.contenidos)
        db.recursoDao().insertarTodos(MockData.recursos)
        db.asignacionDao().insertarTodas(MockData.asignaciones)

        if (db.usuarioDao().contar() == 0) {
            db.usuarioDao().insertarTodos(MockData.usuarios)
        }
    }
}