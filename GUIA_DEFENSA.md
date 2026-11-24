# Guía de práctica — MPAZ PRO 3

No usar durante la defensa: la rúbrica prohíbe IA y recursos de apoyo. Practicar antes.

## Abrir y probar
Abrir la carpeta MpazPro3 que contiene settings.gradle.kts. Seleccionar app y HONOR 400. Modo local en login. Usuarios ficticios: est001 / 1234, docente.3basicoA@mpazpro3.cl / docente123, utp@mpazpro3.cl / utp123.

- Login: probar ab, a b, correo@, clave incorrecta y luego credenciales correctas. Ver errores y movimiento lateral.
- Estudiante: abrir Matemática, revisar material, confirmar y responder desafío; comprobar vibración. Volver a inicio y probar botón directo. Cerrar y abrir app y comprobar respuestas guardadas.
- Estado vacío: comprobar con un curso sin asignaciones (datos iniciales tienen Matemática asignada; no afirmar que est004 esté vacío).
- Docente: asignar contenido aprobado, cambiar intentos y revisar resultados. UTP: aprobar/rechazar con observación. Verificar persistencia. La eliminación individual no está expuesta en el flujo actual; acordar alcance con docente.

## Defensa de 15 minutos
1. Ejecutar y mostrar perfiles (2 min).
2. Explicar MVVM y Room (3 min): LoginScreen dibuja; MpazProViewModel valida y controla sesión; SelectorFuente elige RepositorioLocal; AlmacenRoom escribe en transacciones; DAO consulta tablas; StateFlow actualiza interfaz.
3. Practicar modificación (6 min): cambiar mensaje MENSAJE_FORMATO_USUARIO en ReglasAcceso, ejecutar test; cambiar rango de intentos en ReglasPlanificacion y adaptar pruebas; explicar impacto. Hacer ejercicios en copia local antes del plazo, no modificar repositorio tras cierre.
4. Unit test (2 min): abrir MpazProViewModelTest, ejecutar con clic derecho. MockK sustituye contenedor y fuente; dispatcher de prueba controla corrutinas; assertEquals verifica estado; coVerify verifica que entradas inválidas no consulten repositorio.
5. Firma y Git (2 min): Build > Generate Signed Bundle / APK > APK > Create new > release. Mostrar evidencia real y explicar aportes propios.

## Preguntas para practicar
- ¿Por qué Room y no una lista en memoria? Room conserva datos tras cerrar la app.
- ¿Qué diferencia hay entre ViewModel y Repository? Estado y coordinación frente a acceso a datos y operaciones de negocio.
- ¿Qué prueba evita consultas innecesarias? formatoInvalidoNoConsultaRepositorio.
- ¿Qué prueba verifica permisos? sinSesionNoPuedeAprobarContenido.
- ¿Por qué firmar? Identifica la clave de publicación y permite verificar integridad y continuidad de actualizaciones.
- ¿Qué falta de REST? TODO de capa remota; no afirmar integración real.

## Git
Cada integrante configura su propia identidad, toma una tarea real, crea rama, hace commit, push y PR; otro integrante revisa. Integrar a master antes del plazo. No inventar autorías ni cambiar el historial anterior. El ZIP conserva el historial local, pero los cambios nuevos todavía no están comprometidos. Coordinar una única copia de integración para evitar conflictos.
