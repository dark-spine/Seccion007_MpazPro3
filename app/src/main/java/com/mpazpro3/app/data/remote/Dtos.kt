package com.mpazpro3.app.data.remote

/*
 * Objetos de transferencia (DTO) con la misma forma que el JSON del servidor. Los nombres de los
 * campos coinciden con backend/data/seed.json y con los modelos del dominio. Todas las propiedades
 * tienen valor por defecto: así Gson usa el constructor sin argumentos y un campo ausente queda con
 * su valor por defecto en vez de null.
 */

// ---- Solicitudes --------------------------------------------------------------------------

data class LoginRequest(val usuario: String = "", val clave: String = "")

data class ObservacionRequest(val observacion: String = "")

data class AsignarRequest(val contenidoIds: List<String> = emptyList(), val maxIntentos: Int = 0)

data class RespuestaRequest(val alternativa: Int = 0)

data class EvaluacionRequest(val respuestas: Map<String, Int> = emptyMap())

// ---- Entidades ----------------------------------------------------------------------------

data class UsuarioDto(
    val id: String = "",
    val nombre: String = "",
    val usuario: String = "",
    val perfil: String = "",
    val curso: String? = null
)

data class EstudianteDto(
    val id: String = "",
    val nombreFicticio: String = "",
    val curso: String = ""
)

data class ContenidoDto(
    val id: String = "",
    val nivel: String = "",
    val asignatura: String = "",
    val unidad: String = "",
    val objetivoAprendizaje: String = "",
    val codigoOA: String? = null,
    val ejeCurricular: String = "",
    val recursoReferencia: String = "",
    val estado: String = "",
    val observacionUtp: String = ""
)

data class RecursoDto(
    val id: String = "",
    val contenidoId: String = "",
    val titulo: String = "",
    val explicacion: String = "",
    val ejemplo: String = "",
    val instrucciones: String = "",
    val descripcionImagen: String? = null,
    val audioDisponible: Boolean = false
)

data class AlternativaDto(
    val texto: String = "",
    val esCorrecta: Boolean = false
)

data class ActividadDto(
    val id: String = "",
    val contenidoId: String = "",
    val enunciado: String = "",
    val alternativas: List<AlternativaDto> = emptyList(),
    val dificultad: String = "",
    val orden: Int = 0,
    val esEvaluacionFinal: Boolean = false
)

data class AsignacionDto(
    val id: String = "",
    val curso: String = "",
    val contenidoId: String = "",
    val maxIntentosEvaluacion: Int = 0
)

data class RespuestaDto(
    val actividadId: String = "",
    val alternativaElegida: Int = 0,
    val esCorrecta: Boolean = false,
    val numeroIntento: Int = 0,
    val fecha: Long = 0L
)

data class SeguimientoDto(
    val estudianteId: String = "",
    val contenidoId: String = "",
    val materialRevisado: Boolean = false,
    val intentosEvaluacionUsados: Int = 0,
    val estado: String = "NO_INICIADO",
    val respuestas: List<RespuestaDto> = emptyList()
)

data class InformeDto(
    val id: String = "",
    val estudianteId: String = "",
    val rendimiento: String = "",
    val fortalezas: List<String> = emptyList(),
    val aspectosAReforzar: List<String> = emptyList(),
    val fecha: String = "",
    val estado: String = "BORRADOR"
)

// ---- Respuestas con envoltorio --------------------------------------------------------------

data class SaludDto(
    val estado: String = "",
    val servicio: String = "",
    val version: String = "",
    val hora: String = ""
)

data class LoginResponse(
    val token: String = "",
    val usuario: UsuarioDto = UsuarioDto()
)

data class ContenidoResponse(
    val mensaje: String = "",
    val contenido: ContenidoDto = ContenidoDto()
)

data class AsignarResponse(
    val mensaje: String = "",
    val asignaciones: List<AsignacionDto> = emptyList()
)

data class MaterialResponse(
    val mensaje: String = "",
    val seguimiento: SeguimientoDto = SeguimientoDto()
)

data class DesafioResponse(
    val esCorrecta: Boolean = false,
    val numeroIntento: Int = 0,
    val seguimiento: SeguimientoDto = SeguimientoDto()
)

data class EvaluacionResponse(
    val mensaje: String = "",
    val correctas: Int = 0,
    val total: Int = 0,
    val intentosRestantes: Int = 0,
    val seguimiento: SeguimientoDto = SeguimientoDto()
)

data class InformeResponse(
    val mensaje: String = "",
    val informe: InformeDto = InformeDto()
)

/** Cuerpo de error del servidor: { "error": "...", "campo": "..." }. */
data class ErrorDto(
    val error: String? = null,
    val campo: String? = null
)
