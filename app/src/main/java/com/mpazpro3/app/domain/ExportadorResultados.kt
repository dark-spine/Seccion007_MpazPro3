package com.mpazpro3.app.domain

import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAvance
import com.mpazpro3.app.model.Estudiante
import com.mpazpro3.app.model.SeguimientoAvance
import java.text.Normalizer

/** Una fila del archivo de resultados: un estudiante en un contenido asignado a su curso. */
data class FilaResultado(
    val curso: String,
    val estudianteId: String,
    val estudiante: String,
    val asignatura: String,
    val contenido: String,
    val avancePorcentaje: Int,
    val correctas: Int,
    val incorrectas: Int,
    val intentosUsados: Int,
    val maxIntentos: Int,
    val estado: String
)

/**
 * Arma el archivo CSV de resultados del curso (RF-06). Función PURA: devuelve el texto; guardarlo
 * o compartirlo es trabajo de la pantalla (recursos nativos del dispositivo).
 *
 * Formato: separador punto y coma (se abre directo en Excel con configuración regional de Chile),
 * texto entre comillas cuando hace falta y una fila por estudiante y contenido.
 * Solo contiene identificadores y nombres ficticios.
 */
object ExportadorResultados {

    const val SEPARADOR = ";"

    val ENCABEZADOS = listOf(
        "Curso", "Estudiante ID", "Estudiante", "Asignatura", "Contenido",
        "Avance (%)", "Correctas", "Incorrectas", "Intentos usados", "Máximo de intentos", "Estado"
    )

    fun generarCsv(filas: List<FilaResultado>): String {
        val lineas = mutableListOf(ENCABEZADOS.joinToString(SEPARADOR) { escapar(it) })
        filas.forEach { fila ->
            lineas += listOf(
                fila.curso, fila.estudianteId, fila.estudiante, fila.asignatura, fila.contenido,
                fila.avancePorcentaje.toString(), fila.correctas.toString(), fila.incorrectas.toString(),
                fila.intentosUsados.toString(), fila.maxIntentos.toString(), fila.estado
            ).joinToString(SEPARADOR) { escapar(it) }
        }
        return lineas.joinToString("\r\n") + "\r\n"
    }

    /**
     * Escapa un campo: lo encierra entre comillas si contiene separador, comillas o saltos de
     * línea, y neutraliza fórmulas de planilla (texto que parte con =, +, - o @).
     */
    fun escapar(campo: String): String {
        val seguro = if (campo.isNotEmpty() && campo[0] in "=+-@") "'$campo" else campo
        val necesitaComillas = seguro.any { it == ';' || it == '"' || it == '\n' || it == '\r' }
        return if (necesitaComillas) "\"" + seguro.replace("\"", "\"\"") + "\"" else seguro
    }

    /** Nombre del archivo sugerido, sin caracteres especiales (por ejemplo "resultados_3basicoA.csv"). */
    fun nombreArchivo(curso: String): String {
        val sinTildes = Normalizer.normalize(curso, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")
        val limpio = sinTildes.filter { it.isLetterOrDigit() }.ifEmpty { "curso" }
        return "resultados_$limpio.csv"
    }

    /**
     * Una fila por estudiante del curso y contenido asignado (y aprobado). Si el estudiante aún
     * no tiene seguimiento, la fila sale con ceros y estado "No iniciado".
     */
    fun construirFilas(
        curso: String,
        estudiantes: List<Estudiante>,
        contenidosAsignados: List<Contenido>,
        asignaciones: List<Asignacion>,
        actividades: List<Actividad>,
        seguimientos: List<SeguimientoAvance>
    ): List<FilaResultado> =
        estudiantes.filter { it.curso == curso }.flatMap { estudiante ->
            contenidosAsignados.map { contenido ->
                val seguimiento = seguimientos.find {
                    it.estudianteId == estudiante.id && it.contenidoId == contenido.id
                }
                val deContenido = actividades.filter { it.contenidoId == contenido.id }
                val vigentes = ReglasProgreso.respuestasVigentes(seguimiento?.respuestas.orEmpty(), deContenido)
                val correctas = ReglasProgreso.contarCorrectas(vigentes)
                FilaResultado(
                    curso = curso,
                    estudianteId = estudiante.id,
                    estudiante = estudiante.nombreFicticio,
                    asignatura = contenido.asignatura.etiqueta,
                    contenido = contenido.unidad,
                    avancePorcentaje = ReglasProgreso.porcentaje(ReglasProgreso.progresoContenido(seguimiento, deContenido)),
                    correctas = correctas,
                    incorrectas = vigentes.size - correctas,
                    intentosUsados = seguimiento?.intentosEvaluacionUsados ?: 0,
                    maxIntentos = asignaciones.find { it.curso == curso && it.contenidoId == contenido.id }
                        ?.maxIntentosEvaluacion ?: 0,
                    estado = (seguimiento?.estado ?: EstadoAvance.NO_INICIADO).etiqueta
                )
            }
        }
}
