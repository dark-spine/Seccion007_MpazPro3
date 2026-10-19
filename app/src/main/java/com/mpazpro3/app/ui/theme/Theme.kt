package com.mpazpro3.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Colores PROVISIONALES de la escuela (no se pudieron obtener los oficiales). TODO(equipo):
// reemplazar por los colores del manual de marca / insignia que entregue la escuela o el CITT.
// Si se cambian, cambiar también res/values/colors.xml (ícono de la app y logo).
private val AzulMarcelaPaz = Color(0xFF1565C0)
private val AmarilloEstrella = Color(0xFFFFC107)
private val VerdeAprobado = Color(0xFF2E7D32)

private val LightColors = lightColorScheme(
    primary = AzulMarcelaPaz,
    secondary = VerdeAprobado,
    tertiary = AmarilloEstrella
)

private val DarkColors = darkColorScheme(
    primary = AzulMarcelaPaz,
    secondary = VerdeAprobado,
    tertiary = AmarilloEstrella
)

@Composable
fun MpazPro3Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // false = siempre usar los colores de la escuela
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
