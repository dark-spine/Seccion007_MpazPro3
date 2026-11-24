package com.mpazpro3.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mpazpro3.app.data.EstadoSync
import com.mpazpro3.app.viewmodel.MpazProViewModel

/**
 * Aviso flotante sobre el estado de la conexión con el servidor, visible en todas las pantallas
 * mientras hay una sesión:
 *  - mientras descarga los datos muestra un indicador de carga;
 *  - si no hay conexión o el servidor falla, lo explica, indica cuántas acciones esperan para
 *    enviarse y ofrece "Reintentar";
 *  - muestra los avisos informativos (por ejemplo "se guardó en el dispositivo") hasta cerrarlos.
 * No tapa nada cuando todo está bien.
 */
@Composable
fun AvisoConexion(viewModel: MpazProViewModel, modifier: Modifier = Modifier) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val estado by viewModel.estadoSync.collectAsState()
    val pendientes by viewModel.pendientes.collectAsState()
    val aviso by viewModel.aviso.collectAsState()

    if (usuario == null) return

    val estadoActual = estado
    val cargando = estadoActual is EstadoSync.Cargando
    val sinConexion = estadoActual is EstadoSync.SinConexion
    val problema: String? = when (estadoActual) {
        is EstadoSync.SinConexion -> estadoActual.mensaje
        is EstadoSync.Error -> estadoActual.mensaje
        else -> null
    }
    val textoPendientes = if (pendientes > 0) " Acciones por enviar: $pendientes." else ""

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .animateContentSize()
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(visible = cargando) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 3.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("Actualizando datos…", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (problema != null) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 4.dp)) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (sinConexion) Icons.Filled.CloudOff else Icons.Filled.Error,
                            contentDescription = if (sinConexion) "Sin conexión" else "Error",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(problema + textoPendientes, style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { viewModel.actualizar(forzar = true) }) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("  Reintentar")
                        }
                    }
                }
            }
        }

        if (aviso != null) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(24.dp))
                    Text(aviso.orEmpty(), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.descartarAviso() }) { Text("Cerrar") }
                }
            }
        }
    }
}
