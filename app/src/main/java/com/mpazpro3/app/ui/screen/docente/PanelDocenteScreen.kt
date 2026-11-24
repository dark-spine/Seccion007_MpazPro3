package com.mpazpro3.app.ui.screen.docente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.navigation.Rutas
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.ui.components.PantallaCentrada
import com.mpazpro3.app.viewmodel.MpazProViewModel

/** Pantalla principal del docente: acceso a la planificación (RF-03..05) y a los resultados (RF-06, RF-12..14). */
@Composable
fun PanelDocenteScreen(
    navController: NavHostController,
    viewModel: MpazProViewModel,
    onCerrarSesion: () -> Unit
) {
    val usuario by viewModel.usuarioActivo.collectAsState()

    // Al abrir la pantalla se pide al servidor la información más reciente (API -> Room -> pantalla).
    LaunchedEffect(Unit) { viewModel.actualizar() }
    val contenidos by viewModel.contenidos.collectAsState()
    val asignaciones by viewModel.asignaciones.collectAsState()
    val curso = usuario?.curso

    Scaffold(topBar = { BarraSuperior("Panel docente", navController, onCerrarSesion) }) { padding ->
        PantallaCentrada(padding) {
            Text("Hola, ${usuario?.nombre.orEmpty()}", style = MaterialTheme.typography.titleLarge)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Curso: ${curso ?: "sin curso asociado"}", style = MaterialTheme.typography.titleMedium)
                    val autorizados = viewModel.contenidosDisponiblesParaAsignar().size
                    val asignados = if (curso == null) 0 else viewModel.asignacionesDelCurso(curso).size
                    Text("Contenidos autorizados por UTP: $autorizados", style = MaterialTheme.typography.bodyMedium)
                    Text("Contenidos asignados al curso: $asignados", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Button(
                onClick = { navController.navigate(Rutas.DOCENTE_PLANIFICACION) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Filled.EventNote, contentDescription = null)
                Text("  Planificar actividades")
            }
            OutlinedButton(
                onClick = { navController.navigate(Rutas.DOCENTE_RESULTADOS) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Filled.Assessment, contentDescription = null)
                Text("  Resultados e informes")
            }
        }
    }
}
