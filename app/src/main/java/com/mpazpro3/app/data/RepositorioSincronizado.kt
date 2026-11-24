package com.mpazpro3.app.data

import com.mpazpro3.app.data.remote.ApiResult
import com.mpazpro3.app.data.remote.ColaPendientes
import com.mpazpro3.app.data.remote.Conectividad
import com.mpazpro3.app.data.remote.FuenteRemota
import com.mpazpro3.app.data.remote.OperacionPendiente
import com.mpazpro3.app.data.remote.SesionApi
import com.mpazpro3.app.model.Resultado
import com.mpazpro3.app.model.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repositorio que integra la API con la copia local (Room). Es la fuente del MODO SERVIDOR: SelectorFuente lo usa
 * mientras el interruptor "Modo local" está apagado (BuildConfig.USAR_API solo define el valor inicial del
 * interruptor). En modo local la app trabaja con RepositorioLocal (solo Room) y nada de esto se ejecuta.
 *
 * Qué debe lograr cuando lo completes:
 *  - lectura: API -> actualiza la copia local (Room) -> la interfaz observa Room (sincronizar);
 *  - escritura: primero la API (fuente de verdad); si sale bien, se refleja en Room. Si el servidor rechaza
 *    la acción (400/403/404/409/422) se muestra SU mensaje; sin conexión se avisa con claridad y NO se
 *    guarda nada, salvo en las dos acciones del estudiante que admiten cola de pendientes (opcionales, E3-8c/d);
 *  - las reglas de negocio las valida el servidor; aquí solo se hacen las comprobaciones baratas (motivo
 *    obligatorio, rango de intentos) con las mismas funciones puras de domain/ para responder sin esperar a la red.
 *
 * Ya están hechos: los estados (StateFlow), los avisos, la cola y los métodos auxiliares. Los TODO están
 * numerados E3-6 a E3-9; cada uno indica qué endpoint usar (ver ApiService y backend/docs/API.md).
 */
class RepositorioSincronizado(
    private val fuente: FuenteRemota,
    private val cache: CacheLocal,
    private val cola: ColaPendientes,
    private val conectividad: Conectividad,
    private val sesion: SesionApi
) : FuenteDatos {
    private val _estado = MutableStateFlow<EstadoSync>(EstadoSync.Inactivo)
    override val estado: StateFlow<EstadoSync> = _estado.asStateFlow()

    private val _pendientes = MutableStateFlow(cola.todas().size)
    override val pendientes: StateFlow<Int> = _pendientes.asStateFlow()

    private val _aviso = MutableStateFlow<String?>(null)
    override val aviso: StateFlow<String?> = _aviso.asStateFlow()

    override fun publicarAviso(texto: String?) {
        _aviso.value = texto
    }

    override fun descartarAviso() {
        _aviso.value = null
    }

    // =====================================================================
    // Auxiliares (ya hechos)
    // =====================================================================

    /** Mensaje para una acción de escritura que no se pudo enviar por falta de conexión. */
    private fun sinConexionEnAccion(mensaje: String) = "$mensaje No se guardó ningún cambio."

    /** Refleja en la copia local lo que el servidor ya aceptó; si falla, la próxima sincronización lo corrige. */
    private suspend fun reflejar(accion: suspend () -> Unit) {
        try {
            accion()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _aviso.value = "Se guardó en el servidor, pero no se pudo actualizar la copia del dispositivo. Actualiza los datos."
        }
    }

    /** Agrega una acción hecha sin conexión a la cola de pendientes (solo material revisado y desafíos de práctica). */
    private fun encolar(operacion: OperacionPendiente) {
        cola.agregar(operacion)
        _pendientes.value = cola.todas().size
    }

    // =====================================================================
    // Acceso (RF-00)
    // =====================================================================

    /**
     * TODO(E3-9a) Ingresa contra POST /auth/login.
     *
     * Archivo: este. Qué hacer:
     *  1. Calcular el hash local de la clave con ReglasAcceso.hashClave(clave) (sirve para el ingreso sin conexión).
     *  2. fuente.llamar(requiereSesion = false) { it.login(LoginRequest(usuario.trim(), clave)) }.
     *  3. Exito: convertir con UsuarioDto.aDominio(hash); si el token viene vacío -> ResultadoLogin.Falla(MSG_RESPUESTA_INVALIDA);
     *     si no, guardar el token en sesion.token, reflejar { cache.guardarUsuario(u) } y devolver Exito(u, enLinea = true).
     *  4. Error con código 400..499 (usuario/clave inválidos, 401, 422) -> Rechazado(mensaje, campo); otro Error -> Falla(mensaje).
     *  5. SinConexion -> ingresoSinConexion(usuario, hash, mensaje).
     * Aceptación: utp@mpazpro3.cl / utp123 entra y deja token en la sesión; clave incorrecta devuelve Rechazado con el
     * mensaje del servidor y NO deja token; servidor caído sin usuario guardado devuelve Falla.
     */
    override suspend fun iniciarSesion(usuario: String, clave: String): ResultadoLogin =
        TODO("E3-9a: login contra /auth/login")

    /**
     * TODO(E3-8e) Ingreso sin conexión: si el usuario ya ingresó antes en este dispositivo (cache.autenticarLocal con
     * ReglasAcceso.normalizarUsuario(usuario) y el [hash]) entra con sesion.limpiar() (sin token) y
     * ResultadoLogin.Exito(local, enLinea = false). Si no, ResultadoLogin.Falla("$motivo Tampoco hay datos guardados en
     * este dispositivo para ese usuario y clave.").
     */
    private suspend fun ingresoSinConexion(usuario: String, hash: String, motivo: String): ResultadoLogin =
        TODO("E3-8e: ingreso con los datos guardados en el dispositivo")

    /**
     * TODO(E3-9b) Olvida el token y cierra la sesión en el servidor (si se puede).
     *
     * Qué hacer: guardar el token actual, sesion.limpiar(), poner el estado en Inactivo y borrar el aviso; si había
     * token y hay red, llamar a logout("Bearer $token") ignorando el resultado (el cierre local nunca falla).
     * Aceptación: después de cerrarSesion, sesion.activa es false aunque el servidor no responda.
     */
    override suspend fun cerrarSesion() {
        TODO("E3-9b: limpiar la sesión y avisar al servidor")
    }

    // =====================================================================
    // Lectura: API -> copia local
    // =====================================================================

    /**
     * TODO(E3-6f) Descarga lo que el servidor entrega a este perfil y reemplaza la copia local.
     *
     * Archivo: este. Qué hacer:
     *  1. _estado.value = EstadoSync.Cargando.
     *  2. Sin red -> EstadoSync.SinConexion(MSG_SIN_CONEXION); sin token -> SinConexion(MSG_SIN_SESION_REMOTA).
     *  3. (opcional, E3-8f) enviar primero la cola de pendientes del usuario.
     *  4. fuente.descargarInstantanea(usuario.perfil): Exito -> cache.reemplazarTodo(instantanea.coherente()) y EstadoSync.Ok;
     *     SinConexion -> EstadoSync.SinConexion(mensaje); Error -> EstadoSync.Error(mensaje). Si Room falla,
     *     EstadoSync.Error("No se pudieron guardar los datos en el dispositivo.").
     *  5. Dejar el resultado en _estado y devolverlo.
     * Aceptación: con el servidor encendido, la pantalla del perfil muestra los datos del servidor (lo que aprueba UTP
     * se ve en el docente); con el servidor apagado la copia anterior sigue visible y el estado es SinConexion.
     */
    override suspend fun sincronizar(usuario: Usuario): EstadoSync =
        TODO("E3-6f: descargar del servidor y reemplazar la copia local")

    /**
     * TODO(E3-8f) (opcional, avanzado) Envía en orden las acciones guardadas sin conexión; se detiene si no hay red o
     * el servidor falla. Un 4xx que no sea 401/408/429 descarta la operación con un aviso. Actualiza _pendientes.
     */
    @Suppress("unused")
    private suspend fun vaciarPendientes(usuario: Usuario) {
        TODO("E3-8f: reenviar la cola de pendientes")
    }

    // =====================================================================
    // Autorización de contenidos (RF-01, RN-02, RN-04)
    // =====================================================================

    /**
     * TODO(E3-7a) UTP aprueba un contenido: POST contenidos/{id}/aprobar con ObservacionRequest(observacion).
     *
     * Qué hacer: fuente.llamar { it.aprobarContenido(id, ObservacionRequest(observacion)) } -> Exito: convertir
     * ContenidoResponse (contenido.aDominio()), reflejar { cache.guardarContenido(contenido) } y devolver
     * Resultado.ok(mensaje del servidor); Error: Resultado.error(mensaje del servidor, p. ej. el 409 de RN-04
     * "ya hay una unidad aprobada"); SinConexion: Resultado.error(sinConexionEnAccion(mensaje)).
     * Aceptación: aprobar un segundo contenido de la misma asignatura muestra el mensaje 409 del servidor y no cambia Room.
     */
    override suspend fun aprobarContenido(contenidoId: String, observacion: String): Resultado =
        TODO("E3-7a: aprobar contenido en el servidor y reflejarlo en Room")

    /**
     * TODO(E3-7b) UTP rechaza un contenido: POST contenidos/{id}/rechazar.
     * Antes de llamar, validar el motivo con ReglasContenido.errorObservacionAlRechazar(observacion) (si devuelve texto,
     * Resultado.error(texto) sin llamar al servidor). Luego igual que E3-7a.
     * Aceptación: rechazar sin motivo no hace ninguna petición; rechazar un contenido ya asignado muestra el 409.
     */
    override suspend fun rechazarContenido(contenidoId: String, observacion: String): Resultado =
        TODO("E3-7b: rechazar contenido (motivo obligatorio) en el servidor")

    // =====================================================================
    // Planificación docente (RF-04, RF-05, RN-02)
    // =====================================================================

    /**
     * TODO(E3-7c) El docente asigna contenidos al curso: PUT cursos/{curso}/asignaciones con AsignarRequest(ids sin repetir, maxIntentos).
     *
     * Qué hacer: validar antes con ReglasPlanificacion (curso vacío -> MENSAJE_SIN_CURSO, lista vacía ->
     * MENSAJE_SIN_SELECCION, errorMaxIntentos(maxIntentos)); luego llamar al servidor; Exito: reflejar
     * { cache.guardarAsignaciones(curso, asignaciones convertidas) } y Resultado.ok(mensaje); Error: su mensaje (409 si
     * hay un contenido no autorizado: todo o nada); SinConexion: sinConexionEnAccion.
     * Aceptación: asignar un contenido pendiente muestra el mensaje del servidor y no crea ninguna asignación.
     */
    override suspend fun asignarAlCurso(curso: String, contenidoIds: List<String>, maxIntentos: Int): Resultado =
        TODO("E3-7c: asignar contenidos al curso en el servidor")

    // =====================================================================
    // Experiencia del estudiante (RF-08, RF-09, RF-11, RN-03)
    // =====================================================================

    /**
     * TODO(E3-7d) El estudiante marca el material como revisado: POST estudiantes/{id}/contenidos/{id}/material-revisado.
     *
     * Exito: reflejar { cache.guardarSeguimiento(seguimiento) } y Resultado.ok. Error: su mensaje. SinConexion: mensaje
     * claro y no se guarda (Opcional avanzado E3-8c: cache.marcarMaterialLocal + encolar la operación y avisar que se
     * enviará al volver la red).
     */
    override suspend fun confirmarMaterialRevisado(estudianteId: String, contenidoId: String): Resultado =
        TODO("E3-7d: confirmar material revisado en el servidor")

    /**
     * TODO(E3-7e) Respuesta a un desafío: POST estudiantes/{id}/actividades/{id}/respuestas con RespuestaRequest(alternativa).
     *
     * Exito: la respuesta trae esCorrecta y el seguimiento actualizado -> reflejar y RespuestaDesafio(esCorrecta).
     * Error: RespuestaDesafio(null, mensaje del servidor). SinConexion: RespuestaDesafio(null, mensaje claro)
     * (Opcional avanzado E3-8d: cache.registrarRespuestaLocal + encolar y RespuestaDesafio(correcta, aviso, guardadaSinConexion = true)).
     */
    override suspend fun responderDesafio(estudianteId: String, actividadId: String, alternativa: Int): RespuestaDesafio =
        TODO("E3-7e: registrar la respuesta del desafío en el servidor")

    /**
     * TODO(E3-7f) RN-03: evaluación final: POST estudiantes/{id}/contenidos/{id}/evaluacion con EvaluacionRequest(respuestas).
     *
     * El servidor cuenta los intentos: nunca se cuenta ni se guarda un intento sin conexión. Exito: reflejar el
     * seguimiento y Resultado.ok(mensaje). Error 409 (sin intentos): su mensaje. SinConexion: Resultado.error con
     * "... La evaluación final necesita conexión para respetar el máximo de intentos. No se registró ningún intento."
     * Aceptación: con el máximo agotado se muestra el 409 y el contador de intentos de Room no cambia.
     */
    override suspend fun enviarEvaluacion(estudianteId: String, contenidoId: String, respuestas: Map<String, Int>): Resultado =
        TODO("E3-7f: enviar la evaluación final al servidor (RN-03)")

    // =====================================================================
    // Resultados (RF-06) e informe (RF-12, RF-13)
    // =====================================================================

    /**
     * TODO(E3-7g) CSV de resultados generado por el servidor: GET cursos/{curso}/resultados.csv (ResponseBody).
     * Devolver el texto con fuente.llamar { it.resultadosCsv(curso).string() }.
     * Aceptación: el texto empieza con la fila de encabezados "Curso;Estudiante ID;..." y no está vacío.
     */
    override suspend fun descargarResultadosCsv(curso: String): ApiResult<String> =
        TODO("E3-7g: pedir el CSV de resultados al servidor")

    /**
     * TODO(E3-7h) Genera el informe del estudiante: POST estudiantes/{id}/informe. Exito: reflejar
     * { cache.guardarInforme(informe) } y Resultado.ok(mensaje). Error: su mensaje (409 si el informe ya está validado).
     */
    override suspend fun generarInforme(estudianteId: String): Resultado =
        TODO("E3-7h: generar el informe en el servidor")

    /**
     * TODO(E3-7i) Valida el informe: POST informes/{id}/validar. Exito: reflejar { cache.guardarInforme(informe) } y Resultado.ok(mensaje).
     */
    override suspend fun validarInforme(informeId: String): Resultado =
        TODO("E3-7i: validar el informe en el servidor")
}
