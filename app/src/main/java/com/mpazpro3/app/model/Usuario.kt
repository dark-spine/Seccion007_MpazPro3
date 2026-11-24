package com.mpazpro3.app.model

/**
 * Usuario que inicia sesión. Según su perfil, AppNavigation lo lleva a su pantalla principal.
 * Solo datos ficticios (ver DatosIniciales.usuarios).
 *
 * La clave nunca se guarda en texto plano: se almacena su resumen SHA-256 con una sal fija de
 * demostración (ver ReglasAcceso.hashClave).
 */
data class Usuario(
    val id: String,
    val nombre: String,
    val usuario: String,
    val claveHash: String,
    val perfil: Perfil,
    val curso: String? = null
)
