package com.mpazpro3.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mpazpro3.app.viewmodel.MpazProViewModel

/**
 * Franja fija en la parte superior de TODAS las pantallas que dice con qué se está trabajando:
 * "Modo local" (solo la base del dispositivo, sin servidor) o "Modo servidor" (API). Sirve para que, en la
 * defensa, se note qué se está demostrando.
 */
@Composable
fun FranjaModo(viewModel: MpazProViewModel, modifier: Modifier = Modifier) {
    val local by viewModel.modoLocal.collectAsState()
    val texto = if (local) "Modo local · sin servidor · datos solo en este dispositivo" else "Modo servidor · datos del servidor"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = if (local) "Modo local activo" else "Modo servidor activo" },
        color = if (local) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
        contentColor = if (local) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (local) Icons.Filled.PhoneAndroid else Icons.Filled.Cloud,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(texto, style = MaterialTheme.typography.labelMedium)
        }
    }
}
