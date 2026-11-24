package com.mpazpro3.app.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Recurso nativo: vibración del dispositivo como refuerzo inmediato (RF-08, RF-01).
 *
 * Es pertinente al caso (retroalimentación para niños de 3° básico) y respeta sus restricciones:
 * no usa cámara, GPS, micrófono ni biometría. El permiso VIBRATE es "normal": Android lo concede
 * al instalar, sin preguntar al usuario.
 *
 * Android cambió la forma de pedir el vibrador con los años; por eso se revisa la versión
 * (la app funciona desde Android 7.0, API 24).
 */
private fun vibrar(context: Context, duracionMs: Long) {
    val vibrador: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Tabletas o emuladores sin motor de vibración: no hacer nada.
    if (vibrador == null || !vibrador.hasVibrator()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrador.vibrate(VibrationEffect.createOneShot(duracionMs, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrador.vibrate(duracionMs)
    }
}

/** Vibración al responder un desafío: corta si es correcta, más larga si es incorrecta. */
fun vibrarRespuesta(context: Context, correcta: Boolean) = vibrar(context, if (correcta) 80L else 300L)

/** Vibración breve al confirmar una acción (por ejemplo, aprobar un contenido). */
fun vibrarConfirmacion(context: Context) = vibrar(context, 100L)
