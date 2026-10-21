package com.mpazpro3.app.device

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Acceso al vibrador del dispositivo (segundo recurso nativo del proyecto).
 *
 * Por qué es pertinente al caso: los estudiantes de Tercero Básico tienen distintos ritmos
 * de lectura, y una confirmación táctil acompaña a la visual cuando terminan una actividad
 * o cuando el audio de apoyo llega a su fin. Es un refuerzo, nunca la única señal: toda
 * confirmación sigue mostrándose en pantalla (RNF-01).
 *
 * Por qué es seguro: requiere el permiso VIBRATE, que es de nivel "normal" y se concede al
 * instalar sin diálogo. No accede a cámara, GPS, micrófono ni datos biométricos, de modo
 * que respeta el RNF-03. No lee ni envía ningún dato del dispositivo.
 *
 * Compatibilidad: la app parte en API 24, así que hay dos bifurcaciones.
 *  - Desde API 31 el vibrador se obtiene a través de VibratorManager.
 *  - Desde API 26 existe VibrationEffect; antes solo se podía pasar una duración.
 *
 * Si el dispositivo no tiene vibrador (muchas tablets no lo traen), todas las llamadas
 * no hacen nada y la app sigue funcionando igual.
 */
class Vibrador(context: Context) {

    private val vibrador: Vibrator? = obtenerVibrador(context)

    /** true solo si el dispositivo tiene hardware de vibración. */
    val disponible: Boolean
        get() = vibrador?.hasVibrator() == true

    /** Pulso breve de confirmación (60 ms). */
    fun confirmacion() = vibrar(60)

    /** Pulso un poco más largo para avisar que algo terminó (120 ms). */
    fun aviso() = vibrar(120)

    private fun vibrar(milisegundos: Long) {
        val v = vibrador ?: return
        if (!v.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(
                VibrationEffect.createOneShot(milisegundos, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(milisegundos)
        }
    }

    private fun obtenerVibrador(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val gestor = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            gestor?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
}

/**
 * Crea el vibrador una sola vez por pantalla y lo reutiliza en las siguientes
 * recomposiciones, en lugar de pedirle el servicio al sistema en cada redibujado.
 */
@Composable
fun recordarVibrador(): Vibrador {
    val contexto = LocalContext.current
    return remember(contexto) { Vibrador(contexto) }
}