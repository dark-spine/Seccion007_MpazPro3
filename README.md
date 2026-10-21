# MPAZ PRO 3° — Sección 007 (San Bernardo)

Aplicación móvil Android para el caso de Vinculación con el Medio **"MPAZ PRO 3°: Apoyo
didáctico y seguimiento del aprendizaje"**, de la Escuela Escritora Marcela Paz.

Asignatura: DSY1105 · Desarrollo de Aplicaciones Móviles
Evaluación Parcial 2 · Sección 007

---

## Integrantes

| Nombre | Aporte en esta entrega |
|---|---|
| Pablo Toro | Configuración del proyecto, capa de persistencia con Room (entidades, DAOs, base de datos y repositorio), RF-00 (login validado) y RF-07 (contenidos del curso del estudiante) |
| Jaime Gajardo | RF-11 (seguimiento de avance persistente), reproductor de audio con MediaPlayer y animaciones de interfaz (G7) |
| Edison Monardes | Ilustración de bloques de base diez, resolución del flujo hacia desafíos y eliminación del modo desarrollo (RNF-08) |

Repositorio: https://github.com/dark-spine/Seccion007_MpazPro3

> El repositorio es privado. El docente está agregado como colaborador.

> **Nota sobre el historial de commits:** el equipo trabajó presencialmente en un mismo
> equipo durante las sesiones de taller, por lo que los commits figuran bajo una sola cuenta
> de GitHub. El detalle de qué trabajó cada integrante es el de la tabla anterior y puede
> verificarse con el equipo durante la defensa.

---

## Alcance de esta entrega

El trabajo de la sección se repartió por requerimiento entre los equipos. A este equipo le
correspondió el **RF-07 · El estudiante consulta el material asignado**.

Para que ese requerimiento se pueda demostrar de punta a punta, se implementó además:

- **RF-00** (inicio de sesión), porque sin él no hay forma de entrar al perfil Estudiante.
- **RF-11** (conservar el progreso), en la parte que depende del material revisado.
- La **capa de persistencia completa** con Room, que es transversal a toda la aplicación.

Los requerimientos de los demás módulos (RF-01 a RF-06, RF-08 a RF-10, RF-12 a RF-14)
quedan como los entrega el proyecto base y están a cargo de otros equipos de la sección.
Sus datos se simulan con la carga inicial ficticia, tal como se detalla en *Supuestos*.

---

## Cómo ejecutar la app

1. Descomprimir el proyecto y abrirlo en **Android Studio** (`File` → `Open`), seleccionando
   la carpeta que contiene `settings.gradle.kts`.
2. Si pregunta por el JDK de Gradle, elegir **JVM 21**. Gradle 8.7 no es compatible con
   versiones superiores.
3. Esperar la sincronización de Gradle (descarga las dependencias la primera vez).
4. Ejecutar con el botón ▶ sobre un emulador o dispositivo con **Android 7.0 (API 24)** o superior.

No se requiere conexión a servicios externos ni backend: toda la información es local y ficticia.

### Versiones

| Componente | Versión |
|---|---|
| Kotlin | 2.0.0 |
| Android Gradle Plugin | 8.5.2 |
| Gradle | 8.7 |
| compileSdk / targetSdk | 35 |
| minSdk | 24 |
| Room | 2.6.1 |
| KSP | 2.0.0-1.0.24 |

---

## Usuarios de prueba

Todos los datos son ficticios (RNF-04). No corresponden a ninguna persona real.

| Perfil | Usuario | Clave |
|---|---|---|
| Jefatura UTP | `utp@mpazpro3.cl` | `utp123` |
| Docente 3° básico A | `docente.3basicoA@mpazpro3.cl` | `docente123` |
| Estudiante | `est001` | `1234` |

Para ver el RF-07 funcionando, ingresar como **estudiante**: debe aparecer únicamente
**Matemática**, que es el único contenido asignado a su curso y aprobado por UTP.

---

## Requerimientos implementados

### RF-00 · Inicio de sesión por perfil

`MpazProViewModel.login()` valida las credenciales contra la tabla `usuarios` de Room.

- Campos obligatorios, con mensaje propio para cada uno.
- El nombre de usuario ignora mayúsculas y espacios sobrantes; la clave no.
- Ante credenciales incorrectas el mensaje es genérico, sin revelar cuál de los dos falló.

### RF-07 · El estudiante consulta el material asignado

`MpazProViewModel.contenidosDelEstudiante()` devuelve solo los contenidos asignados al curso
del estudiante que además siguen **APROBADOS** por UTP.

El cruce lo resuelve la consulta `ContenidoDao.observarAsignadosAlCurso()`, con un `INNER JOIN`
entre `contenidos` y `asignaciones`. Con eso la regla **RN-02** queda aplicada en la base de
datos y no solo en la interfaz: un contenido no autorizado nunca llega al estudiante.

La pantalla de material muestra título, explicación, imagen, audio de apoyo, ejemplo e
instrucciones.

### RF-11 · Conservar el progreso (parcial)

`confirmarMaterialRevisado()` guarda en Room que el estudiante terminó de revisar el material.
`progreso()` calcula el avance mostrado en la barra de "Mis actividades".

**Fórmula de avance** acordada por el equipo: el recorrido tiene tres etapas de igual peso.

1. Revisar el material → 1/3
2. Responder los desafíos → 1/3 *(RF-08, otro equipo)*
3. Rendir la evaluación final → 1/3 *(RF-09, otro equipo)*

Como este equipo implementa el RF-07, el avance llega hasta 1/3.

---

## Arquitectura

Patrón **MVVM**, con las capas separadas:

```
Pantalla (@Composable)
    ↓  llama funciones, nunca contiene lógica de negocio
MpazProViewModel          viewmodel/MpazProViewModel.kt
    ↓  StateFlow + viewModelScope
MpazProRepositorio        data/MpazProRepositorio.kt
    ↓  decide de dónde vienen los datos
DAO                       data/local/Daos.kt
    ↓  consultas generadas por Room en tiempo de compilación
SQLite                    mpazpro3.db
```

Un único ViewModel compartido, creado en `AppNavigation`. Al extender `AndroidViewModel`
obtiene el `Context` que Room necesita para abrir la base.

### Persistencia local con Room

Tablas implementadas:

| Tabla | Contenido | Observaciones |
|---|---|---|
| `usuarios` | credenciales y perfil | |
| `contenidos` | unidad, OA, eje, estado de aprobación | enums guardados como texto |
| `recursos` | explicación, ejemplo, instrucciones, audio | FK → `contenidos` |
| `asignaciones` | contenido asignado a un curso | FK → `contenidos`, único (curso, contenidoId) |
| `seguimientos` | avance del estudiante | clave primaria compuesta |

Los enums (`Perfil`, `Asignatura`, `EstadoAprobacion`, `EstadoAvance`) se guardan con
`Convertidores.kt`, que almacena `enum.name` y no la etiqueta legible, para que un cambio de
texto en la interfaz no rompa los datos.

Los datos ficticios se cargan la primera vez con `sembrarSiEstaVacia()`. En ejecuciones
siguientes no se vuelven a insertar: el progreso del estudiante sobrevive al cierre de la app
(**RNF-06**).

---

## Recurso nativo utilizado

**`android.media.MediaPlayer`** — reproductor del audio de apoyo del material de estudio.
Implementado en `ui/components/ReproductorAudio.kt`.

### Por qué es pertinente al caso

El caso indica que el estudiante debe poder consultar el material asignado incluyendo
**audios de apoyo**, y el **RNF-02** exige que esos audios complementen la lectura autónoma
en lugar de reemplazarla. El reproductor responde directamente a ese requerimiento: los
estudiantes de tercero básico con distinto ritmo lector pueden escuchar la explicación
mientras el texto permanece visible en pantalla.

Además, el **RNF-03** prohíbe cámara, GPS, micrófono y datos biométricos, por lo que el
abanico de recursos nativos disponibles es estrecho. `MediaPlayer` forma parte del sistema
operativo, **no requiere ningún permiso en el manifest** y no accede a hardware restringido.

### Cómo está integrado

- El audio se guarda en `res/raw/` y se referencia con `R.raw.*`.
- `DisposableEffect` crea el reproductor al entrar a la pantalla y llama a `release()` al salir,
  de modo que el audio no sigue sonando fuera del material ni queda ocupando memoria.
- `setOnCompletionListener` repone el botón y la barra al terminar la reproducción.
- No hay reproducción automática: el estudiante decide cuándo escuchar (RNF-02).
- El archivo de audio es **material propio** grabado por el equipo, lo que cumple el **RNF-05**
  sobre contenidos propios, públicos o autorizados.

---

## Supuestos vigentes

Mientras el CITT no responda las consultas pendientes, el proyecto trabaja con estos supuestos,
tomados de `docs/Requerimientos_Seccion007.pdf`:

| Código | Supuesto |
|---|---|
| SUP-03 | El estudiante ingresa con un usuario simple, sin autenticación robusta. |
| SUP-04 | La unidad de cada asignatura la elige el equipo; los OA llevan "(por confirmar)". |
| SUP-06 | Los contenidos se cargan como datos iniciales; no hay pantalla para crearlos. |
| SUP-08 | Los audios los produce el equipo con material propio. |

Supuestos adicionales de alcance de este equipo:

- El contenido **C2 (Matemática)** se carga ya **APROBADO**, simulando que UTP lo autorizó
  mediante el RF-01, que corresponde a otro equipo.
- La asignación **AS1** del curso "3° básico A" se carga como dato inicial, simulando el RF-04.

---

## Pendiente antes de la entrega

- [ ] Completar la fecha y hora exactas del plazo (la rúbrica la deja en blanco).
- [ ] Confirmar con el docente el formato del nombre del ZIP para tres integrantes.
- [ ] Armar el ZIP sin las carpetas `build/` ni `.gradle/`.
- [ ] Descomprimir el ZIP en otra carpeta, abrirlo en Android Studio y comprobar que
  sincroniza, compila y ejecuta antes de subirlo a AVA.
- [ ] Verificar la app en un segundo equipo distinto al del taller.

---

## Documentación del caso

En la carpeta `docs/`:

- `Requerimientos_Seccion007.pdf` — requerimientos funcionales, reglas de negocio, RNF y supuestos.
- `Consultas_Pendientes_Seccion007.pdf` — preguntas abiertas al CITT (Paz Morales).