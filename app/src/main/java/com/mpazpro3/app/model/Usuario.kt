package com.mpazpro3.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Usuario que inicia sesión. Según su perfil, AppNavigation lo lleva a su pantalla principal.
 * Solo datos ficticios (ver MockData.usuarios).
 *
 * Pendiente con el CITT: cómo se autentica el ESTUDIANTE (ver Consultas_Pendientes_Seccion007).
 */
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val id: String,
    val nombre: String,
    val usuario: String,
    val clave: String,
    val perfil: Perfil,
    val curso: String? = null
)