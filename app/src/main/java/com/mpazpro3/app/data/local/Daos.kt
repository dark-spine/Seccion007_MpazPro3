package com.mpazpro3.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.RecursoDidactico
import com.mpazpro3.app.model.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Un DAO (Data Access Object) es una interfaz donde se declara QUÉ consultas se necesitan.
 * Room genera la implementación en tiempo de compilación.
 *
 * Dos tipos de función conviven acá:
 *  - suspend fun: se ejecuta una vez y devuelve un resultado. Debe llamarse desde una corrutina.
 *  - fun que devuelve Flow: entrega una corriente de datos que se vuelve a emitir sola cada vez
 *    que la tabla cambia. Es lo que permite que la pantalla se actualice sin recargar a mano.
 */

@Dao
interface UsuarioDao {

    /**
     * Busca credenciales válidas (RF-00). TRIM quita espacios al inicio y al final;
     * LOWER ignora mayúsculas en el nombre de usuario, pero NO en la clave.
     * LIMIT 1 porque basta con la primera coincidencia.
     */
    @Query(
        """
        SELECT * FROM usuarios
        WHERE LOWER(TRIM(usuario)) = LOWER(TRIM(:usuario))
          AND clave = :clave
        LIMIT 1
        """
    )
    suspend fun buscarPorCredenciales(usuario: String, clave: String): Usuario?

    @Query("SELECT * FROM usuarios")
    fun observarTodos(): Flow<List<Usuario>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(usuarios: List<Usuario>)
}

@Dao
interface ContenidoDao {

    @Query("SELECT * FROM contenidos ORDER BY asignatura")
    fun observarTodos(): Flow<List<Contenido>>

    /**
     * Corazón del RF-07: devuelve solo los contenidos asignados al curso del estudiante
     * que además siguen APROBADOS por UTP (RN-02).
     *
     * El INNER JOIN descarta los contenidos que no tienen asignación para ese curso.
     * 'APROBADO' se compara como texto porque el convertidor guarda enum.name.
     */
    @Query(
        """
        SELECT c.* FROM contenidos c
        INNER JOIN asignaciones a ON a.contenidoId = c.id
        WHERE a.curso = :curso
          AND c.estado = 'APROBADO'
        ORDER BY c.asignatura
        """
    )
    fun observarAsignadosAlCurso(curso: String): Flow<List<Contenido>>

    @Query("SELECT * FROM contenidos WHERE id = :id LIMIT 1")
    suspend fun porId(id: String): Contenido?

    @Query("SELECT COUNT(*) FROM contenidos")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(contenidos: List<Contenido>)
}

@Dao
interface RecursoDao {

    @Query("SELECT * FROM recursos")
    fun observarTodos(): Flow<List<RecursoDidactico>>

    @Query("SELECT * FROM recursos WHERE contenidoId = :contenidoId")
    fun observarDeContenido(contenidoId: String): Flow<List<RecursoDidactico>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(recursos: List<RecursoDidactico>)
}

@Dao
interface AsignacionDao {

    @Query("SELECT * FROM asignaciones WHERE curso = :curso")
    fun observarDelCurso(curso: String): Flow<List<Asignacion>>

    @Query("SELECT * FROM asignaciones")
    fun observarTodas(): Flow<List<Asignacion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(asignaciones: List<Asignacion>)
}