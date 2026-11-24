package com.mpazpro3.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mpazpro3.app.BuildConfig
import com.mpazpro3.app.data.AppContenedor
import com.mpazpro3.app.data.EstadoSync
import com.mpazpro3.app.data.RespuestaDesafio
import com.mpazpro3.app.data.ResultadoLogin
import com.mpazpro3.app.data.remote.ApiResult
import com.mpazpro3.app.data.remote.UrlServidor
import com.mpazpro3.app.data.remote.probarServidor
import com.mpazpro3.app.domain.ExportadorResultados
import com.mpazpro3.app.domain.ReglasAcceso
import com.mpazpro3.app.domain.ReglasAcceso.Accion
import com.mpazpro3.app.domain.ReglasAcceso.ErroresLogin
import com.mpazpro3.app.domain.ReglasContenido
import com.mpazpro3.app.domain.ReglasDesafio
import com.mpazpro3.app.domain.ReglasEvaluacion
import com.mpazpro3.app.domain.ReglasProgreso
import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.Estudiante
import com.mpazpro3.app.model.InformeDocente
import com.mpazpro3.app.model.Perfil
import com.mpazpro3.app.model.RecursoDidactico
import com.mpazpro3.app.model.Resultado
import com.mpazpro3.app.model.RespuestaEstudiante
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel único, compartido por todas las pantallas (se crea una sola vez en AppNavigation).
 *
 * Las pantallas solo dibujan y llaman a estas funciones (MVVM). Aquí se hace cumplir el perfil
 * del usuario activo (RN-01); las demás reglas de negocio viven en domain/ (funciones puras con
 * pruebas unitarias) y en MpazProRepositorio (operaciones atómicas sobre Room).
 *
 * Modo local / modo servidor: el ViewModel no sabe de dónde vienen los datos. Usa una única fuente,
 * SelectorFuente, que en cada operación consulta el interruptor "Modo local" (preferencia persistente; la
 * pantalla Servidor lo cambia). Modo local = solo Room (RepositorioLocal), como antes de la Experiencia 3.
 * Modo servidor = el ingreso, la descarga de datos y cada acción que modifica datos pasan por
 * RepositorioSincronizado (la API es la fuente de verdad y Room queda como copia). Lo que falta implementar
 * está en data/remote/ y en RepositorioSincronizado, marcado con TODO(E3-n); este archivo ya trae las ramas
 * listas. BuildConfig.USAR_API solo define el valor INICIAL del interruptor (false = modo local). Cada modo
 * tiene su propia base Room: lo creado en modo local no se envía al servidor ni se mezcla con sus datos.
 *
 * Los datos salen de Room como StateFlow: cuando algo cambia en la base, todas las pantallas que
 * lo observan se vuelven a dibujar solas. Las funciones de lectura (contenidosFiltrados,
 * progreso, respuestasDe…) calculan sobre el valor actual; la pantalla las llama dentro de
 * remember(...) con los StateFlow que observa, para recalcular cuando cambian.
 */
class MpazProViewModel internal constructor(
    application: Application,
    private val contenedor: AppContenedor
) : AndroidViewModel(application) {
    constructor(application: Application) : this(application, AppContenedor.obtener(application))

    /** Piezas de la capa de datos: las dos bases Room, la API, el interruptor de modo y el selector. */

    /** Única fuente de datos: lecturas (Room del modo activo) y escrituras (modo servidor o local, según el interruptor). */
    private val fuente = contenedor.fuente

    /** La primera vez que se abre la app carga los datos ficticios en las dos bases; el login espera a que termine. */
    private val inicializacion = viewModelScope.async { contenedor.sembrarSiEstaVacia() }

    /** Ejecuta el bloque en el alcance del ViewModel: la escritura termina aunque la pantalla se cierre. */
    private suspend fun <T> enSegundoPlano(bloque: suspend () -> T): T =
        viewModelScope.async { bloque() }.await()

    /**
     * Como [enSegundoPlano], pero si la fuente del modo servidor todavía tiene un TODO(E3-n) sin completar
     * (NotImplementedError) lo explica con [alFaltar] en vez de cerrar la app.
     */
    private suspend fun <T> conTodoPendiente(alFaltar: (String) -> T, bloque: suspend () -> T): T =
        try {
            enSegundoPlano(bloque)
        } catch (e: NotImplementedError) {
            alFaltar("Falta completar la Experiencia 3 (${e.message}).")
        }

    // =====================================================================
    // Autenticación (RF-00, RNF-08)
    // =====================================================================

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
    val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo.asStateFlow()

    private val _erroresLogin = MutableStateFlow(ErroresLogin())
    val erroresLogin: StateFlow<ErroresLogin> = _erroresLogin.asStateFlow()

    /**
     * Valida los campos y luego las credenciales: en modo local contra la base local; en modo servidor contra
     * POST /auth/login (sin conexión, con los datos guardados). Devuelve true si el usuario ingresó;
     * AppNavigation lo lleva a la pantalla de su perfil. Si falla, deja en erroresLogin el mensaje por campo
     * (o uno genérico: no se revela si falló el usuario o la clave).
     */
    suspend fun login(usuario: String, clave: String): Boolean {
        val errores = ReglasAcceso.validarCampos(usuario, clave)
        if (errores.hayErrores) {
            _erroresLogin.value = errores
            return false
        }
        val resultado = try {
            enSegundoPlano {
                inicializacion.await()
                fuente.iniciarSesion(usuario, clave)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: NotImplementedError) {
            // Un TODO(E3-n) todavía sin completar: se muestra en pantalla en vez de cerrar la app.
            _erroresLogin.value = ErroresLogin(general = "Falta completar la Experiencia 3 (${e.message}).")
            return false
        } catch (e: Exception) {
            _erroresLogin.value = ErroresLogin(general = "No se pudo ingresar. Inténtalo de nuevo.")
            return false
        }
        return when (resultado) {
            is ResultadoLogin.Exito -> {
                _usuarioActivo.value = resultado.usuario
                _erroresLogin.value = ErroresLogin()
                when {
                    resultado.modoLocal -> Unit
                    resultado.enLinea -> sincronizarAhora(resultado.usuario)
                    else -> fuente.publicarAviso("Ingresaste sin conexión: ves los últimos datos guardados en el dispositivo.")
                }
                true
            }
            is ResultadoLogin.Rechazado -> {
                _erroresLogin.value = when (resultado.campo) {
                    "usuario" -> ErroresLogin(usuario = resultado.mensaje)
                    "clave" -> ErroresLogin(clave = resultado.mensaje)
                    else -> ErroresLogin(general = resultado.mensaje)
                }
                false
            }
            is ResultadoLogin.Falla -> {
                _erroresLogin.value = ErroresLogin(general = resultado.mensaje)
                false
            }
        }
    }

    /** Borra los avisos de error del formulario cuando el usuario corrige lo que escribió. */
    fun limpiarErroresLogin() {
        if (_erroresLogin.value.hayErrores) _erroresLogin.value = ErroresLogin()
    }

    fun logout() {
        // Se toma la fuente AHORA: aunque el modo cambie enseguida, la sesión se cierra en la fuente donde se abrió.
        val abierta = fuente.actual()
        viewModelScope.launch {
            try {
                abierta.cerrarSesion()
            } catch (e: NotImplementedError) {
                // TODO(E3-9b) todavía sin completar: la sesión local ya se cerró.
            }
        }
        _usuarioActivo.value = null
        _erroresLogin.value = ErroresLogin()
        _contenidoSeleccionadoId.value = null
        _estudianteSeleccionadoId.value = null
    }

    // =====================================================================
    // Conexión con el servidor (Experiencia 3): sincronización, avisos y dirección
    // =====================================================================

    /** Estado de la última sincronización (cargando, sin conexión, error). Siempre Inactivo en modo local. */
    val estadoSync: StateFlow<EstadoSync> = fuente.estado.comoEstado(EstadoSync.Inactivo)

    /** Acciones hechas sin conexión que todavía no se enviaron al servidor. */
    val pendientes: StateFlow<Int> = fuente.pendientes.comoEstado(0)

    /** Mensaje informativo (por ejemplo, "se guardó en el dispositivo"); la pantalla lo muestra hasta cerrarlo. */
    val aviso: StateFlow<String?> = fuente.aviso.comoEstado(null)

    fun descartarAviso() {
        fuente.descartarAviso()
    }

    /** true = modo local (sin servidor). Es el interruptor persistente que lee SelectorFuente en cada operación. */
    val modoLocal: StateFlow<Boolean> = contenedor.preferencia.modoLocalFlujo

    /**
     * Cambia entre modo local y modo servidor. Surte efecto de inmediato (la siguiente operación ya usa el
     * otro modo). Si había una sesión abierta se cierra: cada modo tiene sus propios datos y su propia sesión.
     */
    fun cambiarModoLocal(local: Boolean) {
        if (contenedor.preferencia.modoLocal == local) return
        if (_usuarioActivo.value != null) logout()
        contenedor.preferencia.cambiar(local)
        ultimaSincronizacion = 0L
    }

    /** Borra lo creado en modo local y vuelve a los datos de ejemplo (no toca los datos del servidor). */
    suspend fun restablecerDatosLocales(): Resultado = enSegundoPlano { contenedor.local.restablecerDatos() }

    /** Dirección en uso: la guardada en la pantalla Servidor o, mientras falte E3-1, API_BASE_URL. */
    private fun urlInicial(): String =
        try {
            contenedor.configuracion.url
        } catch (e: NotImplementedError) {
            BuildConfig.API_BASE_URL
        }

    private val _urlServidor = MutableStateFlow(urlInicial())

    /** Dirección del servidor que muestra el login. */
    val urlServidor: StateFlow<String> = _urlServidor.asStateFlow()

    private var ultimaSincronizacion = 0L

    private suspend fun sincronizarAhora(usuario: Usuario) {
        try {
            enSegundoPlano { fuente.sincronizar(usuario) }
        } catch (e: NotImplementedError) {
            fuente.publicarAviso("Falta completar la Experiencia 3 (${e.message}).")
        }
        ultimaSincronizacion = System.currentTimeMillis()
    }

    /**
     * Descarga de nuevo los datos del servidor y actualiza la copia local. Las pantallas principales
     * lo llaman al abrirse (sin repetir si se sincronizó hace pocos segundos); "Reintentar" lo fuerza.
     * En modo local no hace nada.
     */
    fun actualizar(forzar: Boolean = false) {
        val usuario = _usuarioActivo.value ?: return
        if (contenedor.preferencia.modoLocal) return
        if (estadoSync.value == EstadoSync.Cargando) return
        if (!forzar && System.currentTimeMillis() - ultimaSincronizacion < INTERVALO_MINIMO_MS) return
        viewModelScope.launch { sincronizarAhora(usuario) }
    }

    /** Mensaje de error de una dirección escrita en la pantalla Servidor (null = válida). */
    fun errorDeUrlServidor(entrada: String): String? =
        (UrlServidor.validar(entrada) as? UrlServidor.Validacion.Invalida)?.mensaje

    /** Guarda la dirección del servidor (sin recompilar); las siguientes llamadas ya la usan. */
    fun guardarUrlServidor(entrada: String): Resultado {
        val validacion = try {
            contenedor.configuracion.guardar(entrada)
        } catch (e: NotImplementedError) {
            return Resultado.error("Falta completar la Experiencia 3 (${e.message}).")
        }
        return when (validacion) {
            is UrlServidor.Validacion.Valida -> {
                _urlServidor.value = validacion.url
                Resultado.ok("Servidor guardado: ${validacion.url}")
            }
            is UrlServidor.Validacion.Invalida -> Resultado.error(validacion.mensaje)
        }
    }

    /** Vuelve a la dirección inicial (API_BASE_URL) y la devuelve. */
    fun restablecerUrlServidor(): String {
        val url = try {
            contenedor.configuracion.restablecer()
        } catch (e: NotImplementedError) {
            BuildConfig.API_BASE_URL
        }
        _urlServidor.value = url
        return url
    }

    /** Prueba GET /health contra la dirección escrita, sin guardarla. */
    suspend fun probarConexion(entrada: String): Resultado {
        return when (val validacion = UrlServidor.validar(entrada)) {
            is UrlServidor.Validacion.Invalida -> Resultado.error(validacion.mensaje)
            is UrlServidor.Validacion.Valida ->
                conTodoPendiente({ Resultado.error(it) }) { probarServidor(contenedor.servicioDePrueba(validacion.url)) }
        }
    }

    private companion object {
        /** No se repite la descarga si la anterior fue hace menos de este tiempo. */
        const val INTERVALO_MINIMO_MS = 5_000L
    }

    private fun permitido(accion: Accion): Boolean = ReglasAcceso.puede(_usuarioActivo.value?.perfil, accion)

    private fun esEstudianteActivo(estudianteId: String): Boolean {
        val usuario = _usuarioActivo.value
        return usuario?.perfil == Perfil.ESTUDIANTE && usuario.id == estudianteId
    }

    private val sinPermiso = Resultado.error(ReglasAcceso.MENSAJE_SIN_PERMISO)

    // =====================================================================
    // Datos compartidos (vienen de Room) y selección
    // =====================================================================

    private fun <T> Flow<T>.comoEstado(inicial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.Eagerly, inicial)

    val contenidos: StateFlow<List<Contenido>> = fuente.contenidos().comoEstado(emptyList())
    val recursos: StateFlow<List<RecursoDidactico>> = fuente.recursos().comoEstado(emptyList())
    val actividades: StateFlow<List<Actividad>> = fuente.actividades().comoEstado(emptyList())
    val asignaciones: StateFlow<List<Asignacion>> = fuente.asignaciones().comoEstado(emptyList())
    val estudiantes: StateFlow<List<Estudiante>> = fuente.estudiantes().comoEstado(emptyList())
    val seguimientos: StateFlow<List<SeguimientoAvance>> = fuente.seguimientos().comoEstado(emptyList())

    /** RN-01: los informes solo se exponen mientras el usuario activo es DOCENTE. */
    val informes: StateFlow<List<InformeDocente>> =
        combine(_usuarioActivo, fuente.informes()) { usuario, lista ->
            if (ReglasAcceso.puede(usuario?.perfil, Accion.GESTIONAR_INFORMES)) lista else emptyList()
        }.comoEstado(emptyList())

    private val _contenidoSeleccionadoId = MutableStateFlow<String?>(null)
    val contenidoSeleccionadoId: StateFlow<String?> = _contenidoSeleccionadoId.asStateFlow()

    private val _estudianteSeleccionadoId = MutableStateFlow<String?>(null)
    val estudianteSeleccionadoId: StateFlow<String?> = _estudianteSeleccionadoId.asStateFlow()

    fun seleccionarContenido(id: String?) {
        _contenidoSeleccionadoId.value = id
    }

    fun seleccionarEstudiante(id: String?) {
        _estudianteSeleccionadoId.value = id
    }

    fun contenidoPorId(id: String?): Contenido? = contenidos.value.find { it.id == id }

    fun estudiantePorId(id: String?): Estudiante? = estudiantes.value.find { it.id == id }

    fun recursosDe(contenidoId: String?): List<RecursoDidactico> =
        recursos.value.filter { it.contenidoId == contenidoId }

    fun actividadPorId(id: String): Actividad? = actividades.value.find { it.id == id }

    // =====================================================================
    // Módulo "Autorización de contenidos (UTP)": RF-01, RF-02, RN-04
    // =====================================================================

    /** RF-02: contenidos filtrados por asignatura y estado (null = todos), pendientes primero. */
    fun contenidosFiltrados(asignatura: Asignatura?, estado: EstadoAprobacion?): List<Contenido> =
        if (permitido(Accion.REVISAR_CONTENIDOS)) {
            ReglasContenido.filtrar(contenidos.value, asignatura, estado)
        } else {
            emptyList()
        }

    /** RF-01: UTP aprueba el contenido y deja su observación (opcional). Aplica RN-04. */
    suspend fun aprobarContenido(contenidoId: String, observacion: String): Resultado {
        if (!permitido(Accion.REVISAR_CONTENIDOS)) return sinPermiso
        return conTodoPendiente({ Resultado.error(it) }) { fuente.aprobarContenido(contenidoId, observacion.trim()) }
    }

    /** RF-01: UTP rechaza el contenido; el motivo es obligatorio. */
    suspend fun rechazarContenido(contenidoId: String, observacion: String): Resultado {
        if (!permitido(Accion.REVISAR_CONTENIDOS)) return sinPermiso
        return conTodoPendiente({ Resultado.error(it) }) { fuente.rechazarContenido(contenidoId, observacion.trim()) }
    }

    // =====================================================================
    // Módulo "Planificación docente": RF-03 a RF-06, RN-02, RN-03
    // =====================================================================

    /** RF-03 y RN-02: solo los contenidos APROBADOS por UTP. */
    fun contenidosDisponiblesParaAsignar(): List<Contenido> =
        if (permitido(Accion.PLANIFICAR_CURSO)) ReglasContenido.disponiblesParaAsignar(contenidos.value) else emptyList()

    /** RF-04 y RF-05: asigna los contenidos al curso con el máximo de intentos de la evaluación final. */
    suspend fun asignarAlCurso(curso: String, contenidoIds: List<String>, maxIntentos: Int): Resultado {
        if (!permitido(Accion.PLANIFICAR_CURSO)) return sinPermiso
        return conTodoPendiente({ Resultado.error(it) }) { fuente.asignarAlCurso(curso, contenidoIds, maxIntentos) }
    }

    fun asignacionesDelCurso(curso: String): List<Asignacion> =
        asignaciones.value.filter { it.curso == curso }

    fun estudiantesDelCurso(curso: String): List<Estudiante> =
        if (permitido(Accion.VER_RESULTADOS)) estudiantes.value.filter { it.curso == curso } else emptyList()

    /** Contenidos aprobados y asignados al curso. */
    private fun contenidosAsignados(curso: String?): List<Contenido> =
        ReglasContenido.asignadosAlCurso(contenidos.value, asignaciones.value, curso)

    private fun cursoDe(estudianteId: String): String? = estudiantePorId(estudianteId)?.curso

    /** RF-06: avance general (0.0 a 1.0) = promedio del avance en los contenidos asignados a su curso. */
    fun progresoGeneral(estudianteId: String): Float {
        val asignados = contenidosAsignados(cursoDe(estudianteId))
        return ReglasProgreso.progresoGeneral(asignados.map { progreso(estudianteId, it.id) })
    }

    /**
     * RF-06: arma el CSV de resultados del curso (una fila por estudiante y contenido). La
     * pantalla lo guarda en el dispositivo o lo comparte. Devuelve null si no corresponde
     * (otro perfil) o si el curso no tiene estudiantes. En modo servidor lo genera el servidor
     * (GET /cursos/{curso}/resultados.csv) y, sin conexión, se arma en el dispositivo; en modo local se
     * arma con la base local.
     */
    suspend fun descargarResultados(curso: String): String? {
        if (!permitido(Accion.VER_RESULTADOS)) return null
        when (val r = conTodoPendiente<ApiResult<String>>({ ApiResult.Error(0, it) }) { fuente.descargarResultadosCsv(curso) }) {
            is ApiResult.Exito -> return r.datos
            is ApiResult.Error -> {
                fuente.publicarAviso(r.mensaje)
                return null
            }
            is ApiResult.SinConexion ->
                fuente.publicarAviso("Sin conexión: el archivo se generó con los datos guardados en el dispositivo.")
        }
        return resultadosLocales(curso)
    }

    private fun resultadosLocales(curso: String): String? {
        val delCurso = estudiantes.value.filter { it.curso == curso }
        if (delCurso.isEmpty()) return null
        val filas = ExportadorResultados.construirFilas(
            curso = curso,
            estudiantes = delCurso,
            contenidosAsignados = contenidosAsignados(curso),
            asignaciones = asignaciones.value,
            actividades = actividades.value,
            seguimientos = seguimientos.value
        )
        return ExportadorResultados.generarCsv(filas)
    }

    // =====================================================================
    // Módulo "Informe pedagógico": RF-12 a RF-14, RN-01, RN-05, RN-06
    // =====================================================================

    fun informeDe(estudianteId: String?): InformeDocente? =
        informes.value.find { it.estudianteId == estudianteId }

    /** RF-12: genera el informe en BORRADOR. Un informe VALIDADO no se regenera. */
    suspend fun generarInforme(estudianteId: String): Resultado {
        if (!permitido(Accion.GESTIONAR_INFORMES)) return sinPermiso
        return conTodoPendiente({ Resultado.error(it) }) { fuente.generarInforme(estudianteId) }
    }

    /** RF-13: BORRADOR -> VALIDADO. */
    suspend fun validarInforme(informeId: String): Resultado {
        if (!permitido(Accion.GESTIONAR_INFORMES)) return sinPermiso
        return conTodoPendiente({ Resultado.error(it) }) { fuente.validarInforme(informeId) }
    }

    // =====================================================================
    // Módulo "Experiencia del estudiante": RF-07 a RF-11, RN-01, RN-03
    // =====================================================================

    /** RF-07: contenidos asignados al curso del estudiante que siguen aprobados. */
    fun contenidosDelEstudiante(usuario: Usuario?): List<Contenido> =
        if (usuario?.perfil == Perfil.ESTUDIANTE) contenidosAsignados(usuario.curso) else emptyList()

    /** RF-11: avance (0.0 a 1.0) del estudiante en un contenido. */
    fun progreso(estudianteId: String, contenidoId: String): Float {
        val seguimiento = seguimientos.value.find { it.estudianteId == estudianteId && it.contenidoId == contenidoId }
        val deContenido = actividades.value.filter { it.contenidoId == contenidoId }
        return ReglasProgreso.progresoContenido(seguimiento, deContenido)
    }

    /** RF-11: guarda que el estudiante terminó de revisar el material. */
    suspend fun confirmarMaterialRevisado(estudianteId: String, contenidoId: String): Resultado {
        if (!esEstudianteActivo(estudianteId)) return sinPermiso
        return conTodoPendiente({ Resultado.error(it) }) { fuente.confirmarMaterialRevisado(estudianteId, contenidoId) }
    }

    /** RF-08: desafíos del contenido (sin la evaluación final) de fácil a difícil. */
    fun desafiosDe(contenidoId: String?): List<Actividad> =
        ReglasDesafio.desafiosDe(actividades.value, contenidoId)

    /**
     * RF-08: registra la respuesta y devuelve si fue correcta (true/false). Devuelve null, sin
     * registrar nada, si la alternativa no existe o el contenido no está asignado al estudiante
     * (en modo servidor, también si el servidor la rechazó). El motivo queda en el aviso de la pantalla.
     */
    suspend fun responderDesafio(estudianteId: String, actividad: Actividad, alternativa: Int): Boolean? {
        if (!esEstudianteActivo(estudianteId)) return null
        val respuesta = conTodoPendiente({ RespuestaDesafio(null, it) }) {
            fuente.responderDesafio(estudianteId, actividad.id, alternativa)
        }
        respuesta.mensaje?.let { fuente.publicarAviso(it) }
        return respuesta.esCorrecta
    }

    /** RF-09: preguntas de la evaluación final del contenido. */
    fun evaluacionFinalDe(contenidoId: String?): List<Actividad> =
        ReglasDesafio.evaluacionFinalDe(actividades.value, contenidoId)

    /** RF-09 y RN-03: máximo de la asignación del curso menos los intentos usados (null = no asignado). */
    fun intentosRestantes(estudianteId: String, contenidoId: String): Int? {
        val curso = cursoDe(estudianteId)
        val asignado = contenidosAsignados(curso).any { it.id == contenidoId }
        val maximo = if (asignado) {
            asignaciones.value.find { it.curso == curso && it.contenidoId == contenidoId }?.maxIntentosEvaluacion
        } else {
            null
        }
        val usados = seguimientos.value
            .find { it.estudianteId == estudianteId && it.contenidoId == contenidoId }
            ?.intentosEvaluacionUsados ?: 0
        return ReglasEvaluacion.intentosRestantes(maximo, usados)
    }

    /**
     * RF-09 y RN-03: registra un intento de la evaluación solo si quedan intentos (se vuelve a
     * verificar aquí y dentro de la transacción; no se confía en el botón de la pantalla).
     *
     * @param respuestas mapa idActividad -> índice de la alternativa elegida.
     */
    suspend fun enviarEvaluacion(estudianteId: String, contenidoId: String, respuestas: Map<String, Int>): Resultado {
        if (!esEstudianteActivo(estudianteId)) return sinPermiso
        return conTodoPendiente({ Resultado.error(it) }) { fuente.enviarEvaluacion(estudianteId, contenidoId, respuestas) }
    }

    /**
     * RF-10: respuestas vigentes del estudiante en el contenido (la última de cada desafío y las
     * del último intento de la evaluación). Solo el propio estudiante ve las suyas (RN-01).
     */
    fun respuestasDe(estudianteId: String, contenidoId: String): List<RespuestaEstudiante> {
        if (!esEstudianteActivo(estudianteId)) return emptyList()
        val seguimiento = seguimientos.value.find { it.estudianteId == estudianteId && it.contenidoId == contenidoId }
            ?: return emptyList()
        val deContenido = actividades.value.filter { it.contenidoId == contenidoId }
        return ReglasProgreso.respuestasVigentes(seguimiento.respuestas, deContenido)
    }
}
