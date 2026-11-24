package com.mpazpro3.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos local (SQLite a través de Room). UNA sola base con todas las entidades del caso.
 *
 * version = 1 porque es el primer esquema. Si más adelante se agrega o cambia una columna hay
 * que subir el número y escribir una Migration; de lo contrario la app falla al abrir la base
 * (en desarrollo basta con desinstalar la app del dispositivo).
 *
 * exportSchema = false evita que Room exija una carpeta donde volcar el esquema en JSON.
 */
@Database(
    entities = [
        UsuarioEntity::class,
        EstudianteEntity::class,
        ContenidoEntity::class,
        RecursoEntity::class,
        ActividadEntity::class,
        AlternativaEntity::class,
        AsignacionEntity::class,
        SeguimientoEntity::class,
        RespuestaEntity::class,
        InformeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun estudianteDao(): EstudianteDao
    abstract fun contenidoDao(): ContenidoDao
    abstract fun recursoDao(): RecursoDao
    abstract fun actividadDao(): ActividadDao
    abstract fun asignacionDao(): AsignacionDao
    abstract fun seguimientoDao(): SeguimientoDao
    abstract fun respuestaDao(): RespuestaDao
    abstract fun informeDao(): InformeDao

    companion object {

        /** Base donde se guarda la copia de lo que entrega el servidor (modo servidor). */
        const val NOMBRE_SERVIDOR = "mpazpro3.db"

        /** Base del modo local: independiente de la copia del servidor (mismo esquema, otro archivo). */
        const val NOMBRE_LOCAL = "mpazpro3_local.db"

        private val instancias = mutableMapOf<String, AppDatabase>()

        /**
         * Una sola instancia por archivo: abrir dos veces el mismo archivo provoca bloqueos, por eso
         * se crea una vez y se reutiliza (synchronized evita que dos hilos la creen a la vez).
         */
        fun obtener(context: Context, nombre: String = NOMBRE_SERVIDOR): AppDatabase =
            synchronized(this) {
                instancias.getOrPut(nombre) {
                    Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, nombre).build()
                }
            }
    }
}
