package com.mpazpro3.app.data

import com.mpazpro3.app.data.remote.ApiResult
import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.Estudiante
import com.mpazpro3.app.model.InformeDocente
import com.mpazpro3.app.model.RecursoDidactico
import com.mpazpro3.app.model.Resultado
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Interruptor de modo en memoria (en la app es SharedPreferences). */
class PreferenciaModoMemoria(local: Boolean = false) : PreferenciaModo {
    private val _flujo = MutableStateFlow(local)
    override val modoLocal: Boolean get() = _flujo.value
    override val modoLocalFlujo: StateFlow<Boolean> = _flujo.asStateFlow()
    override fun cambiar(local: Boolean) {
        _flujo.value = local
    }
}

/** Base local en memoria: guarda lo que RepositorioLocal le pide, igual que Room pero sin Android. */
class AlmacenLocalMemoria(
    private val inicial: () -> DatosLocales = { datosDeEjemplo() },
    val usuarios: List<Usuario> = DatosIniciales.usuarios
) : AlmacenLocal {

    var datos: DatosLocales = inicial()
    var restablecimientos = 0
    var transacciones = 0

    override suspend fun <T> transaccion(bloque: suspend () -> T): T {
        transacciones++
        return bloque()
    }

    override suspend fun leer(): DatosLocales = datos

    override suspend fun autenticar(usuarioNormalizado: String, claveHash: String): Usuario? =
        usuarios.firstOrNull { it.usuario.lowercase() == usuarioNormalizado && it.claveHash == claveHash }

    override suspend fun actualizarContenido(contenidoId: String, estado: EstadoAprobacion, observacion: String) {
        datos = datos.copy(
            contenidos = datos.contenidos.map {
                if (it.id == contenidoId) it.copy(estado = estado, observacionUtp = observacion) else it
            }
        )
    }

    override suspend fun reemplazarAsignacionesDelCurso(curso: String, asignaciones: List<Asignacion>) {
        datos = datos.copy(asignaciones = datos.asignaciones.filter { it.curso != curso } + asignaciones)
    }

    override suspend fun guardarSeguimiento(seguimiento: SeguimientoAvance) {
        datos = datos.copy(
            seguimientos = datos.seguimientos.filterNot {
                it.estudianteId == seguimiento.estudianteId && it.contenidoId == seguimiento.contenidoId
            } + seguimiento
        )
    }

    override suspend fun guardarInforme(informe: InformeDocente) {
        datos = datos.copy(informes = datos.informes.filter { it.estudianteId != informe.estudianteId } + informe)
    }

    override suspend fun restablecer() {
        datos = inicial()
        restablecimientos++
    }

    companion object {
        /** Los mismos datos de ejemplo que se cargan en Room la primera vez que se abre la app. */
        fun datosDeEjemplo() = DatosLocales(
            contenidos = DatosIniciales.contenidos,
            actividades = DatosIniciales.actividades,
            asignaciones = DatosIniciales.asignaciones,
            estudiantes = DatosIniciales.estudiantes,
            seguimientos = DatosIniciales.seguimientos
        )
    }
}

/** Fuente de datos que solo anota qué le pidieron y responde con su nombre (sirve para probar al selector). */
class FuenteFalsa(val nombre: String) : FuenteDatos {
    val llamadas = mutableListOf<String>()

    val estadoFlujo = MutableStateFlow<EstadoSync>(EstadoSync.Inactivo)
    val avisoFlujo = MutableStateFlow<String?>(null)
    val pendientesFlujo = MutableStateFlow(0)

    override val estado: Flow<EstadoSync> = estadoFlujo
    override val pendientes: Flow<Int> = pendientesFlujo
    override val aviso: Flow<String?> = avisoFlujo

    override fun publicarAviso(texto: String?) {
        llamadas += "publicarAviso"
        avisoFlujo.value = texto
    }

    override fun descartarAviso() {
        avisoFlujo.value = null
    }

    private fun registrar(operacion: String) {
        llamadas += operacion
    }

    override suspend fun iniciarSesion(usuario: String, clave: String): ResultadoLogin {
        registrar("iniciarSesion")
        return ResultadoLogin.Falla(nombre)
    }

    override suspend fun cerrarSesion() = registrar("cerrarSesion")

    override suspend fun sincronizar(usuario: Usuario): EstadoSync {
        registrar("sincronizar")
        return EstadoSync.Ok
    }

    override suspend fun aprobarContenido(contenidoId: String, observacion: String): Resultado {
        registrar("aprobarContenido")
        return Resultado.ok(nombre)
    }

    override suspend fun rechazarContenido(contenidoId: String, observacion: String): Resultado {
        registrar("rechazarContenido")
        return Resultado.ok(nombre)
    }

    override suspend fun asignarAlCurso(curso: String, contenidoIds: List<String>, maxIntentos: Int): Resultado {
        registrar("asignarAlCurso")
        return Resultado.ok(nombre)
    }

    override suspend fun confirmarMaterialRevisado(estudianteId: String, contenidoId: String): Resultado {
        registrar("confirmarMaterialRevisado")
        return Resultado.ok(nombre)
    }

    override suspend fun responderDesafio(estudianteId: String, actividadId: String, alternativa: Int): RespuestaDesafio {
        registrar("responderDesafio")
        return RespuestaDesafio(true, nombre)
    }

    override suspend fun enviarEvaluacion(estudianteId: String, contenidoId: String, respuestas: Map<String, Int>): Resultado {
        registrar("enviarEvaluacion")
        return Resultado.ok(nombre)
    }

    override suspend fun descargarResultadosCsv(curso: String): ApiResult<String> {
        registrar("descargarResultadosCsv")
        return ApiResult.Exito(nombre)
    }

    override suspend fun generarInforme(estudianteId: String): Resultado {
        registrar("generarInforme")
        return Resultado.ok(nombre)
    }

    override suspend fun validarInforme(informeId: String): Resultado {
        registrar("validarInforme")
        return Resultado.ok(nombre)
    }
}

/** Lecturas de una base: la lista de contenidos identifica de qué base vienen. */
class LecturasFalsas(idContenido: String) : LecturasObservables {
    val contenidosFlujo = MutableStateFlow(
        listOf(
            Contenido(
                id = idContenido, asignatura = Asignatura.LENGUAJE, unidad = "u", objetivoAprendizaje = "o",
                ejeCurricular = "e", recursoReferencia = "r"
            )
        )
    )

    override fun contenidos(): Flow<List<Contenido>> = contenidosFlujo
    override fun recursos(): Flow<List<RecursoDidactico>> = MutableStateFlow(emptyList())
    override fun actividades(): Flow<List<Actividad>> = MutableStateFlow(emptyList())
    override fun asignaciones(): Flow<List<Asignacion>> = MutableStateFlow(emptyList())
    override fun estudiantes(): Flow<List<Estudiante>> = MutableStateFlow(emptyList())
    override fun informes(): Flow<List<InformeDocente>> = MutableStateFlow(emptyList())
    override fun seguimientos(): Flow<List<SeguimientoAvance>> = MutableStateFlow(emptyList())
}
