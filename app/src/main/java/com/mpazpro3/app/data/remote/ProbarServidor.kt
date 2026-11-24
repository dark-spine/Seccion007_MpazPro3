package com.mpazpro3.app.data.remote

import com.mpazpro3.app.model.Resultado

/**
 * TODO(E3-10c) "Probar conexión" de la pantalla Servidor: consulta GET /health y explica el resultado en español.
 *
 * Archivo: este. Qué hacer: llamar a servicio.salud() dentro de llamarApi { ... } y devolver un Resultado:
 *  - Exito con estado "ok" (sin distinguir mayúsculas): Resultado.ok("Conexión exitosa con el servidor (versión X).")
 *    (la parte de la versión solo si el servidor la informa);
 *  - Exito con otro estado, o Error 404 (hay otro servidor en esa dirección): Resultado.error("Hay un servidor en esa
 *    dirección, pero no es el de la app.");
 *  - otro Error o SinConexion: Resultado.error(mensaje en español del resultado).
 * Aceptación: contra el backend encendido dice "Conexión exitosa"; con el servidor apagado muestra
 * MSG_SIN_CONEXION; con una URL que responde 404 dice que no es el servidor de la app.
 */
suspend fun probarServidor(servicio: ApiService): Resultado =
    TODO("E3-10c: consultar /health y explicar el resultado")
