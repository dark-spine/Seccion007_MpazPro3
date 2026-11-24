package com.mpazpro3.app.domain

import com.mpazpro3.app.model.Perfil
import java.security.MessageDigest

/**
 * Reglas de acceso por perfil (RF-00, RN-01, RNF-08) como funciones PURAS: no usan Android ni
 * Room, por eso se prueban con pruebas unitarias JVM (ver ReglasAccesoTest).
 */
object ReglasAcceso {

    const val MENSAJE_USUARIO_VACIO = "Ingresa tu usuario"
    const val MENSAJE_FORMATO_USUARIO = "Ingresa un usuario o correo válido"
    private val nombreUsuario = Regex("^[A-Za-z0-9._-]{3,30}$")
    private val correo = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")
    const val MENSAJE_CLAVE_VACIA = "Ingresa tu clave"
    const val MENSAJE_CREDENCIALES = "Usuario o clave incorrectos"
    const val MENSAJE_SIN_PERMISO = "No tienes permiso para realizar esta acción."

    /**
     * Sal FIJA de demostración. Sirve para no guardar claves en texto plano en el proyecto
     * académico; un sistema real usaría una sal distinta por usuario y un algoritmo de
     * derivación de claves (por ejemplo bcrypt o Argon2).
     */
    private const val SAL_DEMO = "mpazpro3-demo-2026:"

    /** Errores por campo del formulario de ingreso (null = el campo está bien). */
    data class ErroresLogin(
        val usuario: String? = null,
        val clave: String? = null,
        val general: String? = null
    ) {
        val hayErrores: Boolean get() = usuario != null || clave != null || general != null
    }

    /** Valida que los campos no estén vacíos; cada campo tiene su propio mensaje. */
    fun validarCampos(usuario: String, clave: String): ErroresLogin = ErroresLogin(
        usuario = when {
            usuario.isBlank() -> MENSAJE_USUARIO_VACIO
            !nombreUsuario.matches(usuario.trim()) && !correo.matches(usuario.trim()) -> MENSAJE_FORMATO_USUARIO
            else -> null
        },
        clave = if (clave.isEmpty()) MENSAJE_CLAVE_VACIA else null
    )

    /** El usuario se compara sin distinguir mayúsculas y sin espacios al inicio o al final. */
    fun normalizarUsuario(usuario: String): String = usuario.trim().lowercase()

    /** SHA-256 (en hexadecimal) de la sal de demostración más la clave. */
    fun hashClave(clave: String): String {
        val resumen = MessageDigest.getInstance("SHA-256")
            .digest((SAL_DEMO + clave).toByteArray(Charsets.UTF_8))
        return resumen.joinToString("") { "%02x".format(it) }
    }

    /** Acciones protegidas por perfil (matriz de permisos del caso). */
    enum class Accion {
        /** RF-01 y RF-02: revisar, aprobar, rechazar y filtrar contenidos. */
        REVISAR_CONTENIDOS,

        /** RF-03 a RF-05: ver contenidos autorizados, asignarlos y definir intentos. */
        PLANIFICAR_CURSO,

        /** RF-06: ver y descargar resultados del curso. */
        VER_RESULTADOS,

        /** RF-12 y RF-13: generar, ver y validar el informe pedagógico. */
        GESTIONAR_INFORMES,

        /** RF-07 a RF-11: consultar material, desafíos, evaluación y sus propias respuestas. */
        TRABAJAR_COMO_ESTUDIANTE
    }

    /**
     * Matriz de permisos. Las celdas pendientes de confirmar con la escuela ("?" para UTP en
     * ver contenidos aprobados, resultados e informe) se resuelven como NO permitido (SUP).
     * El estudiante no accede nunca al informe pedagógico (RN-01).
     */
    fun puede(perfil: Perfil?, accion: Accion): Boolean = when (accion) {
        Accion.REVISAR_CONTENIDOS -> perfil == Perfil.UTP
        Accion.PLANIFICAR_CURSO,
        Accion.VER_RESULTADOS,
        Accion.GESTIONAR_INFORMES -> perfil == Perfil.DOCENTE
        Accion.TRABAJAR_COMO_ESTUDIANTE -> perfil == Perfil.ESTUDIANTE
    }
}
