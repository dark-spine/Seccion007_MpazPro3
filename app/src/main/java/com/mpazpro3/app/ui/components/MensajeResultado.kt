package com.mpazpro3.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mpazpro3.app.model.Resultado

/**
 * Aviso animado con el resultado de una acción: verde con ícono de check si salió bien, rojo con
 * ícono de error si no. Aparece y desaparece con un fundido (el cambio queda claro sin ser
 * brusco) y avisa a los lectores de pantalla cuando cambia.
 */
@Composable
fun MensajeResultado(resultado: Resultado?, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = resultado,
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
        label = "mensajeResultado"
    ) { actual ->
        if (actual != null) {
            val colorBase = if (actual.exito) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = if (actual.exito) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (actual.exito) Icons.Filled.CheckCircle else Icons.Filled.Error,
                        contentDescription = if (actual.exito) "Correcto" else "Error",
                        tint = colorBase
                    )
                    Text(
                        actual.mensaje,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (actual.exito) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }
        }
    }
}
