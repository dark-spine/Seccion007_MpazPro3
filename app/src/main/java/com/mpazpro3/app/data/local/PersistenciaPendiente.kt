package com.mpazpro3.app.data.local

// =====================================================================================
// TODO(equipo): PERSISTENCIA LOCAL (contenido de la Semana 9)
// =====================================================================================
// Las dependencias de Room, KSP y DataStore YA están configuradas en Gradle. Falta crear
// las clases en esta carpeta (data/local/) y reemplazar las listas de MockData que hoy usa
// MpazProViewModel. El caso exige conservar el progreso del estudiante.
//
// 1) ENTIDADES (@Entity) — una por tabla del Modelo Relacional del equipo, por ejemplo:
//      contenidos, recursos, actividades, alternativas, asignaciones, estudiantes,
//      seguimientos/respuestas e informes (a partir de las clases de model/).
//    Las listas (alternativas, fortalezas) conviene guardarlas en su propia tabla o con
//    un @TypeConverter. Los enums se pueden guardar como String (enum.name).
//
// 2) DAOs (@Dao) — consultas que necesita cada pantalla, por ejemplo:
//      - contenidos por estado y asignatura (UTP)
//      - contenidos APROBADOS (docente)
//      - contenidos asignados al curso del estudiante
//      - desafíos de un contenido ordenados por dificultad / evaluación final
//      - respuestas de un estudiante en un contenido; intentos usados
//      - informe de un estudiante
//
// 3) BASE DE DATOS (@Database) — AppDatabase con las entidades, version = 1,
//    exportSchema = false, e instancia única.
//
// 4) DATASTORE (opcional) — recordar el último usuario o curso que ingresó.
//
// 5) VIEWMODEL — reemplazar MutableStateFlow(MockData...) por los Flow del DAO con
//    stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()).
//    Para tener Context, el ViewModel puede extender AndroidViewModel(application).
// =====================================================================================
