package com.mpazpro3.app.ui.screen.docente

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.model.EstadoValidacion
import com.mpazpro3.app.model.Resultado
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.ui.components.MensajeResultado
import com.mpazpro3.app.ui.components.PantallaCentrada
import com.mpazpro3.app.ui.components.vibrarConfirmacion
import com.mpazpro3.app.viewmodel.MpazProViewModel
import kotlinx.coroutines.launch

/**
 * RF-12, RF-13 y RF-14: el docente genera el informe pedagógico de un estudiante (siempre nace
 * en borrador), lo revisa y lo valida. Un informe validado ya no se regenera. El estudiante no
 * tiene acceso a esta pantalla (RN-01).
 */
@Composable
fun InformeDocenteScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val estudianteId by viewModel.estudianteSeleccionadoId.collectAsState()
    val estudiantes by viewModel.estudiantes.collectAsState()
    val informes by viewModel.informes.collectAsState()

    val estudiante = remember(estudiantes, estudianteId) { viewModel.estudiantePorId(estudianteId) }
    val informe = remember(informes, estudianteId) { viewModel.informeDe(estudianteId) }

    var resultado by remember(estudianteId) { mutableStateOf<Resultado?>(null) }
    var confirmarValidacion by remember { mutableStateOf(false) }
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()

    Scaffold(topBar = { BarraSuperior("Informe pedagógico", navController) }) { padding ->
        PantallaCentrada(padding) {
            if (estudiante == null) {
                Text("Estudiante no encontrado.", style = MaterialTheme.typography.bodyLarge)
                return@PantallaCentrada
            }
            Text(estudiante.nombreFicticio, style = MaterialTheme.typography.titleLarge)

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    "Este informe es de apoyo para el docente. No constituye un diagnóstico ni define consecuencias académicas.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (informe == null) {
                Text("Todavía no se ha generado un informe para este estudiante.", style = MaterialTheme.typography.bodyLarge)
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Fecha: ${informe.fecha}", style = MaterialTheme.typography.labelLarge)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (informe.estado == EstadoValidacion.VALIDADO) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            }
                            Text("Estado: ${informe.estado.etiqueta}", style = MaterialTheme.typography.titleSmall)
                        }
                        Text("Rendimiento", style = MaterialTheme.typography.titleMedium)
                        Text(informe.rendimiento, style = MaterialTheme.typography.bodyMedium)
                        Text("Fortalezas", style = MaterialTheme.typography.titleMedium)
                        informe.fortalezas.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                        Text("Aspectos a reforzar", style = MaterialTheme.typography.titleMedium)
                        informe.aspectosAReforzar.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }

            val validado = informe?.estado == EstadoValidacion.VALIDADO
            AnimatedVisibility(visible = !validado) {
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    onClick = {
                        alcance.launch { resultado = viewModel.generarInforme(estudiante.id) }
                    }
                ) { Text(if (informe == null) "Generar informe" else "Regenerar informe") }
            }
            AnimatedVisibility(visible = informe != null && !validado) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    onClick = { confirmarValidacion = true }
                ) { Text("Validar informe") }
            }

            MensajeResultado(resultado)
        }

        if (confirmarValidacion && informe != null) {
            AlertDialog(
                onDismissRequest = { confirmarValidacion = false },
                title = { Text("¿Validar este informe?") },
                text = { Text("Una vez validado, el informe no se podrá regenerar.") },
                confirmButton = {
                    TextButton(onClick = {
                        confirmarValidacion = false
                        alcance.launch {
                            val r = viewModel.validarInforme(informe.id)
                            resultado = r
                            if (r.exito) vibrarConfirmacion(contexto)
                        }
                    }) { Text("Validar") }
                },
                dismissButton = { TextButton(onClick = { confirmarValidacion = false }) { Text("Cancelar") } }
            )
        }
    }
}
