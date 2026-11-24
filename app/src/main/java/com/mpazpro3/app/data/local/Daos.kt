package com.mpazpro3.app.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/*
 * Un DAO declara QUÉ consultas se necesitan; Room genera la implementación al compilar.
 *  - fun suspend: se ejecuta una vez, fuera del hilo principal.
 *  - fun que devuelve Flow: emite de nuevo cada vez que cambia la tabla, y así las pantallas
 *    se actualizan solas.
 */

@Dao
interface UsuarioDao {

    /**
     * RF-00: busca credenciales válidas. COLLATE NOCASE ignora mayúsculas en el usuario; la
     * clave se compara por su resumen SHA-256 y SÍ distingue mayúsculas.
     */
    @Query("SELECT * FROM usuarios WHERE usuario = :usuario COLLATE NOCASE AND claveHash = :claveHash LIMIT 1")
    suspend fun buscarPorCredenciales(usuario: String, claveHash: String): UsuarioEntity?

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Query("DELETE FROM usuarios")
    suspend fun borrarTodos()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(usuarios: List<UsuarioEntity>)
}

@Dao
interface EstudianteDao {

    @Query("SELECT * FROM estudiantes ORDER BY id")
    fun observarTodos(): Flow<List<EstudianteEntity>>

    @Query("SELECT * FROM estudiantes WHERE id = :id LIMIT 1")
    suspend fun porId(id: String): EstudianteEntity?

    @Query("SELECT * FROM estudiantes ORDER BY id")
    suspend fun todos(): List<EstudianteEntity>

    @Query("DELETE FROM estudiantes")
    suspend fun borrarTodos()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(estudiantes: List<EstudianteEntity>)
}

@Dao
interface ContenidoDao {

    @Query("SELECT * FROM contenidos ORDER BY asignatura")
    fun observarTodos(): Flow<List<ContenidoEntity>>

    @Query("SELECT * FROM contenidos")
    suspend fun todos(): List<ContenidoEntity>

    @Query("SELECT * FROM contenidos WHERE id = :id LIMIT 1")
    suspend fun porId(id: String): ContenidoEntity?

    @Query("SELECT COUNT(*) FROM contenidos")
    suspend fun contar(): Int

    /** RF-01: cambia el estado y la observación de UTP de un contenido. */
    @Query("UPDATE contenidos SET estado = :estado, observacionUtp = :observacion WHERE id = :id")
    suspend fun actualizarEstado(id: String, estado: String, observacion: String)

    @Query("DELETE FROM contenidos")
    suspend fun borrarTodos()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(contenidos: List<ContenidoEntity>)
}

@Dao
interface RecursoDao {

    @Query("SELECT * FROM recursos ORDER BY id")
    fun observarTodos(): Flow<List<RecursoEntity>>

    @Query("DELETE FROM recursos")
    suspend fun borrarTodos()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(recursos: List<RecursoEntity>)
}

/** Una actividad junto con sus alternativas (relación 1 a N). */
data class ActividadConAlternativas(
    @Embedded val actividad: ActividadEntity,
    @Relation(parentColumn = "id", entityColumn = "actividadId")
    val alternativas: List<AlternativaEntity>
)

@Dao
interface ActividadDao {

    @Transaction
    @Query("SELECT * FROM actividades ORDER BY contenidoId, orden")
    fun observarTodas(): Flow<List<ActividadConAlternativas>>

    @Transaction
    @Query("SELECT * FROM actividades ORDER BY contenidoId, orden")
    suspend fun todas(): List<ActividadConAlternativas>

    @Transaction
    @Query("SELECT * FROM actividades WHERE contenidoId = :contenidoId")
    suspend fun deContenido(contenidoId: String): List<ActividadConAlternativas>

    @Transaction
    @Query("SELECT * FROM actividades WHERE id = :id LIMIT 1")
    suspend fun porId(id: String): ActividadConAlternativas?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarActividades(actividades: List<ActividadEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAlternativas(alternativas: List<AlternativaEntity>)

    @Query("DELETE FROM alternativas")
    suspend fun borrarAlternativas()

    @Query("DELETE FROM actividades")
    suspend fun borrarActividades()
}

@Dao
interface AsignacionDao {

    @Query("SELECT * FROM asignaciones ORDER BY id")
    fun observarTodas(): Flow<List<AsignacionEntity>>

    @Query("SELECT * FROM asignaciones WHERE curso = :curso")
    suspend fun delCurso(curso: String): List<AsignacionEntity>

    @Query("SELECT * FROM asignaciones WHERE curso = :curso AND contenidoId = :contenidoId LIMIT 1")
    suspend fun buscar(curso: String, contenidoId: String): AsignacionEntity?

    @Query("SELECT * FROM asignaciones")
    suspend fun todas(): List<AsignacionEntity>

    @Query("DELETE FROM asignaciones")
    suspend fun borrarTodas()

    @Query("DELETE FROM asignaciones WHERE curso = :curso")
    suspend fun borrarDelCurso(curso: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(asignaciones: List<AsignacionEntity>)
}

@Dao
interface SeguimientoDao {

    @Query("SELECT * FROM seguimientos")
    fun observarTodos(): Flow<List<SeguimientoEntity>>

    @Query("SELECT * FROM seguimientos")
    suspend fun todos(): List<SeguimientoEntity>

    @Query("SELECT * FROM seguimientos WHERE estudianteId = :estudianteId AND contenidoId = :contenidoId LIMIT 1")
    suspend fun buscar(estudianteId: String, contenidoId: String): SeguimientoEntity?

    /** REPLACE sirve como "insertar o actualizar": la clave compuesta decide cuál es. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(seguimiento: SeguimientoEntity)

    @Query("DELETE FROM seguimientos")
    suspend fun borrarTodos()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(seguimientos: List<SeguimientoEntity>)
}

@Dao
interface RespuestaDao {

    @Query("SELECT * FROM respuestas ORDER BY id")
    fun observarTodas(): Flow<List<RespuestaEntity>>

    @Query("SELECT * FROM respuestas ORDER BY id")
    suspend fun todas(): List<RespuestaEntity>

    @Query("SELECT * FROM respuestas WHERE estudianteId = :estudianteId")
    suspend fun deEstudiante(estudianteId: String): List<RespuestaEntity>

    @Query("SELECT COALESCE(MAX(numeroIntento), 0) FROM respuestas WHERE estudianteId = :estudianteId AND actividadId = :actividadId")
    suspend fun ultimoIntento(estudianteId: String, actividadId: String): Int

    @Insert
    suspend fun insertar(respuesta: RespuestaEntity)

    @Insert
    suspend fun insertarTodas(respuestas: List<RespuestaEntity>)

    @Query("DELETE FROM respuestas")
    suspend fun borrarTodas()

    @Query("DELETE FROM respuestas WHERE estudianteId = :estudianteId AND contenidoId = :contenidoId")
    suspend fun borrarDe(estudianteId: String, contenidoId: String)
}

@Dao
interface InformeDao {

    @Query("SELECT * FROM informes ORDER BY estudianteId")
    fun observarTodos(): Flow<List<InformeEntity>>

    @Query("SELECT * FROM informes ORDER BY estudianteId")
    suspend fun todos(): List<InformeEntity>

    @Query("SELECT * FROM informes WHERE estudianteId = :estudianteId LIMIT 1")
    suspend fun deEstudiante(estudianteId: String): InformeEntity?

    @Query("SELECT * FROM informes WHERE id = :id LIMIT 1")
    suspend fun porId(id: String): InformeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(informe: InformeEntity)

    @Query("DELETE FROM informes")
    suspend fun borrarTodos()

    /** RN-05: el estado solo cambia en filas que siguen en BORRADOR. Devuelve las filas afectadas. */
    @Query("UPDATE informes SET estado = 'VALIDADO' WHERE id = :id AND estado = 'BORRADOR'")
    suspend fun validar(id: String): Int
}
