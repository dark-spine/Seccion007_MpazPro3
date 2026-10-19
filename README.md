# MPAZ PRO 3° — Proyecto base (Sección 007, San Bernardo)

Proyecto Android base para el caso de Vinculación con el Medio **"MPAZ PRO 3°: Apoyo didáctico y seguimiento del aprendizaje"** de la Escuela Escritora Marcela Paz. Es el punto de partida común de todos los equipos de la sección: trae la estructura, las pantallas de cada módulo y perfil, la navegación y la persistencia configurada. **La lógica de negocio del caso no está implementada**: esa es la parte que construye cada equipo.

Documentación del caso (carpeta `docs/`):

- `Requerimientos_Seccion007.pdf`: requerimientos funcionales, reglas de negocio, RNF y supuestos.
- `Consultas_Pendientes_Seccion007.pdf`: preguntas abiertas al CITT (Paz Morales). No resolver con supuestos propios.

## Cómo abrir y ejecutar

1. Descomprimir el ZIP en una carpeta limpia.
2. Android Studio → **File > Open…** → seleccionar la carpeta que contiene `settings.gradle.kts`.
3. Esperar el Gradle Sync (la primera vez necesita internet).
4. Ejecutar ▶ **Run 'app'**.
5. En el login, usar **"Modo desarrollo"** para entrar con cualquiera de los tres perfiles (el login real es tarea del equipo).

Si Android Studio sugiere actualizar Gradle o el Android Gradle Plugin, **no aceptar**: las versiones están fijadas para que todos los equipos usen exactamente las mismas.

Usuarios de prueba (en `data/MockData.kt`, para cuando implementen el login):

| Perfil | Usuario | Clave |
|---|---|---|
| Jefatura UTP | utp@mpazpro3.cl | utp123 |
| Docente 3° básico A | docente.3basicoA@mpazpro3.cl | docente123 |
| Estudiante | est001 | 1234 |

La forma en que se autentica el estudiante está pendiente de confirmar con el CITT.

## Stack

Kotlin 2.0 · Jetpack Compose · Material 3 · MVVM (ViewModel + StateFlow) · Navigation Compose · Room + KSP y DataStore (ya configurados en Gradle). El caso no define el stack: se aplica el estándar de la asignatura mientras el CITT no indique otra cosa. El caso **no** permite usar cámara, GPS, micrófono ni datos biométricos.

## Flujo de pantallas

```
LOGIN ──(según perfil)──┬─> UTP: Contenidos ──> Revisar contenido (aprobar / rechazar)
                        ├─> DOCENTE: Panel ──> Planificación (objetivos, intentos, asignar)
                        │                 └─> Resultados del curso ──> Informe pedagógico
                        └─> ESTUDIANTE: Mis actividades ──> Material ──> Desafíos
                                        ──> Evaluación final ──> Mis respuestas
Cerrar sesión (cualquier perfil) ──> LOGIN
```

El botón atrás nativo de Android funciona solo (lo maneja el NavHost). La flecha "←" de la barra superior hace lo mismo. Al ingresar, el login sale de la pila; "Cerrar sesión" la limpia completa. Ninguna pantalla del estudiante navega al informe pedagógico (restricción del caso).

## Estructura del código (`app/src/main/java/com/mpazpro3/app/`)

| Carpeta | Contenido |
|---|---|
| `model/` | `Contenido`, `RecursoDidactico`, `Actividad`, `Asignacion`, `Estudiante`, `SeguimientoAvance`, `InformeDocente`, `Usuario`, `Perfil` y sus enums. Corresponden a las entidades del caso. |
| `data/` | `MockData.kt`: datos y textos pedagógicos ficticios de ejemplo. |
| `data/local/` | `PersistenciaPendiente.kt`: guía de qué entidades, DAOs y base de datos crear con Room. |
| `viewmodel/` | `MpazProViewModel`: un solo ViewModel compartido. Todas las funciones pendientes están marcadas con `TODO(equipo)`. |
| `navigation/` | `AppNavigation`: rutas, NavHost, ingreso por perfil y cierre de sesión. |
| `ui/components/` | `BarraSuperior` (flecha atrás y cerrar sesión) y `SelectorDesplegable` (lista desplegable reutilizable). |
| `ui/screen/` | `LoginScreen` y una carpeta por perfil: `utp/`, `docente/`, `estudiante/`. |
| `ui/theme/` y `res/` | Tema, colores, logo e ícono **provisionales**. |

## Trazabilidad: requerimiento → pantalla → función a implementar

| Módulo | Requerimiento | Pantalla (ya construida) | Función en el ViewModel (TODO del equipo) |
|---|---|---|---|
| Acceso | Inicio de sesión por perfil | `LoginScreen` | `login()` |
| Autorización (UTP) | Revisar y autorizar o rechazar un contenido | `ContenidosUtpScreen`, `DetalleContenidoUtpScreen` | `aprobarContenido()`, `rechazarContenido()` |
| Autorización (UTP) | Registrar el estado de aprobación | `DetalleContenidoUtpScreen` | `aprobarContenido()`, `rechazarContenido()` |
| Autorización (UTP) | Filtrar contenidos por asignatura y estado | `ContenidosUtpScreen` | `contenidosFiltrados()` |
| Planificación docente | Seleccionar objetivos disponibles (autorizados) | `PlanificacionDocenteScreen` | `contenidosDisponiblesParaAsignar()` |
| Planificación docente | Asignar contenidos y desafíos al curso | `PlanificacionDocenteScreen`, `PanelDocenteScreen` | `asignarAlCurso()`, `asignacionesDelCurso()` |
| Planificación docente | Definir el máximo de intentos de la evaluación final | `PlanificacionDocenteScreen` | `asignarAlCurso()` |
| Planificación docente | Revisar y descargar resultados | `ResultadosCursoScreen` | `progresoGeneral()`, `descargarResultados()` |
| Experiencia del estudiante | Consultar el material asignado | `InicioEstudianteScreen`, `MaterialScreen` | `contenidosDelEstudiante()`, reproducción de audio |
| Experiencia del estudiante | Desafíos de dificultad progresiva | `DesafiosScreen` | `desafiosDe()`, `responderDesafio()` |
| Experiencia del estudiante | Evaluación final dentro de los intentos permitidos | `EvaluacionFinalScreen` | `evaluacionFinalDe()`, `intentosRestantes()`, `enviarEvaluacion()` |
| Experiencia del estudiante | Revisar respuestas correctas e incorrectas | `RevisionRespuestasScreen` | `respuestasDe()` |
| Experiencia del estudiante | Conservar el progreso y confirmar la finalización | `MaterialScreen`, `InicioEstudianteScreen` | `confirmarMaterialRevisado()`, `progreso()` |
| Informe pedagógico | Generar informe (rendimiento, fortalezas, aspectos a reforzar) | `InformeDocenteScreen` | `generarInforme()` |
| Informe pedagógico | Validación docente antes de considerarlo definitivo | `InformeDocenteScreen` | `validarInforme()` |
| Persistencia | Guardar datos y progreso en el dispositivo | — | Room: ver `data/local/PersistenciaPendiente.kt` |

**Reglas de negocio que el equipo debe hacer cumplir:**

- El estudiante no puede modificar contenidos ni actividades, ni acceder al informe pedagógico.
- Un contenido no autorizado por UTP no puede ser asignado por el docente.
- El número de intentos de la evaluación final lo define el docente y debe respetarse estrictamente.
- El informe no emite diagnósticos ni aplica consecuencias académicas automáticas.

Para ver todo lo pendiente: **Edit > Find > Find in Files** (Ctrl+Shift+F / Cmd+Shift+F) y buscar `TODO(equipo)`.

## Qué pasa hoy al ejecutar la app (es intencional)

- **Ingresar:** el login siempre rechaza. Hay que entrar con "Modo desarrollo".
- **Listas:** se muestran todos los contenidos y actividades, sin filtrar (por estado, curso o tipo).
- **Acciones:** aprobar, rechazar, asignar, responder, enviar la evaluación, generar o validar un informe no cambian nada.
- **Avance, retroalimentación e intentos:** el avance aparece en 0 %, la retroalimentación de los desafíos y el conteo de intentos aparecen como pendientes, y la revisión de respuestas aparece vacía.

## Orden sugerido de trabajo

1. **Login:** validar campos vacíos y luego las credenciales por perfil.
2. **UTP:** aprobar y rechazar contenidos, y filtrar la lista.
3. **Docente:**
   - Mostrar solo los contenidos aprobados.
   - Asignarlos al curso con el máximo de intentos.
4. **Estudiante:**
   - Mostrar sus contenidos asignados.
   - Desafíos ordenados con retroalimentación.
   - Evaluación final con control de intentos.
   - Revisión de respuestas.
5. **Docente:**
   - Mostrar el avance de cada estudiante.
   - Generar y validar el informe.
   - Descargar los resultados.
6. **Persistencia:** reemplazar `MockData` por Room (Semana 9). El caso exige conservar el progreso del estudiante.
7. **Cierre:**
   - Quitar el "Modo desarrollo".
   - Revisar que no quede ningún `TODO(equipo)`.
   - Reemplazar el logo, los colores y los textos de ejemplo.

## Contenido pedagógico

Los textos de `MockData.kt` son **de ejemplo**. Cada equipo debe trabajar como máximo una unidad por asignatura (Lenguaje, Matemática, Ciencias Naturales, Historia), con los OA que se definan con el CITT. El material debe ser propio, público o expresamente autorizado: no se puede copiar material protegido. Los audios de apoyo complementan la lectura; no la reemplazan.

## Reglas del proyecto

- **Datos:** solo ficticios (usuarios, cursos, respuestas, resultados e informes).
- **GitHub:**
  - Un integrante sube esta base como commit inicial.
  - Después, una rama por funcionalidad (por ejemplo `feature/login`).
  - Debe haber commits de **ambos** integrantes.
- **Consultas sobre el caso:** van por el canal Estudiante → Docente → CITT → organización.

## Logo e ícono

Son **provisionales** (un libro abierto con estrella). Para usar el logo oficial que entregue la escuela:

- **Logo:** copiar el PNG a `res/drawable/logo_escuela.png` y borrar `logo_escuela.xml`.
- **Ícono de la app:** clic derecho en `res` > **New > Image Asset**.
