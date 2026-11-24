package com.mpazpro3.app.data

import com.mpazpro3.app.domain.ReglasAcceso
import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Alternativa
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.Dificultad
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.EstadoAvance
import com.mpazpro3.app.model.Estudiante
import com.mpazpro3.app.model.Perfil
import com.mpazpro3.app.model.RecursoDidactico
import com.mpazpro3.app.model.RespuestaEstudiante
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario

/**
 * Datos iniciales FICTICIOS que se cargan en Room la primera vez que se abre la app (RNF-04).
 *
 * Una sola unidad por asignatura (RN-04). Los textos pedagógicos son propios del proyecto
 * (RNF-05) y quedan sujetos a la revisión de la Jefatura UTP: por eso Lenguaje y Ciencias parten
 * PENDIENTE, Historia parte RECHAZADA (con una observación de ejemplo) y Matemática APROBADA.
 *
 * Este archivo es Kotlin puro (sin Android) para poder usarse también en las pruebas unitarias.
 */
object DatosIniciales {

    const val CURSO = "3° básico A"

    private const val REFERENCIA_BASES = "Bases Curriculares 3° básico (Mineduc), Currículum Nacional"
    private const val CODIGO_POR_CONFIRMAR = "OA (por confirmar)"

    /** Fecha fija de las respuestas de ejemplo (milisegundos desde 1970). */
    private const val FECHA_EJEMPLO = 1_790_000_000_000L

    private const val CLAVE_ESTUDIANTES = "1234"

    val usuarios = listOf(
        Usuario("U1", "Jefatura UTP (demo)", "utp@mpazpro3.cl", ReglasAcceso.hashClave("utp123"), Perfil.UTP),
        Usuario(
            "U2", "Docente 3° básico A (demo)", "docente.3basicoA@mpazpro3.cl",
            ReglasAcceso.hashClave("docente123"), Perfil.DOCENTE, CURSO
        ),
        Usuario("EST-001", "Estudiante 1 (demo)", "est001", ReglasAcceso.hashClave(CLAVE_ESTUDIANTES), Perfil.ESTUDIANTE, CURSO),
        Usuario("EST-002", "Estudiante 2 (demo)", "est002", ReglasAcceso.hashClave(CLAVE_ESTUDIANTES), Perfil.ESTUDIANTE, CURSO),
        Usuario("EST-003", "Estudiante 3 (demo)", "est003", ReglasAcceso.hashClave(CLAVE_ESTUDIANTES), Perfil.ESTUDIANTE, CURSO),
        Usuario("EST-004", "Estudiante 4 (demo)", "est004", ReglasAcceso.hashClave(CLAVE_ESTUDIANTES), Perfil.ESTUDIANTE, CURSO),
    )

    val estudiantes = listOf(
        Estudiante("EST-001", "Estudiante 1", CURSO),
        Estudiante("EST-002", "Estudiante 2", CURSO),
        Estudiante("EST-003", "Estudiante 3", CURSO),
        Estudiante("EST-004", "Estudiante 4", CURSO),
    )

    val contenidos = listOf(
        Contenido(
            id = "C1", asignatura = Asignatura.LENGUAJE,
            unidad = "Unidad 1 - Leer con fluidez y comprender",
            objetivoAprendizaje = "Leer en voz alta de manera fluida textos variados apropiados a su edad, respetando los signos de puntuación.",
            codigoOA = CODIGO_POR_CONFIRMAR, ejeCurricular = "Lectura",
            recursoReferencia = REFERENCIA_BASES, estado = EstadoAprobacion.PENDIENTE
        ),
        Contenido(
            id = "C2", asignatura = Asignatura.MATEMATICA,
            unidad = "Unidad 1 - Números hasta el 1.000",
            objetivoAprendizaje = "Representar y describir números del 0 al 1.000.",
            codigoOA = CODIGO_POR_CONFIRMAR, ejeCurricular = "Números y operaciones",
            recursoReferencia = REFERENCIA_BASES, estado = EstadoAprobacion.APROBADO
        ),
        Contenido(
            id = "C3", asignatura = Asignatura.CIENCIAS_NATURALES,
            unidad = "Unidad 1 - Los seres vivos y sus necesidades",
            objetivoAprendizaje = "Observar y describir las necesidades básicas de los seres vivos: agua, aire, alimento y luz.",
            codigoOA = CODIGO_POR_CONFIRMAR, ejeCurricular = "Ciencias de la vida",
            recursoReferencia = REFERENCIA_BASES, estado = EstadoAprobacion.PENDIENTE
        ),
        Contenido(
            id = "C4", asignatura = Asignatura.HISTORIA,
            unidad = "Unidad 1 - Ubicarnos en el espacio",
            objetivoAprendizaje = "Ubicar lugares en un mapa usando los puntos cardinales.",
            codigoOA = CODIGO_POR_CONFIRMAR, ejeCurricular = "Geografía",
            recursoReferencia = "Currículum Nacional - 3° básico",
            estado = EstadoAprobacion.RECHAZADO,
            observacionUtp = "Falta indicar el recurso de referencia exacto de la unidad."
        ),
    )

    val recursos = listOf(
        RecursoDidactico(
            id = "R1", contenidoId = "C2", titulo = "¿Qué son las centenas?",
            explicacion = "Una centena es un grupo de 100 unidades. El número 452 tiene 4 centenas, 5 decenas y 2 unidades.",
            ejemplo = "300 = 3 centenas. 250 = 2 centenas y 5 decenas.",
            instrucciones = "Lee la explicación y el ejemplo. Después, resuelve los desafíos.",
            descripcionImagen = "Bloques de base diez: 4 placas de 100, 5 barras de 10 y 2 cubos de 1 forman el número 452.",
            audioDisponible = true
        ),
        RecursoDidactico(
            id = "R2", contenidoId = "C1", titulo = "Leer con fluidez",
            explicacion = "Leer con fluidez es leer sin detenerse en cada palabra, respetando los puntos y las comas. " +
                "El punto indica una pausa larga y la coma, una pausa corta.",
            ejemplo = "Lee esta oración: \"El perro corre en el parque.\"",
            instrucciones = "Lee en voz alta la oración del ejemplo dos veces.",
            descripcionImagen = "Un libro abierto con un punto y una coma, los signos que marcan las pausas al leer."
        ),
        RecursoDidactico(
            id = "R3", contenidoId = "C3", titulo = "Lo que necesitan los seres vivos",
            explicacion = "Las plantas, los animales y las personas son seres vivos. Todos necesitan agua, aire y alimento " +
                "para vivir. Las plantas también necesitan la luz del sol para crecer.",
            ejemplo = "Una planta sin agua se seca. Un perro necesita comer y beber todos los días.",
            instrucciones = "Mira la imagen y lee la explicación. Después, resuelve los desafíos.",
            descripcionImagen = "Una planta con el sol arriba y una gota de agua al lado: la luz y el agua ayudan a crecer."
        ),
        RecursoDidactico(
            id = "R4", contenidoId = "C4", titulo = "Los puntos cardinales",
            explicacion = "Los puntos cardinales nos ayudan a ubicarnos: norte (N), sur (S), este (E) y oeste (O). " +
                "El sol sale por el este y se esconde por el oeste.",
            ejemplo = "Si miras al norte, el sur queda a tu espalda, el este a tu derecha y el oeste a tu izquierda.",
            instrucciones = "Mira la rosa de los vientos y lee la explicación. Después, resuelve los desafíos.",
            descripcionImagen = "Rosa de los vientos: el norte apunta arriba, el sur abajo, el este a la derecha y el oeste a la izquierda."
        ),
    )

    private fun alt(texto: String, esCorrecta: Boolean = false) = Alternativa(texto, esCorrecta)

    val actividades = listOf(
        // ---- C2 Matemática ----
        Actividad("A1", "C2", "¿Cuántas centenas tiene el número 300?",
            listOf(alt("3", true), alt("30"), alt("300")), Dificultad.FACIL, orden = 1),
        Actividad("A2", "C2", "¿Qué número tiene 4 centenas, 5 decenas y 2 unidades?",
            listOf(alt("254"), alt("452", true), alt("425")), Dificultad.MEDIO, orden = 2),
        Actividad("A3", "C2", "¿Qué número es mayor?",
            listOf(alt("609"), alt("690", true), alt("096")), Dificultad.DIFICIL, orden = 3),
        Actividad("E1", "C2", "Evaluación final: ¿cuántas decenas tiene el número 570?",
            listOf(alt("7", true), alt("5"), alt("57")), Dificultad.MEDIO, orden = 1, esEvaluacionFinal = true),
        Actividad("E2", "C2", "Evaluación final: ¿qué número va entre 199 y 201?",
            listOf(alt("200", true), alt("210"), alt("190")), Dificultad.MEDIO, orden = 2, esEvaluacionFinal = true),

        // ---- C1 Lenguaje ----
        Actividad("A4", "C1", "¿Qué signo usamos al final de una oración para mostrar que terminó?",
            listOf(alt("La coma"), alt("El punto", true), alt("Las comillas")), Dificultad.FACIL, orden = 1),
        Actividad("A5", "C1", "Lee: \"El perro corre en el parque.\" ¿Dónde corre el perro?",
            listOf(alt("En la playa"), alt("En la casa"), alt("En el parque", true)), Dificultad.MEDIO, orden = 2),
        Actividad("A6", "C1", "Cuando leemos en voz alta, ¿qué nos indica la coma?",
            listOf(alt("Una pausa corta", true), alt("Que debemos gritar"), alt("Que el texto terminó")), Dificultad.DIFICIL, orden = 3),
        Actividad("E3", "C1", "Evaluación final: ¿qué signos usamos para escribir una pregunta?",
            listOf(alt("Los signos de exclamación ¡ !"), alt("Los signos de interrogación ¿ ?", true), alt("Las comillas")),
            Dificultad.MEDIO, orden = 1, esEvaluacionFinal = true),
        Actividad("E4", "C1", "Evaluación final: lee \"Ana guarda sus libros en la mochila.\" ¿Qué guarda Ana?",
            listOf(alt("Sus juguetes"), alt("Su ropa"), alt("Sus libros", true)),
            Dificultad.MEDIO, orden = 2, esEvaluacionFinal = true),

        // ---- C3 Ciencias Naturales ----
        Actividad("A7", "C3", "¿Cuál de estos es un ser vivo?",
            listOf(alt("Una piedra"), alt("Un gato", true), alt("Una silla")), Dificultad.FACIL, orden = 1),
        Actividad("A8", "C3", "¿Qué necesitan todos los seres vivos para vivir?",
            listOf(alt("Juguetes y libros"), alt("Zapatos y ropa"), alt("Agua, aire y alimento", true)), Dificultad.MEDIO, orden = 2),
        Actividad("A9", "C3", "Además de agua y aire, ¿qué necesita una planta para crecer?",
            listOf(alt("Luz del sol", true), alt("Música"), alt("Un paraguas")), Dificultad.DIFICIL, orden = 3),
        Actividad("E5", "C3", "Evaluación final: ¿qué le pasa a una planta que no recibe agua?",
            listOf(alt("Crece más rápido"), alt("Se seca", true), alt("Se convierte en animal")),
            Dificultad.MEDIO, orden = 1, esEvaluacionFinal = true),
        Actividad("E6", "C3", "Evaluación final: ¿cuál de estos NO es un ser vivo?",
            listOf(alt("Un árbol"), alt("Una mariposa"), alt("Una roca", true)),
            Dificultad.MEDIO, orden = 2, esEvaluacionFinal = true),

        // ---- C4 Historia, Geografía y Ciencias Sociales ----
        Actividad("A10", "C4", "¿Cuántos puntos cardinales hay?",
            listOf(alt("Tres"), alt("Cuatro", true), alt("Cinco")), Dificultad.FACIL, orden = 1),
        Actividad("A11", "C4", "¿Por qué punto cardinal sale el sol?",
            listOf(alt("Este", true), alt("Oeste"), alt("Sur")), Dificultad.MEDIO, orden = 2),
        Actividad("A12", "C4", "Miras al norte. ¿Hacia dónde queda el sur?",
            listOf(alt("A tu derecha"), alt("A tu espalda", true), alt("Frente a ti")), Dificultad.DIFICIL, orden = 3),
        Actividad("E7", "C4", "Evaluación final: ¿cuál es el punto cardinal opuesto al norte?",
            listOf(alt("El sur", true), alt("El este"), alt("El oeste")),
            Dificultad.MEDIO, orden = 1, esEvaluacionFinal = true),
        Actividad("E8", "C4", "Evaluación final: ¿por dónde se esconde el sol?",
            listOf(alt("Por el norte"), alt("Por el oeste", true), alt("Por el este")),
            Dificultad.MEDIO, orden = 2, esEvaluacionFinal = true),
    )

    /** El docente ya asignó Matemática (contenido aprobado) a su curso, con 2 intentos. */
    val asignaciones = listOf(
        Asignacion("AS1", CURSO, "C2", maxIntentosEvaluacion = 2)
    )

    /**
     * Avance de ejemplo para que el docente vea datos al abrir sus resultados:
     *  - EST-001 todavía no empieza;
     *  - EST-002 terminó los desafíos y rindió un intento de la evaluación;
     *  - EST-003 revisó el material y respondió dos desafíos.
     * EST-004 no tiene seguimiento (avance 0 %).
     */
    val seguimientos = listOf(
        SeguimientoAvance(estudianteId = "EST-001", contenidoId = "C2"),
        SeguimientoAvance(
            estudianteId = "EST-002", contenidoId = "C2",
            respuestas = listOf(
                RespuestaEstudiante("A1", 0, true, 1, FECHA_EJEMPLO),
                RespuestaEstudiante("A2", 0, false, 1, FECHA_EJEMPLO),
                RespuestaEstudiante("A2", 1, true, 2, FECHA_EJEMPLO),
                RespuestaEstudiante("A3", 1, true, 1, FECHA_EJEMPLO),
                RespuestaEstudiante("E1", 0, true, 1, FECHA_EJEMPLO),
                RespuestaEstudiante("E2", 1, false, 1, FECHA_EJEMPLO),
            ),
            intentosEvaluacionUsados = 1, estado = EstadoAvance.FINALIZADO, materialRevisado = true
        ),
        SeguimientoAvance(
            estudianteId = "EST-003", contenidoId = "C2",
            respuestas = listOf(
                RespuestaEstudiante("A1", 0, true, 1, FECHA_EJEMPLO),
                RespuestaEstudiante("A2", 0, false, 1, FECHA_EJEMPLO),
            ),
            estado = EstadoAvance.EN_PROGRESO, materialRevisado = true
        ),
    )
}
