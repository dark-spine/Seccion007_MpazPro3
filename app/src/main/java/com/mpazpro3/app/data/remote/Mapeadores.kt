package com.mpazpro3.app.data.remote

import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Alternativa
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.Dificultad
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.EstadoAvance
import com.mpazpro3.app.model.EstadoValidacion
import com.mpazpro3.app.model.Estudiante
import com.mpazpro3.app.model.InformeDocente
import com.mpazpro3.app.model.Perfil
import com.mpazpro3.app.model.RecursoDidactico
import com.mpazpro3.app.model.RespuestaEstudiante
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario

/*
 * TODO(E3-4) Mapeadores DTO <-> modelo de dominio.
 *
 * Archivo: este. Dos funciones ya están hechas como ejemplo (Estudiante y Asignacion, en ambos sentidos);
 * completa las demás. Reglas:
 *  - Copiar campo a campo (los nombres coinciden con el JSON del servidor; ver Dtos.kt y los modelos de model/).
 *  - Los textos que representan enumeraciones (perfil, asignatura, estado, dificultad) se convierten con
 *    valueOf(...) al leer y con .name al escribir. Un valor desconocido lanza IllegalArgumentException: no la
 *    captures aquí; ApiResult.transformar la convierte en un Error "respuesta inválida" (MSG_RESPUESTA_INVALIDA).
 *  - Las alternativas de una actividad y las respuestas de un seguimiento son listas: mapear cada elemento.
 *  - El servidor nunca envía el hash de la clave: UsuarioDto.aDominio recibe el hash calculado por la app.
 * Aceptación: para cada tipo, dto.aDominio().aDto() == dto (ida y vuelta sin pérdidas) y un estado
 * inexistente (por ejemplo "ARCHIVADO") lanza IllegalArgumentException.
 */

// ---- DTO -> dominio ------------------------------------------------------------------------

/** El servidor no envía el hash de la clave; la app lo calcula con la clave escrita (para el ingreso sin conexión). */
fun UsuarioDto.aDominio(claveHash: String): Usuario = TODO("E3-4: UsuarioDto -> Usuario (Perfil.valueOf(perfil))")

// Ejemplo ya resuelto:
fun EstudianteDto.aDominio() = Estudiante(id = id, nombreFicticio = nombreFicticio, curso = curso)

fun ContenidoDto.aDominio(): Contenido = TODO("E3-4: ContenidoDto -> Contenido (Asignatura.valueOf, EstadoAprobacion.valueOf)")

fun RecursoDto.aDominio(): RecursoDidactico = TODO("E3-4: RecursoDto -> RecursoDidactico")

fun ActividadDto.aDominio(): Actividad = TODO("E3-4: ActividadDto -> Actividad (Alternativa(texto, esCorrecta), Dificultad.valueOf)")

// Ejemplo ya resuelto:
fun AsignacionDto.aDominio() = Asignacion(
    id = id, curso = curso, contenidoId = contenidoId, maxIntentosEvaluacion = maxIntentosEvaluacion
)

fun RespuestaDto.aDominio(): RespuestaEstudiante = TODO("E3-4: RespuestaDto -> RespuestaEstudiante")

fun SeguimientoDto.aDominio(): SeguimientoAvance = TODO("E3-4: SeguimientoDto -> SeguimientoAvance (EstadoAvance.valueOf, respuestas)")

fun InformeDto.aDominio(): InformeDocente = TODO("E3-4: InformeDto -> InformeDocente (EstadoValidacion.valueOf)")

// ---- Dominio -> DTO ------------------------------------------------------------------------

fun Usuario.aDto(): UsuarioDto = TODO("E3-4: Usuario -> UsuarioDto (perfil.name; la clave NO se envía)")

// Ejemplo ya resuelto:
fun Estudiante.aDto() = EstudianteDto(id = id, nombreFicticio = nombreFicticio, curso = curso)

fun Contenido.aDto(): ContenidoDto = TODO("E3-4: Contenido -> ContenidoDto (asignatura.name, estado.name)")

fun RecursoDidactico.aDto(): RecursoDto = TODO("E3-4: RecursoDidactico -> RecursoDto")

fun Actividad.aDto(): ActividadDto = TODO("E3-4: Actividad -> ActividadDto (AlternativaDto(texto, esCorrecta), dificultad.name)")

// Ejemplo ya resuelto:
fun Asignacion.aDto() = AsignacionDto(
    id = id, curso = curso, contenidoId = contenidoId, maxIntentosEvaluacion = maxIntentosEvaluacion
)

fun RespuestaEstudiante.aDto(): RespuestaDto = TODO("E3-4: RespuestaEstudiante -> RespuestaDto")

fun SeguimientoAvance.aDto(): SeguimientoDto = TODO("E3-4: SeguimientoAvance -> SeguimientoDto (estado.name, respuestas)")

fun InformeDocente.aDto(): InformeDto = TODO("E3-4: InformeDocente -> InformeDto (estado.name)")
