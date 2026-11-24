# MPAZ PRO 3° — Proyecto de la Experiencia 3 (integración con servidor)

Aplicación Android (Kotlin, Jetpack Compose con Material 3, MVVM con `ViewModel` + `StateFlow` y Room) para el
apoyo pedagógico de 3° básico en cuatro asignaturas. El proyecto trae **todos los requerimientos funcionales del
caso (RF-00 a RF-14) funcionando de forma local con Room** y el andamiaje de la Experiencia 3: una capa remota con
Retrofit que debes completar para que la app trabaje contra un servidor (API REST), más pruebas unitarias, una
prueba de interfaz y la generación de un APK firmado. Un interruptor **Modo local / Modo servidor** (sección 5) permite
alternar entre las dos formas de trabajar sin recompilar.

El servidor ya está hecho: se entrega aparte, en el archivo `..._Backend.zip` (carpeta `backend/`, Node + Express, sin
base de datos externa). Tu trabajo es la **app**.

## 1. Descripción del caso

Un colegio necesita que la Jefatura UTP autorice los contenidos, que el docente los planifique para su curso y
que los estudiantes los trabajen con material, desafíos y una evaluación final. Hay tres perfiles:

| Perfil | Qué hace |
|---|---|
| Jefatura UTP | Revisa contenidos curriculares, los filtra por asignatura y estado, y los aprueba o rechaza (con motivo). |
| Docente | Ve solo los contenidos aprobados, los asigna a su curso, define el máximo de intentos de la evaluación (1 a 10), revisa resultados, descarga un CSV y genera/valida el informe pedagógico de cada estudiante. |
| Estudiante | Ve lo que se le asignó, estudia el material (imagen, texto y audio opcional), resuelve desafíos de dificultad progresiva, rinde la evaluación final y revisa sus propias respuestas. |

Reglas de negocio principales:

- **RN-01**: cada perfil accede solo a lo suyo; el estudiante nunca ve el informe. Se controla en la navegación y de nuevo en el `ViewModel`.
- **RN-02**: solo los contenidos aprobados por UTP se pueden asignar y mostrar. Si hay uno no autorizado en un lote, no se asigna ninguno.
- **RN-03**: la evaluación final respeta estrictamente el máximo de intentos del docente.
- **RN-04**: máximo una unidad aprobada por asignatura.
- **RN-05 / RN-06**: el informe nace en borrador, el docente lo valida y, una vez validado, no se regenera. El texto solo describe el desempeño: no diagnostica ni aplica consecuencias académicas.

Todos los datos de ejemplo son ficticios.

## 2. Perfiles y usuarios de prueba

| Perfil | Usuario | Clave |
|---|---|---|
| Jefatura UTP | `utp@mpazpro3.cl` | `utp123` |
| Docente (3° básico A) | `docente.3basicoA@mpazpro3.cl` | `docente123` |
| Estudiante | `est001`, `est002`, `est003`, `est004` | `1234` |

El usuario no distingue mayúsculas; la clave sí. Las claves **no** se guardan en texto plano: se almacena su SHA-256
con una sal fija de demostración (`ReglasAcceso.hashClave`). Un sistema real debe usar una sal por usuario y un
algoritmo de derivación de claves (bcrypt, Argon2).

Datos de ejemplo al abrir por primera vez: Matemática aprobada y asignada al curso; Lenguaje y Ciencias pendientes;
Historia rechazada con observación. `est002` ya terminó Matemática, `est003` va a la mitad y `est001` / `est004` no
han empezado.

## 3. Requerimientos funcionales

| RF | Descripción | Estado local | Dónde |
|---|---|---|---|
| RF-00 | Inicio de sesión por perfil con validación por campo | Implementado | `ui/screen/LoginScreen.kt`, `domain/ReglasAcceso.kt`, `viewmodel/MpazProViewModel.login` |
| RF-01 | UTP aprueba o rechaza contenidos (motivo obligatorio al rechazar, confirmación, RN-04) | Implementado | `ui/screen/utp/DetalleContenidoUtpScreen.kt`, `domain/ReglasContenido.kt` |
| RF-02 | Listado y filtros por asignatura y estado, pendientes primero | Implementado | `ui/screen/utp/ContenidosUtpScreen.kt`, `ReglasContenido.filtrar` |
| RF-03 | El docente ve solo contenidos autorizados | Implementado | `ui/screen/docente/PlanificacionDocenteScreen.kt`, `ReglasContenido.disponiblesParaAsignar` |
| RF-04 | Asignar contenidos al curso (todo o nada) | Implementado | `PlanificacionDocenteScreen`, `ReglasPlanificacion`, `MpazProRepositorio.asignarAlCurso` |
| RF-05 | Máximo de intentos de la evaluación (1 a 10) | Implementado | `PlanificacionDocenteScreen`, `ReglasPlanificacion.errorMaxIntentos` |
| RF-06 | Resultados del curso, avance por estudiante y descarga CSV | Implementado | `ui/screen/docente/ResultadosCursoScreen.kt`, `domain/ExportadorResultados.kt`, `ReglasProgreso` |
| RF-07 | El estudiante ve sus actividades y el material (imagen, texto, audio) | Implementado | `ui/screen/estudiante/InicioEstudianteScreen.kt`, `MaterialScreen.kt`, `ui/components/ReproductorAudio.kt` |
| RF-08 | Desafíos de dificultad progresiva con retroalimentación inmediata | Implementado | `ui/screen/estudiante/DesafiosScreen.kt`, `domain/ReglasDesafio.kt` |
| RF-09 | Evaluación final con intentos limitados | Implementado | `ui/screen/estudiante/EvaluacionFinalScreen.kt`, `domain/ReglasEvaluacion.kt` |
| RF-10 | El estudiante revisa sus respuestas (correcta / incorrecta) | Implementado | `ui/screen/estudiante/RevisionRespuestasScreen.kt`, `ReglasProgreso.respuestasVigentes` |
| RF-11 | Seguimiento de avance (material, desafíos, evaluación) | Implementado | `ReglasProgreso`, `MpazProRepositorio.confirmarMaterialRevisado` |
| RF-12 | Generar el informe pedagógico en borrador | Implementado | `ui/screen/docente/InformeDocenteScreen.kt`, `domain/GeneradorInforme.kt` |
| RF-13 | Validar el informe (un informe validado no se regenera) | Implementado | `InformeDocenteScreen`, `MpazProRepositorio.validarInforme` |
| RF-14 | El informe describe, no diagnostica, y lleva el aviso correspondiente | Implementado | `GeneradorInforme`, `InformeDocenteScreen` |

Fórmula del avance de un contenido:

```
avance = (material revisado + desafíos distintos respondidos + evaluación rendida) / (1 + n.º de desafíos + 1)
```

Repetir un desafío no suma de nuevo. El avance general es el promedio de los contenidos asignados al curso.
Las respuestas "vigentes" son la última de cada desafío y las del último intento de la evaluación.

### Recursos nativos del dispositivo

- **Selector de archivos del sistema (SAF)**: guardar el CSV de resultados donde el docente elija (`CreateDocument`), más la hoja de compartir del sistema.
- **Vibración**: refuerzo al responder un desafío y al confirmar acciones (permiso `VIBRATE`, que no pregunta al usuario).
- **Audio** (`MediaPlayer`): reproductor del material de apoyo (no se reproduce solo).

No se usan cámara, GPS, micrófono ni biometría.

### Formato del CSV

Separador `;`, saltos de línea CRLF, una fila por estudiante y contenido asignado. Columnas: `Curso;Estudiante ID;
Estudiante;Asignatura;Contenido;Avance (%);Correctas;Incorrectas;Intentos usados;Máximo de intentos;Estado`.
Los campos que empiezan con `=`, `+`, `-` o `@` se neutralizan para evitar fórmulas en planillas.

## 4. Arquitectura

MVVM con un único `ViewModel`, una única base de datos y una única navegación.

```
com.mpazpro3.app
├── model/        Datos puros del dominio (Contenido, Actividad, Asignacion, SeguimientoAvance, InformeDocente, Resultado…)
├── domain/       Reglas de negocio PURAS y probadas en JVM (ReglasAcceso, ReglasContenido, ReglasPlanificacion,
│                 ReglasDesafio, ReglasEvaluacion, ReglasProgreso, GeneradorInforme, ExportadorResultados)
├── data/         DatosIniciales (semilla), MpazProRepositorio (lecturas observables de Room), RepositorioLocal (modo local:
│   │             solo Room), SelectorFuente + PreferenciaModo (eligen el modo en cada operación), AppContenedor (arma las
│   │             piezas), CacheLocal (contrato de la copia local), RepositorioSincronizado (modo servidor: API + Room)
│   │             <-- TODO(E3-6..E3-9)
│   ├── local/    Room: entidades, DAOs, AppDatabase (dos archivos: base local y copia del servidor), mapeos entidad <-> modelo,
│   │             AlmacenRoom (base del modo local) y CacheRoom (copia del servidor)  <-- TODO(E3-6, E3-8)
│   └── remote/   Retrofit: Dtos y ApiService (completos: son el contrato), ApiClient, ErroresApi, Mapeadores,
│                 FuenteRemota, UrlServidor, ConfiguracionServidor, ProbarServidor  <-- TODO(E3-1 a E3-5, E3-10)
├── viewmodel/    MpazProViewModel: estado (StateFlow), permisos por perfil y acciones; usa una sola fuente (SelectorFuente)
├── navigation/   AppNavigation (NavHost, rutas, control de acceso por perfil y aviso de conexión)
└── ui/
    ├── theme/        Tema Material 3 (paleta del colegio)
    ├── components/   BarraSuperior, SelectorDesplegable, MensajeResultado, ReproductorAudio, AvisoConexion, ServidorDialog…
    └── screen/       login, utp, docente, estudiante
```

Decisiones de diseño:

- Las reglas viven en `domain/` sin dependencias de Android, por eso se prueban con JUnit en la JVM.
- Las acciones que modifican datos son `suspend`, se ejecutan fuera del hilo principal y devuelven un `Resultado(exito, mensaje)` que las pantallas muestran con `MensajeResultado`.
- Las pantallas dibujan lo que hay en Room. Con la API, cada sincronización actualiza Room y la pantalla se refresca sola.
- Las reglas de negocio (RN-02, RN-03, RN-04…) las valida el servidor; la app solo hace las comprobaciones baratas (motivo, rango de intentos) para responder sin esperar a la red. El servidor tiene la última palabra.
- La base de datos está en la versión 1 y no usa migración destructiva: si cambias el esquema, sube la versión y escribe una `Migration` (en desarrollo basta desinstalar la app).

## 5. Dos modos de ejecución: interruptor "Modo local / Modo servidor"

La app trabaja de dos maneras, y se elige con el interruptor **Modo local (sin servidor)** del diálogo **Servidor** (botón al final de
la pantalla de ingreso). El cambio es inmediato: la preferencia se guarda en el dispositivo y se lee en **cada operación**, no solo al abrir la app.
Una franja fija en la parte superior de todas las pantallas dice "Modo local" o "Modo servidor".

| Modo | Qué hace la app |
|---|---|
| **Modo local** (valor inicial con `USAR_API=false`) | Funciona completa y sin servidor, solo con Room: ingreso con el hash local, y todas las acciones con escritura (aprobar/rechazar, asignar, material, desafíos, evaluación con intentos, CSV, informes) con las mismas funciones puras de `domain/` y los mismos mensajes del servidor. No se ejecuta nada de la capa remota. Es el respaldo para demostrar sin servidor. |
| **Modo servidor** (valor inicial con `USAR_API=true`) | Login contra `/auth/login`, lectura desde el servidor (Room queda como copia) y cada acción que modifica datos se envía primero al servidor. **Depende de lo que completes en los `TODO(E3-n)`.** |

**`USAR_API` solo define el valor INICIAL del interruptor** (`gradle.properties` → `BuildConfig.USAR_API`; también `-PUSAR_API=true` al compilar).
En cuanto eliges un modo en el diálogo Servidor, manda lo guardado en el dispositivo y `USAR_API` ya no influye (para volver al valor
inicial hay que borrar los datos de la app o reinstalarla).

- Si **Probar conexión** falla, el diálogo ofrece el botón **Usar modo local**.
- **Datos independientes**: cada modo tiene su propia base Room (`mpazpro3_local.db` para el modo local y `mpazpro3.db` para la copia del servidor).
  **No hay sincronización**: lo que se crea en modo local no se envía al servidor al volver al modo servidor, ni lo del servidor se mezcla con lo local.
- **Restablecer datos locales** (en el diálogo, con el modo local activo y con confirmación) borra lo creado en modo local y vuelve a cargar los datos de ejemplo.
- Cambiar de modo con una sesión abierta cierra la sesión.
- Si actualizas desde una versión anterior del proyecto, desinstala la app: la base local ahora es otro archivo.

Mientras un `TODO` esté sin completar, la función correspondiente del modo servidor lanza `NotImplementedError` con el código del TODO
("An operation is not implemented: E3-3b: ..."); la app lo muestra como aviso en vez de cerrarse. Por eso conviene pasar a
modo servidor recién cuando hayas terminado las partes 2 y 3 de la guía.

> En la defensa debe mostrarse el **modo servidor** (integración con la API); el modo local es solo respaldo.

## 6. Qué debes completar: los TODO de la Experiencia 3

Cada `TODO(E3-n)` está en el código, dice **qué hacer, en qué archivo y con qué criterio de aceptación**. Búscalos con
*Edit > Find in Files* (`Ctrl+Shift+F`) escribiendo `TODO(E3`. Ya están hechos, y no debes cambiar su forma:
`Dtos.kt` y `ApiService.kt` (son el contrato con el servidor, ver `backend/docs/API.md`), `ApiResult`, `SesionApi`,
`Conectividad`, la cola de pendientes, `CacheLocal`, `AppContenedor`, el modo local, las pantallas y el `ViewModel`.

| Código | Archivo | Qué se hace |
|---|---|---|
| E3-1 | `data/remote/UrlServidor.kt` | Validar y normalizar la URL del servidor (esquema, host, puerto, http solo local). |
| E3-2 | `data/remote/ApiClient.kt` | Interceptor del token, `OkHttpClient` con timeouts y `Retrofit` con Gson. |
| E3-3 | `data/remote/ErroresApi.kt` | Traducir códigos HTTP, cuerpo `{error, campo}`, timeout, sin red y JSON mal formado a mensajes en español. |
| E3-4 | `data/remote/Mapeadores.kt` | Mapeadores DTO ↔ modelo (hay dos de ejemplo ya resueltos). |
| E3-5 | `data/remote/FuenteRemota.kt` | Fuente de datos remota: ejecutar llamadas con red y sesión, y descargar todo lo del perfil. |
| E3-6 | `data/local/CacheRoom.kt`, `data/RepositorioSincronizado.kt` | Copia en Room (reemplazo atómico, UPDATE sin REPLACE) y `sincronizar`: API → Room → pantalla. |
| E3-7 | `data/RepositorioSincronizado.kt` | Escrituras con la API primero: aprobar, rechazar, asignar, material, desafío, evaluación, CSV e informes. |
| E3-8 | `CacheRoom.kt`, `RepositorioSincronizado.kt` | Comportamiento sin red: ingreso con datos guardados y (avanzado) cola de pendientes. |
| E3-9 | `data/RepositorioSincronizado.kt` | Login contra `POST /auth/login` y cierre de sesión. |
| E3-10 | `ConfiguracionServidor.kt`, `ProbarServidor.kt` | Guardar/restablecer la URL y "Probar conexión" contra `GET /health`. |
| E3-T1 a T6 | `src/test/...`, `src/androidTest/...` | Pruebas: API con MockWebServer, errores, mapeadores, URL, repositorio y una prueba de interfaz. |

Orden recomendado: E3-1 → E3-2 → E3-3 → E3-4 → E3-5 → E3-9 → E3-6 → E3-7 → E3-8 → E3-10, probando cada parte con su prueba.

## 7. Cómo ejecutar

Requisitos: Android Studio (Koala o posterior), JDK 17 y Android SDK 35.

1. Abrir la carpeta del proyecto en Android Studio y esperar la sincronización de Gradle.
2. Elegir un emulador o dispositivo (Android 7.0 / API 24 o superior).
3. Ejecutar la configuración `app`, o por consola: `./gradlew assembleDebug` (en Windows: `gradlew.bat assembleDebug`).

**Modo local** (valor inicial con `USAR_API=false`): los datos de ejemplo se cargan solos la primera vez que se abre la aplicación y no
se necesita ningún servidor.

**Modo servidor** (valor inicial con `USAR_API=true`, o activándolo en el diálogo **Servidor**, interruptor apagado):

1. Levantar el servidor (ver su propio `README.md`): `cd backend && npm install && npm start` y comprobar `http://localhost:3000/health`.
2. Elegir la URL que corresponda:

| Dónde corre la app | URL en la app |
|---|---|
| Emulador de Android Studio | `http://10.0.2.2:3000/` (valor por defecto de `API_BASE_URL`) |
| Teléfono físico en la misma red Wi-Fi que el PC | `http://<IP del PC>:3000/` (por ejemplo `http://192.168.1.20:3000/`) |
| Teléfono fuera de la red del PC | URL https pública de VS Code Port Forwarding (puerto 3000 con visibilidad **Pública**) |

   La URL inicial sale de `API_BASE_URL` en `gradle.properties` (o `-PAPI_BASE_URL=...` al compilar). Para cambiarla sin
   recompilar, en el login toca **Servidor**, escribe la URL, **Probar conexión** y **Guardar**. Las direcciones
   `http://` solo se aceptan hacia el emulador, `localhost` y redes locales; cualquier servidor público debe usar `https://`.
3. Ingresar con los usuarios de la sección 2.

Solución de problemas: **"No se pudo conectar"** → confirma que el servidor corre y que la URL es la correcta; en el
emulador `localhost` es el propio emulador, el PC se ve como `10.0.2.2`; si un teléfono físico no llega, permite el
puerto 3000 de Node.js en el firewall y revisa que ambos estén en la misma red. **"Datos que la app no entiende"** con
Port Forwarding → el puerto está en visibilidad privada (responde una página de acceso en vez del JSON). **"Tu sesión
expiró"** → el servidor perdió la sesión (por ejemplo, tras `npm run reset`): cierra sesión y vuelve a ingresar.

## 8. Cómo correr las pruebas

Pruebas unitarias JVM (JUnit 4, sin emulador) en `app/src/test`:

```
./gradlew testDebugUnitTest
```

- **Ya funcionan**: reglas de negocio de `domain/` (acceso, contenidos, planificación, desafíos, evaluación, progreso, informe, CSV), coherencia de los datos de ejemplo, el contrato de los DTO (`ContratoDtoTest`) y la regla de hosts permitidos para `http` (`HostPermiteHttpTest`).
- **Modo local (ya funcionan, 44 casos activos)**: `SelectorFuenteTest` (el selector elige la fuente según la preferencia y la relee en cada operación, también al cambiar de modo en caliente), `RepositorioLocalTest` (ingreso con hash local y cada acción con escritura con sus reglas: casos correctos y errores de RN-02, RN-03, RN-04, informe validado y CSV) y `ReglasModoLocalTest`. Usan una base en memoria (`AlmacenLocalMemoria` en `data/FakesLocal.kt`); no los marques con `@Ignore`.
- **Esqueletos con `@Ignore("E3: completar")`** (cada archivo lista los casos que debe cubrir): `ApiServiceMockWebServerTest`, `ErroresApiTest`, `MapeadoresTest`, `UrlServidorTest`, `RepositorioSincronizadoTest`. Al completar cada uno, quita el `@Ignore`. Entre todos suman más de 15 casos, incluidos 400/401/403/404/409, sin red, timeout, cuerpo mal formado, mapeadores y caché. `data/Fakes.kt` trae las utilidades ya hechas (ApiService, conectividad y copia local falsos).
- **Prueba de interfaz** (`app/src/androidTest/.../LoginUiTest.kt`, 3 casos con `@Ignore`): se ejecuta en emulador o dispositivo con `./gradlew connectedDebugAndroidTest`.

El reporte queda en `app/build/reports/tests/testDebugUnitTest/index.html`. Las pruebas del servidor están en `backend/test`
(`cd backend && npm test`).

## 9. Cómo generar el APK firmado

El repositorio **no contiene llaves ni contraseñas**. La firma es opcional: sin datos de firma, `./gradlew assembleRelease`
genera un APK sin firmar (`app-release-unsigned.apk`). Los pasos completos (crear el `.jks`, firmas V1 y V2, instalar,
verificar y registrar la bitácora) están en la guía de la actividad, Parte 6. En resumen:

1. Crear una llave propia (una sola vez, exclusiva para esta entrega):

```
keytool -genkeypair -v -keystore mi-llave.jks -alias mi-alias -keyalg RSA -keysize 2048 -validity 10000
```

2. Copiar `keystore.properties.example` como `keystore.properties` (queda fuera de Git) y completar los cuatro datos
   `MPAZPRO_STORE_FILE`, `MPAZPRO_STORE_PASSWORD`, `MPAZPRO_KEY_ALIAS` y `MPAZPRO_KEY_PASSWORD`. También se leen de
   `~/.gradle/gradle.properties` o de variables de entorno con esos mismos nombres.
3. Generar el APK: `./gradlew assembleRelease`. Queda en `app/build/outputs/apk/release/app-release.apk`, firmado con
   esquemas V1 y V2 (están activados en `app/build.gradle.kts`).

`.gitignore` excluye `local.properties`, `build/`, `.gradle/`, `.idea/` y `.kotlin/`. Para la evaluación, el `.jks` (llave exclusiva de esta entrega), el APK firmado y su configuración van en el repositorio; no reutilices esa llave en otra app.
Versión actual: `versionName 1.0.0`, `versionCode 1` (sube `versionCode` con cada entrega). El build release tiene la minificación desactivada.

## 10. Material didáctico y orígenes (RNF-05)

| Recurso | Origen |
|---|---|
| Ilustraciones vectoriales (`res/drawable`) | Propias del proyecto (XML vectorial). |
| Logo y paleta | Identidad del colegio entregada con la plantilla del caso. |
| Textos de contenidos, desafíos y evaluaciones | Redactados para el proyecto; sujetos a revisión de la Jefatura UTP. |
| Audio de apoyo `res/raw/centenas_explicacion.wav` | **Señal de ejemplo generada por código** (tonos que cuentan unidades, decenas y centenas), sin voz ni material de terceros. Es un recurso de reemplazo: para usar tu propia explicación, grábala (30 a 60 segundos, tu propia voz o con permiso de quien hable) y reemplaza el archivo conservando el nombre base `centenas_explicacion` (`.wav`, `.mp3`, `.ogg` o `.m4a`). En `res/raw` debe quedar un solo archivo con ese nombre; la referencia está en `ui/components/RecursosMultimedia.kt` y no hay que cambiarla. |

## 11. Tarea pendiente: validar los códigos OA

Los Objetivos de Aprendizaje figuran como `OA (por confirmar)` en los datos de ejemplo. Parte de tu trabajo es buscar el
código oficial de cada uno de los cuatro contenidos en el sitio del Currículum Nacional del Ministerio de Educación
(3° básico: Lenguaje y Comunicación, Matemática, Ciencias Naturales e Historia, Geografía y Ciencias Sociales), comprobar
que el texto del objetivo coincide y anotarlo en la bitácora. Luego cámbialo en `data/DatosIniciales.kt`
(constante `CODIGO_POR_CONFIRMAR` y los cuatro `codigoOA`) **y** en `backend/data/seed.json` (campo `codigoOA`), ejecuta
`npm run reset` en el servidor y desinstala la app del dispositivo para que Room tome los datos nuevos.

## 12. Qué sigue

- **Seguridad real**: el servidor es de demostración (sal fija, tokens sin vencimiento, sin límite de intentos). Para producción: HTTPS obligatorio, sal por usuario con bcrypt/Argon2, vencimiento de sesión y limitación de peticiones.
- **Despliegue**: el servidor puede publicarse en un servicio gratuito (Render, Railway o Koyeb) con `npm start`.

## Avance 3 — adaptación del equipo (Toro, Gajardo, Monardes)

Base del docente integrada sobre el historial existente de la entrega 2. Los cambios se entregan en el árbol de trabajo de master, pendientes de commits reales y revisión cruzada. Modo local recomendado por el correo del docente; API REST pendiente. No se afirma que la API externa esté eximida: consultar al docente.

Cambios: formato de usuario en ReglasAcceso, animación lateral del formulario al fallar, acceso directo a desafíos tras revisar material, pruebas JVM de acceso y ViewModel, configuración de cobertura de RepositorioLocal. La base docente ya aporta Room, estado vacío y vibración. No se incluyen claves ni APK sin verificar.

En Windows, desde la carpeta que contiene settings.gradle.kts:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat coberturaLocal
.\gradlew.bat assembleRelease
```

Informe de tests: `app/build/reports/tests/testDebugUnitTest/index.html`. Cobertura: `app/build/reports/jacoco/coberturaLocal/html/index.html` (verificar que exista y que alcance 80%; no se ha medido). Las pruebas remotas marcadas Ignore por el docente permanecen pendientes. Configurar firma con `keystore.properties.example`; generar llave académica en Android Studio, conservarla fuera del repositorio público y acordar entrega privada con el docente. Añadir capturas reales de firma y resultados al README antes de entregar.

Verificación pendiente: compilación, pruebas, cobertura, CRUD completo (incluida eliminación según alcance acordado), APK firmado, revisión de código sin uso y aportes individuales. El entorno de preparación no pudo descargar Gradle 8.7 (UnknownHostException).
