package com.mpazpro3.app.model

/**
 * Los tres perfiles del caso (ver Requerimientos_Seccion007, "Actores"): UTP autoriza
 * contenidos, el Docente planifica y revisa resultados, el Estudiante desarrolla las actividades.
 */
enum class Perfil(val etiqueta: String) {
    UTP("Jefatura UTP"),
    DOCENTE("Docente de 3° básico"),
    ESTUDIANTE("Estudiante")
}
