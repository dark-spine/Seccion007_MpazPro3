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
import com.mpazpro3.app.model.EstadoValidacion
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.viewmodel.MpazProViewModel

/**
 * Módulo "Informe pedagógico": informe de uso docente del estudiante elegido (rendimiento,
 * fortalezas, aspectos a reforzar) con su estado de validación.
 *
 * Restricciones del caso: el estudiante NO puede ver esta pantalla, y el informe no emite
 * diagnósticos ni consecuencias académicas automáticas.
 *
 * TODO(equipo): generarInforme() y validarInforme() todavía no hacen nada. Pendiente con el
 * CITT: si el docente puede editar el informe antes de validarlo.
 */
@Composable
fun InformeDocenteScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val estudianteId by viewModel.estudianteSeleccionadoId.collectAsState()
    val informes by viewModel.informes.collectAsState()
    val estudiante = viewModel.estudiantePorId(estudianteId)
    val informe = informes.find { it.estudianteId == estudianteId }

    Scaffold(topBar = { BarraSuperior("Informe pedagógico", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(estudiante?.nombreFicticio ?: "Estudiante", style = MaterialTheme.typography.titleLarge)
            Text(
                "Informe de uso docente. No constituye un diagnóstico ni define consecuencias académicas.",
                style = MaterialTheme.typography.bodySmall
            )

            if (informe == null) {
                Text("Todavía no hay un informe generado para este estudiante.", style = MaterialTheme.typography.bodyMedium)
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { estudianteId?.let { viewModel.generarInforme(it) } }
                ) { Text("Generar informe") }
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Rendimiento", style = MaterialTheme.typography.titleSmall)
                        Text(informe.rendimiento)
                        Text("Fortalezas", style = MaterialTheme.typography.titleSmall)
                        informe.fortalezas.forEach { Text("• $it") }
                        Text("Aspectos a reforzar", style = MaterialTheme.typography.titleSmall)
                        informe.aspectosAReforzar.forEach { Text("• $it") }
                        Text("Fecha: ${informe.fecha}", style = MaterialTheme.typography.labelSmall)
                        Text("Estado: ${informe.estado.etiqueta}", style = MaterialTheme.typography.labelMedium)
                    }
                }
                if (informe.estado == EstadoValidacion.BORRADOR) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.validarInforme(informe.id) }
                    ) { Text("Validar informe") }
                }
            }
        }
    }
}
