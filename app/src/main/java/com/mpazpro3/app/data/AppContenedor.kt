package com.mpazpro3.app.data

import android.content.Context
import com.mpazpro3.app.BuildConfig
import com.mpazpro3.app.data.local.AlmacenRoom
import com.mpazpro3.app.data.local.AppDatabase
import com.mpazpro3.app.data.local.CacheRoom
import com.mpazpro3.app.data.remote.AlmacenUrlPrefs
import com.mpazpro3.app.data.remote.ApiClient
import com.mpazpro3.app.data.remote.ApiService
import com.mpazpro3.app.data.remote.ColaPendientesPrefs
import com.mpazpro3.app.data.remote.ConectividadAndroid
import com.mpazpro3.app.data.remote.ConfiguracionServidor
import com.mpazpro3.app.data.remote.FuenteRemota
import com.mpazpro3.app.data.remote.InterceptoresExtra
import com.mpazpro3.app.data.remote.ProveedorApi
import com.mpazpro3.app.data.remote.SesionApi

/**
 * Arma, una sola vez, las piezas de la capa de datos: DOS bases Room independientes (la copia del
 * servidor y la del modo local), la API, la configuración del servidor, la cola de pendientes, el
 * interruptor de modo y el selector que elige la fuente en cada operación.
 */
class AppContenedor private constructor(contexto: Context) {

    /** Copia de lo que entrega el servidor (modo servidor). */
    private val dbServidor = AppDatabase.obtener(contexto, AppDatabase.NOMBRE_SERVIDOR)

    /** Base del modo local: otro archivo, con sus propios datos de ejemplo. */
    private val dbLocal = AppDatabase.obtener(contexto, AppDatabase.NOMBRE_LOCAL)

    private val sesion = SesionApi()
    private val conectividad = ConectividadAndroid(contexto)

    private val lecturasServidor = MpazProRepositorio(dbServidor)
    private val lecturasLocal = MpazProRepositorio(dbLocal)

    /**
     * Interruptor "Modo local / Modo servidor". BuildConfig.USAR_API solo define el valor INICIAL (false = modo
     * local, true = modo servidor); en cuanto el usuario lo cambia en la pantalla Servidor, manda lo guardado
     * en el dispositivo y se lee en cada operación.
     */
    val preferencia: PreferenciaModo = PreferenciaModoPrefs(contexto, inicialLocal = !BuildConfig.USAR_API)

    /** Dirección del servidor: la guardada por el usuario o API_BASE_URL (gradle.properties). */
    val configuracion = ConfiguracionServidor(AlmacenUrlPrefs(contexto), BuildConfig.API_BASE_URL)

    private val proveedorApi = ProveedorApi({ configuracion.url }) { url ->
        ApiClient.crearServicio(url, sesion, InterceptoresExtra.lista())
    }

    /** Modo servidor (Experiencia 3): API como fuente de verdad + copia en Room + cola de pendientes. */
    val remoto = RepositorioSincronizado(
        fuente = FuenteRemota(api = { proveedorApi.obtener() }, conectividad = conectividad, sesion = sesion),
        cache = CacheRoom(dbServidor),
        cola = ColaPendientesPrefs(contexto),
        conectividad = conectividad,
        sesion = sesion
    )

    /** Modo local: solo Room (base local), con las mismas reglas de dominio. */
    val local = RepositorioLocal(AlmacenRoom(dbLocal, sembrar = { lecturasLocal.sembrarSiEstaVacia() }))

    /** Lo único que usa el ViewModel: elige modo servidor o local en cada operación. */
    val fuente = SelectorFuente(preferencia, remoto, local, lecturasServidor, lecturasLocal)

    /** La primera vez que se abre la app carga los datos de ejemplo en las dos bases. */
    suspend fun sembrarSiEstaVacia() {
        lecturasServidor.sembrarSiEstaVacia()
        lecturasLocal.sembrarSiEstaVacia()
    }

    /** Servicio con tiempos cortos para "Probar conexión" (no usa la sesión ni cambia la dirección guardada). */
    fun servicioDePrueba(url: String): ApiService =
        ApiClient.crearServicio(url, SesionApi(), InterceptoresExtra.lista(), conexionSeg = 5, lecturaSeg = 5)

    companion object {
        @Volatile
        private var instancia: AppContenedor? = null

        fun obtener(contexto: Context): AppContenedor =
            instancia ?: synchronized(this) {
                instancia ?: AppContenedor(contexto.applicationContext).also { instancia = it }
            }
    }
}
