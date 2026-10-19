package com.mpazpro3.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.RecursoDidactico
import com.mpazpro3.app.model.Usuario

/**
 * Base de datos local (SQLite a través de Room).
 *
 * version = 1 porque es el primer esquema. Si más adelante se agrega o cambia una columna,
 * hay que subir el número y escribir una migración, o la app se cae al abrir.
 *
 * exportSchema = false evita que Room exija una carpeta donde volcar el esquema en JSON.
 */
@Database(
    entities = [
        Usuario::class,
        Contenido::class,
        RecursoDidactico::class,
        Asignacion::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Convertidores::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun contenidoDao(): ContenidoDao
    abstract fun recursoDao(): RecursoDao
    abstract fun asignacionDao(): AsignacionDao

    companion object {

        /**
         * @Volatile asegura que todos los hilos vean el mismo valor de la instancia.
         * Abrir dos veces la misma base de datos provoca bloqueos, por eso se crea una sola vez.
         */
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpazpro3.db"
                ).build().also { instancia = it }
            }
    }
}