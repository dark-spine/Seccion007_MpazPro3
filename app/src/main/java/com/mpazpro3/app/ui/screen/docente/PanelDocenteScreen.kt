package com.mpazpro3.app.ui.screen.docente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.navigation.Rutas
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.viewmodel.MpazProViewModel

/**
 * Pantalla principal del Docente: resumen de lo asignado a su curso y acceso a la
 * planificación y a los resultados de sus estudiantes.
 */
@Composable
fun PanelDocenteScreen(
    navController: NavHostController,
    viewModel: MpazProViewModel,
    onCerrarSesion: () -> Unit
) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val todas by viewModel.asignaciones.collectAsState()
    val curso = usuario?.curso.orEmpty()
    val asignaciones = viewModel.asignacionesDelCurso(curso)

    Scaffold(topBar = { BarraSuperior("Panel docente", navController, onCerrarSesion) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Hola, ${usuario?.nombre.orEmpty()}", style = MaterialTheme.typography.titleLarge)
            Text("Curso: $curso", style = MaterialTheme.typography.bodyMedium)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Contenidos asignados a tu curso: ${asignaciones.size}", style = MaterialTheme.typography.titleSmall)
                    asignaciones.forEach { asignacion ->
                        val contenido = viewModel.contenidoPorId(asignacion.contenidoId)
                        Text(
                            "• ${contenido?.asignatura?.etiqueta.orEmpty()} — ${contenido?.unidad.orEmpty()} (máx. ${asignacion.maxIntentosEvaluacion} intentos)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text("Asignaciones registradas en total: ${todas.size}", style = MaterialTheme.typography.labelSmall)
                }
            }

            Button(modifier = Modifier.fillMaxWidth(), onClick = { navController.navigate(Rutas.DOCENTE_PLANIFICACION) }) {
                Text("Seleccionar objetivos y asignar contenidos")
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = { navController.navigate(Rutas.DOCENTE_RESULTADOS) }) {
                Text("Resultados e informes de mis estudiantes")
            }
        }
    }
}
