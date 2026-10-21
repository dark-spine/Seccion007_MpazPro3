package com.mpazpro3.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mpazpro3.app.data.MockData
import com.mpazpro3.app.data.MpazProRepositorio
import com.mpazpro3.app.data.local.AppDatabase
import com.mpazpro3.app.data.local.SeguimientoEntity
import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.EstadoAvance
import com.mpazpro3.app.model.Estudiante
import com.mpazpro3.app.model.InformeDocente
import com.mpazpro3.app.model.Perfil
import com.mpazpro3.app.model.RecursoDidactico
import com.mpazpro3.app.model.RespuestaEstudiante
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel único, compartido por todas las pantallas (se crea una sola vez en AppNavigation).
 *
 * ESTO ES UN ESQUELETO: las pantallas ya llaman a todas estas funciones, pero la lógica de
 * negocio del caso está vacía a propósito y marcada con TODO(equipo), citando el módulo de
 * Requerimientos_Seccion007 al que corresponde. Mientras no se implemente, las listas se
 * muestran sin filtrar y las acciones (aprobar, asignar, responder, validar) no cambian nada.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MpazProViewModel(application: Application) : AndroidViewModel(application) {

    // =====================================================================
    // Persistencia (Room)
    // =====================================================================

    /**
     * El ViewModel no habla con Room directamente: habla con el repositorio.
     * Cadena completa: Pantalla -> ViewModel -> Repositorio -> DAO -> SQLite.
     */
    private val repositorio = MpazProRepositorio(AppDatabase.obtener(application))

    init {
        // La primera vez que se abre la app, carga los datos ficticios en la base.
        // En ejecuciones siguientes no hace nada: los datos ya persisten.
        viewModelScope.launch { repositorio.sembrarSiEstaVacia() }
    }

    /**
     * Las pantallas llaman funciones normales (no suspend), así que las corrientes de Room
     * se recogen acá y quedan disponibles como StateFlow.
     *
     * Se usa SharingStarted.Eagerly y no WhileSubscribed porque varias funciones leen
     * .value sin suscribirse: con WhileSubscribed nadie colectaría y el valor quedaría vacío.
     */
    private val usuariosRoom: StateFlow<List<Usuario>> = repositorio.usuarios()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Seguimientos de avance guardados en Room (RF-11).
     *
     * Es público para que la pantalla pueda observarlo con collectAsState(): así la barra de
     * avance se vuelve a dibujar sola cuando cambia el progreso, sin depender de que el
     * usuario navegue hacia atrás para forzar la recomposición.
     */
    val avances: StateFlow<List<SeguimientoEntity>> = repositorio.seguimientos()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // =====================================================================
    // Autenticación
    // =====================================================================

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
    val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo.asStateFlow()

    private val _errorLogin = MutableStateFlow<String?>(null)
    val errorLogin: StateFlow<String?> = _errorLogin.asStateFlow()

    /**
     * Errores por campo del formulario de inicio de sesión.
     *
     * Van separados del mensaje general para que la pantalla pueda marcar en rojo el campo
     * exacto que falla y poner el mensaje justo debajo de él, en lugar de un aviso suelto.
     * La decisión de qué campo está mal se toma acá, no en el @Composable: la interfaz
     * solo dibuja lo que el ViewModel ya determinó.
     */
    private val _errorUsuario = MutableStateFlow<String?>(null)
    val errorUsuario: StateFlow<String?> = _errorUsuario.asStateFlow()

    private val _errorClave = MutableStateFlow<String?>(null)
    val errorClave: StateFlow<String?> = _errorClave.asStateFlow()

    /**
     * RF-00. Valida las credenciales contra la tabla usuarios de Room.
     *
     * Reglas aplicadas:
     *  - campos obligatorios, con mensaje distinto para cada uno;
     *  - el nombre de usuario ignora mayúsculas y espacios sobrantes; la clave no;
     *  - si falla, el mensaje es genérico: no se revela si el error fue el usuario o la clave.
     */
    fun login(usuario: String, clave: String): Boolean {
        // Se limpian los errores anteriores antes de volver a validar.
        _errorUsuario.value = null
        _errorClave.value = null
        _errorLogin.value = null

        // Campos obligatorios: se revisan los dos para avisar de ambos a la vez.
        var hayVacios = false
        if (usuario.isBlank()) {
            _errorUsuario.value = "Ingresa tu usuario"
            hayVacios = true
        }
        if (clave.isBlank()) {
            _errorClave.value = "Ingresa tu clave"
            hayVacios = true
        }
        if (hayVacios) return false

        val buscado = usuario.trim().lowercase()
        val encontrado = usuariosRoom.value.firstOrNull {
            it.usuario.trim().lowercase() == buscado && it.clave == clave
        }

        return if (encontrado != null) {
            _usuarioActivo.value = encontrado
            true
        } else {
            // Mensaje genérico: no se revela cuál de los dos datos falló.
            _errorLogin.value = "Usuario o clave incorrectos"
            _errorUsuario.value = " "
            _errorClave.value = " "
            false
        }
    }

    /** Borra los avisos de error cuando el usuario corrige lo que escribió. */
    fun limpiarErroresLogin() {
        if (_errorUsuario.value != null || _errorClave.value != null || _errorLogin.value != null) {
            _errorUsuario.value = null
            _errorClave.value = null
            _errorLogin.value = null
        }
    }


    fun logout() {
        _usuarioActivo.value = null
        _errorLogin.value = null
        _errorUsuario.value = null
        _errorClave.value = null
        _contenidoSeleccionadoId.value = null
        _estudianteSeleccionadoId.value = null
    }

    // =====================================================================
    // Datos compartidos y selección (no es lógica de negocio)
    // =====================================================================

    /** Contenidos: ahora vienen de Room, no de una lista en memoria. */
    val contenidos: StateFlow<List<Contenido>> = repositorio.contenidos()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _recursos: StateFlow<List<RecursoDidactico>> = repositorio.recursos()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Todavía en memoria: estas tablas se migran a Room en una etapa posterior.
    private val _actividades = MutableStateFlow(MockData.actividades)

    val asignaciones: StateFlow<List<Asignacion>> = repositorio.asignaciones()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * RF-07. Contenidos del curso del estudiante activo, resueltos por la consulta SQL
     * del DAO (INNER JOIN con asignaciones + filtro estado = APROBADO, que es RN-02).
     *
     * flatMapLatest reacciona al cambio de usuario: al iniciar o cerrar sesión, la
     * corriente se vuelve a armar con el curso que corresponda.
     */
    private val contenidosDelCurso: StateFlow<List<Contenido>> = _usuarioActivo
        .flatMapLatest { activo ->
            val curso = activo?.curso
            if (activo?.perfil == Perfil.ESTUDIANTE && !curso.isNullOrBlank()) {
                repositorio.contenidosDelCurso(curso)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _estudiantes = MutableStateFlow(MockData.estudiantes)
    val estudiantes: StateFlow<List<Estudiante>> = _estudiantes.asStateFlow()

    private val _seguimientos = MutableStateFlow(MockData.seguimientos)
    val seguimientos: StateFlow<List<SeguimientoAvance>> = _seguimientos.asStateFlow()

    private val _informes = MutableStateFlow<List<InformeDocente>>(emptyList())
    val informes: StateFlow<List<InformeDocente>> = _informes.asStateFlow()

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

    fun estudiantePorId(id: String?): Estudiante? = _estudiantes.value.find { it.id == id }

    fun recursosDe(contenidoId: String?): List<RecursoDidactico> =
        _recursos.value.filter { it.contenidoId == contenidoId }

    fun actividadPorId(id: String): Actividad? = _actividades.value.find { it.id == id }

    // =====================================================================
    // Módulo "Autorización de contenidos (UTP)"
    // =====================================================================

    /**
     * TODO(equipo): filtrar por asignatura y por estado (null = todos). Hoy devuelve todo.
     */
    fun contenidosFiltrados(asignatura: Asignatura?, estado: EstadoAprobacion?): List<Contenido> = contenidos.value

    /**
     * TODO(equipo): cambiar el estado del contenido a APROBADO y guardar la observación.
     * RF: "UTP debe poder revisar y autorizar/rechazar un contenido" y "registrar el estado de
     * aprobación de cada contenido". Hoy no hace nada.
     */
    fun aprobarContenido(contenidoId: String, observacion: String) {
        // Sin implementar todavía.
    }

    /** TODO(equipo): igual que aprobarContenido(), pero con estado RECHAZADO. Hoy no hace nada. */
    fun rechazarContenido(contenidoId: String, observacion: String) {
        // Sin implementar todavía.
    }

    // =====================================================================
    // Módulo "Planificación docente"
    // =====================================================================

    /**
     * TODO(equipo): devolver SOLO los contenidos APROBADOS por UTP (regla de negocio: un
     * contenido no autorizado no puede asignarse). Hoy devuelve todos.
     */
    fun contenidosDisponiblesParaAsignar(): List<Contenido> = contenidos.value

    /**
     * TODO(equipo): crear las asignaciones del curso para los contenidos elegidos, con el máximo
     * de intentos de la evaluación final. RF: "seleccionar los OA disponibles", "asignar
     * contenidos y desafíos a su curso", "definir el número máximo de intentos". Validar que
     * todos los contenidos estén aprobados. Hoy no hace nada.
     */
    fun asignarAlCurso(curso: String, contenidoIds: List<String>, maxIntentos: Int) {
        // Sin implementar todavía.
    }

    /** TODO(equipo): devolver solo las asignaciones del curso indicado. Hoy devuelve todas. */
    fun asignacionesDelCurso(curso: String): List<Asignacion> = asignaciones.value

    /**
     * TODO(equipo): calcular el avance del estudiante (0.0 a 1.0) a partir de sus
     * SeguimientoAvance. RF: "revisar los resultados de sus estudiantes". Hoy devuelve 0.
     */
    fun progresoGeneral(estudianteId: String): Float = 0f

    /**
     * TODO(equipo): exportar los resultados del curso (por ejemplo a un archivo CSV o PDF que
     * se pueda compartir). RF: "revisar, descargar y validar los resultados". Hoy no hace nada.
     */
    fun descargarResultados(curso: String) {
        // Sin implementar todavía.
    }

    // =====================================================================
    // Módulo "Informe pedagógico"
    // =====================================================================

    fun informeDe(estudianteId: String?): InformeDocente? = _informes.value.find { it.estudianteId == estudianteId }

    /**
     * TODO(equipo): generar el informe del estudiante (rendimiento, fortalezas y aspectos a
     * reforzar) a partir de sus respuestas, en estado BORRADOR. Restricciones del caso: sin
     * diagnósticos ni consecuencias académicas automáticas. Hoy no hace nada.
     */
    fun generarInforme(estudianteId: String) {
        // Sin implementar todavía.
    }

    /**
     * TODO(equipo): pasar el informe de BORRADOR a VALIDADO. RF: "el informe debe quedar
     * sujeto a validación docente antes de considerarse definitivo". Hoy no hace nada.
     */
    fun validarInforme(informeId: String) {
        // Sin implementar todavía.
    }

    // =====================================================================
    // Módulo "Experiencia del estudiante"
    // =====================================================================

    /**
     * RF-07. Solo los contenidos asignados al curso del estudiante y aprobados por UTP.
     *
     * El cruce y el filtro los resuelve la consulta del DAO; acá se devuelve el último
     * valor emitido. Si el usuario no es estudiante o no tiene curso, la lista va vacía:
     * el estudiante nunca ve contenido que no le fue asignado (RN-01 y RN-02).
     */
    fun contenidosDelEstudiante(usuario: Usuario?): List<Contenido> = contenidosDelCurso.value

    /**
     * RF-11. Avance del estudiante en un contenido, de 0.0 a 1.0.
     *
     * Fórmula acordada por el equipo: el recorrido tiene tres etapas de igual peso.
     *   1) revisar el material      -> 1/3
     *   2) responder los desafíos   -> 1/3   (RF-08, a cargo de otro equipo)
     *   3) rendir la evaluación     -> 1/3   (RF-09, a cargo de otro equipo)
     *
     * Como este equipo implementa el RF-07, hoy el avance llega hasta 1/3 con el
     * material revisado (EN_PROGRESO). FINALIZADO queda reservado para cuando se
     * registre la evaluación final.
     */
    fun progreso(estudianteId: String, contenidoId: String): Float {
        val seguimiento = avances.value.firstOrNull {
            it.estudianteId == estudianteId && it.contenidoId == contenidoId
        } ?: return 0f

        return when (seguimiento.estado) {
            EstadoAvance.NO_INICIADO -> 0f
            EstadoAvance.EN_PROGRESO -> 1f / 3f
            EstadoAvance.FINALIZADO -> 1f
        }
    }

    /**
     * RF-11. Guarda en Room que el estudiante revisó el material del contenido.
     *
     * Se ejecuta en viewModelScope porque la escritura en la base es una función suspend:
     * Room no permite tocar la base de datos en el hilo principal (RNF-09).
     */
    fun confirmarMaterialRevisado(estudianteId: String, contenidoId: String) {
        if (estudianteId.isBlank() || contenidoId.isBlank()) return
        viewModelScope.launch {
            repositorio.marcarMaterialRevisado(estudianteId, contenidoId)
        }
    }

    /**
     * TODO(equipo): devolver los desafíos del contenido (sin la evaluación final), ordenados
     * por dificultad progresiva. Hoy devuelve TODAS las actividades, sin filtrar ni ordenar.
     */
    fun desafiosDe(contenidoId: String?): List<Actividad> = _actividades.value

    /**
     * TODO(equipo): registrar la respuesta en el seguimiento del estudiante y devolver si fue
     * correcta (true/false) para mostrar la retroalimentación. Hoy devuelve null.
     */
    fun responderDesafio(estudianteId: String, actividad: Actividad, alternativa: Int): Boolean? = null

    /** TODO(equipo): devolver solo las preguntas de la evaluación final del contenido. Hoy devuelve todas. */
    fun evaluacionFinalDe(contenidoId: String?): List<Actividad> = _actividades.value

    /**
     * TODO(equipo): intentos que le quedan al estudiante en la evaluación final (máximo definido
     * por el docente menos los usados). Regla de negocio: el máximo debe respetarse
     * estrictamente. Hoy devuelve null (sin calcular).
     */
    fun intentosRestantes(estudianteId: String, contenidoId: String): Int? = null

    /**
     * TODO(equipo): registrar las respuestas de la evaluación final como un nuevo intento
     * (solo si quedan intentos) y actualizar el seguimiento. Hoy no hace nada.
     * @param respuestas mapa idActividad -> índice de la alternativa elegida.
     */
    fun enviarEvaluacion(estudianteId: String, contenidoId: String, respuestas: Map<String, Int>) {
        // Sin implementar todavía.
    }

    /**
     * TODO(equipo): devolver las respuestas del estudiante en el contenido, para que revise
     * cuáles fueron correctas e incorrectas. RF: "revisar cuáles de sus respuestas fueron
     * correctas o incorrectas". Hoy devuelve una lista vacía.
     */
    fun respuestasDe(estudianteId: String, contenidoId: String): List<RespuestaEstudiante> = emptyList()
}