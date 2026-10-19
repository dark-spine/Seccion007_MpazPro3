package com.mpazpro3.app.data

import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Alternativa
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.Dificultad
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.Estudiante
import com.mpazpro3.app.model.Perfil
import com.mpazpro3.app.model.RecursoDidactico
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario

/**
 * Datos FICTICIOS de ejemplo (el caso prohíbe datos reales de estudiantes, nóminas o notas).
 * Los textos pedagógicos son de ejemplo: cada equipo debe reemplazarlos por la unidad y los OA
 * que se definan con el CITT, usando material propio, público o autorizado.
 */
object MockData {

    /** Usuarios de prueba para el login. MpazProViewModel.login() todavía NO los usa. */
    val usuarios = listOf(
        Usuario("U1", "Jefatura UTP (demo)", "utp@mpazpro3.cl", "utp123", Perfil.UTP),
        Usuario("U2", "Docente 3° básico A (demo)", "docente.3basicoA@mpazpro3.cl", "docente123", Perfil.DOCENTE, "3° básico A"),
        Usuario("EST-001", "Estudiante 1 (demo)", "est001", "1234", Perfil.ESTUDIANTE, "3° básico A"),
    )

    val estudiantes = listOf(
        Estudiante("EST-001", "Estudiante 1", "3° básico A"),
        Estudiante("EST-002", "Estudiante 2", "3° básico A"),
        Estudiante("EST-003", "Estudiante 3", "3° básico A"),
        Estudiante("EST-004", "Estudiante 4", "3° básico A"),
    )

    val contenidos = listOf(
        Contenido(
            id = "C1", asignatura = Asignatura.LENGUAJE, unidad = "Unidad de ejemplo - Comprensión lectora",
            objetivoAprendizaje = "Leer en voz alta de manera fluida textos variados apropiados a su edad.",
            codigoOA = "OA (por confirmar)", ejeCurricular = "Lectura",
            recursoReferencia = "Currículum Nacional - 3° básico", estado = EstadoAprobacion.PENDIENTE
        ),
        Contenido(
            id = "C2", asignatura = Asignatura.MATEMATICA, unidad = "Unidad de ejemplo - Números hasta el 1.000",
            objetivoAprendizaje = "Representar y describir números del 0 al 1.000.",
            codigoOA = "OA (por confirmar)", ejeCurricular = "Números y operaciones",
            recursoReferencia = "Currículum Nacional - 3° básico", estado = EstadoAprobacion.APROBADO
        ),
        Contenido(
            id = "C3", asignatura = Asignatura.CIENCIAS_NATURALES, unidad = "Unidad de ejemplo - Seres vivos",
            objetivoAprendizaje = "Observar y describir necesidades básicas de los seres vivos.",
            codigoOA = "OA (por confirmar)", ejeCurricular = "Ciencias de la vida",
            recursoReferencia = "Currículum Nacional - 3° básico", estado = EstadoAprobacion.PENDIENTE
        ),
        Contenido(
            id = "C4", asignatura = Asignatura.HISTORIA, unidad = "Unidad de ejemplo - Ubicación espacial",
            objetivoAprendizaje = "Ubicar lugares en un mapa usando puntos cardinales.",
            codigoOA = "OA (por confirmar)", ejeCurricular = "Geografía",
            recursoReferencia = "Currículum Nacional - 3° básico", estado = EstadoAprobacion.RECHAZADO,
            observacionUtp = "Ejemplo de observación: falta indicar el recurso de referencia exacto."
        ),
    )

    val recursos = listOf(
        RecursoDidactico(
            id = "R1", contenidoId = "C2", titulo = "¿Qué son las centenas?",
            explicacion = "Una centena es un grupo de 100 unidades. El número 452 tiene 4 centenas, 5 decenas y 2 unidades.",
            ejemplo = "300 = 3 centenas. 250 = 2 centenas y 5 decenas.",
            instrucciones = "Lee la explicación y el ejemplo. Después, resuelve los desafíos.",
            descripcionImagen = "Bloques de base diez (imagen de ejemplo)", audioDisponible = true
        ),
        RecursoDidactico(
            id = "R2", contenidoId = "C1", titulo = "Leer con fluidez",
            explicacion = "Leer con fluidez es leer sin detenerse en cada palabra, respetando los puntos y las comas.",
            ejemplo = "Lee esta oración: \"El perro corre en el parque.\"",
            instrucciones = "Lee en voz alta la oración del ejemplo dos veces."
        ),
    )

    val actividades = listOf(
        Actividad("A1", "C2", "¿Cuántas centenas tiene el número 300?",
            listOf(Alternativa("3", true), Alternativa("30", false), Alternativa("300", false)), Dificultad.FACIL, orden = 1),
        Actividad("A2", "C2", "¿Qué número tiene 4 centenas, 5 decenas y 2 unidades?",
            listOf(Alternativa("254", false), Alternativa("452", true), Alternativa("425", false)), Dificultad.MEDIO, orden = 2),
        Actividad("A3", "C2", "¿Qué número es mayor?",
            listOf(Alternativa("609", false), Alternativa("690", true), Alternativa("096", false)), Dificultad.DIFICIL, orden = 3),
        Actividad("E1", "C2", "Evaluación final: ¿cuántas decenas tiene el número 570?",
            listOf(Alternativa("7", true), Alternativa("5", false), Alternativa("57", false)), Dificultad.MEDIO, orden = 1, esEvaluacionFinal = true),
        Actividad("E2", "C2", "Evaluación final: ¿qué número va entre 199 y 201?",
            listOf(Alternativa("200", true), Alternativa("210", false), Alternativa("190", false)), Dificultad.MEDIO, orden = 2, esEvaluacionFinal = true),
    )

    /** Ejemplo: el docente ya asignó Matemática (contenido aprobado) a su curso, con 2 intentos. */
    val asignaciones = listOf(
        Asignacion("AS1", "3° básico A", "C2", maxIntentosEvaluacion = 2)
    )

    val seguimientos = listOf(
        SeguimientoAvance(estudianteId = "EST-001", contenidoId = "C2")
    )
}
