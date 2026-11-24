package com.mpazpro3.app.data.local

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
 * Traducción entre las entidades de Room (texto, números y booleanos) y los modelos del dominio
 * (enums y listas). Las reglas de negocio trabajan solo con los modelos del dominio.
 */

private const val SEPARADOR_LISTA = "\n"

fun UsuarioEntity.aDominio() = Usuario(
    id = id, nombre = nombre, usuario = usuario, claveHash = claveHash,
    perfil = Perfil.valueOf(perfil), curso = curso
)

fun Usuario.aEntidad() = UsuarioEntity(
    id = id, nombre = nombre, usuario = usuario, claveHash = claveHash,
    perfil = perfil.name, curso = curso
)

fun EstudianteEntity.aDominio() = Estudiante(id, nombreFicticio, curso)
fun Estudiante.aEntidad() = EstudianteEntity(id, nombreFicticio, curso)

fun ContenidoEntity.aDominio() = Contenido(
    id = id, nivel = nivel, asignatura = Asignatura.valueOf(asignatura), unidad = unidad,
    objetivoAprendizaje = objetivoAprendizaje, codigoOA = codigoOA, ejeCurricular = ejeCurricular,
    recursoReferencia = recursoReferencia, estado = EstadoAprobacion.valueOf(estado),
    observacionUtp = observacionUtp
)

fun Contenido.aEntidad() = ContenidoEntity(
    id = id, nivel = nivel, asignatura = asignatura.name, unidad = unidad,
    objetivoAprendizaje = objetivoAprendizaje, codigoOA = codigoOA, ejeCurricular = ejeCurricular,
    recursoReferencia = recursoReferencia, estado = estado.name, observacionUtp = observacionUtp
)

fun RecursoEntity.aDominio() = RecursoDidactico(
    id = id, contenidoId = contenidoId, titulo = titulo, explicacion = explicacion, ejemplo = ejemplo,
    instrucciones = instrucciones, descripcionImagen = descripcionImagen, audioDisponible = audioDisponible
)

fun RecursoDidactico.aEntidad() = RecursoEntity(
    id = id, contenidoId = contenidoId, titulo = titulo, explicacion = explicacion, ejemplo = ejemplo,
    instrucciones = instrucciones, descripcionImagen = descripcionImagen, audioDisponible = audioDisponible
)

fun ActividadConAlternativas.aDominio() = Actividad(
    id = actividad.id,
    contenidoId = actividad.contenidoId,
    enunciado = actividad.enunciado,
    alternativas = alternativas.sortedBy { it.posicion }.map { Alternativa(it.texto, it.esCorrecta) },
    dificultad = Dificultad.valueOf(actividad.dificultad),
    orden = actividad.orden,
    esEvaluacionFinal = actividad.esEvaluacionFinal
)

fun Actividad.aEntidad() = ActividadEntity(
    id = id, contenidoId = contenidoId, enunciado = enunciado, dificultad = dificultad.name,
    orden = orden, esEvaluacionFinal = esEvaluacionFinal
)

fun Actividad.alternativasAEntidad(): List<AlternativaEntity> = alternativas.mapIndexed { posicion, alternativa ->
    AlternativaEntity(actividadId = id, texto = alternativa.texto, esCorrecta = alternativa.esCorrecta, posicion = posicion)
}

fun AsignacionEntity.aDominio() = Asignacion(id, curso, contenidoId, maxIntentosEvaluacion)
fun Asignacion.aEntidad() = AsignacionEntity(id, curso, contenidoId, maxIntentosEvaluacion)

fun RespuestaEntity.aDominio() = RespuestaEstudiante(
    actividadId = actividadId, alternativaElegida = alternativaElegida, esCorrecta = esCorrecta,
    numeroIntento = numeroIntento, fecha = fecha
)

/** Arma el seguimiento del dominio con sus respuestas (que viven en su propia tabla). */
fun SeguimientoEntity.aDominio(respuestas: List<RespuestaEstudiante>) = SeguimientoAvance(
    estudianteId = estudianteId, contenidoId = contenidoId, respuestas = respuestas,
    intentosEvaluacionUsados = intentosEvaluacionUsados, estado = EstadoAvance.valueOf(estado),
    materialRevisado = materialRevisado
)

fun SeguimientoAvance.aEntidad() = SeguimientoEntity(
    estudianteId = estudianteId, contenidoId = contenidoId, materialRevisado = materialRevisado,
    intentosEvaluacionUsados = intentosEvaluacionUsados, estado = estado.name
)

/** Filas de la tabla "respuestas" de un seguimiento (las respuestas viven en su propia tabla). */
fun SeguimientoAvance.respuestasAEntidad(): List<RespuestaEntity> = respuestas.map { respuesta ->
    RespuestaEntity(
        estudianteId = estudianteId, actividadId = respuesta.actividadId, contenidoId = contenidoId,
        alternativaElegida = respuesta.alternativaElegida, esCorrecta = respuesta.esCorrecta,
        numeroIntento = respuesta.numeroIntento, fecha = respuesta.fecha
    )
}

fun InformeEntity.aDominio() = InformeDocente(
    id = id, estudianteId = estudianteId, rendimiento = rendimiento,
    fortalezas = fortalezas.split(SEPARADOR_LISTA).filter { it.isNotBlank() },
    aspectosAReforzar = aspectosAReforzar.split(SEPARADOR_LISTA).filter { it.isNotBlank() },
    fecha = fecha, estado = EstadoValidacion.valueOf(estado)
)

fun InformeDocente.aEntidad() = InformeEntity(
    id = id, estudianteId = estudianteId, rendimiento = rendimiento,
    fortalezas = fortalezas.joinToString(SEPARADOR_LISTA),
    aspectosAReforzar = aspectosAReforzar.joinToString(SEPARADOR_LISTA),
    fecha = fecha, estado = estado.name
)
