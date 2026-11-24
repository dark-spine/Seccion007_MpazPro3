package com.mpazpro3.app.ui.screen.estudiante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.domain.ReglasEvaluacion
import com.mpazpro3.app.model.Resultado
import com.mpazpro3.app.navigation.Rutas
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.ui.components.MensajeResultado
import com.mpazpro3.app.ui.components.PantallaCentrada
import com.mpazpro3.app.ui.components.vibrarConfirmacion
import com.mpazpro3.app.viewmodel.MpazProViewModel
import kotlinx.coroutines.launch

/**
 * RF-09 y RN-03: evaluación final con un máximo de intentos definido por el docente. El botón
 * se bloquea cuando no quedan intentos y el ViewModel vuelve a verificarlo al enviar.
 */
@Composable
fun EvaluacionFinalScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val contenidoId by viewModel.contenidoSeleccionadoId.collectAsState()
    val actividades by viewModel.actividades.collectAsState()
    val asignaciones by viewModel.asignaciones.collectAsState()
    val seguimientos by viewModel.seguimientos.collectAsState()

    val estudianteId = usuario?.id.orEmpty()
    val preguntas = remember(actividades, contenidoId) { viewModel.evaluacionFinalDe(contenidoId) }
    val restantes = remember(asignaciones, seguimientos, contenidoId) {
        contenidoId?.let { viewModel.intentosRestantes(estudianteId, it) }
    }

    var respuestas by remember(contenidoId) { mutableStateOf(mapOf<String, Int>()) }
    var resultado by remember(contenidoId) { mutableStateOf<Resultado?>(null) }
    var finalizada by remember(contenidoId) { mutableStateOf<Resultado?>(null) }
    var errorIncompleta by remember(contenidoId) { mutableStateOf(false) }

    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    val sinIntentos = restantes != null && restantes <= 0

    Scaffold(topBar = { BarraSuperior("Evaluación final", navController) }) { padding ->
        PantallaCentrada(padding) {
            when {
                restantes == null -> Text(ReglasEvaluacion.MENSAJE_NO_ASIGNADO, style = MaterialTheme.typography.bodyLarge)
                preguntas.isEmpty() -> Text(ReglasEvaluacion.MENSAJE_SIN_PREGUNTAS, style = MaterialTheme.typography.bodyLarge)
                else -> {
                    Text("Intentos que te quedan: $restantes", style = MaterialTheme.typography.titleMedium)
                    if (sinIntentos) {
                        Text(ReglasEvaluacion.MENSAJE_SIN_INTENTOS, style = MaterialTheme.typography.bodyLarge)
                        Button(
                            onClick = { navController.navigate(Rutas.EST_REVISION) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) { Text("Ver mis respuestas") }
                    } else {
                        preguntas.forEachIndexed { numero, pregunta ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .selectableGroup(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("${numero + 1}. ${pregunta.enunciado}", style = MaterialTheme.typography.titleMedium)
                                    pregunta.alternativas.forEachIndexed { i, alternativa ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .selectable(
                                                    selected = respuestas[pregunta.id] == i,
                                                    role = Role.RadioButton,
                                                    onClick = {
                                                        respuestas = respuestas + (pregunta.id to i)
                                                        errorIncompleta = false
                                                    }
                                                )
                                        ) {
                                            RadioButton(selected = respuestas[pregunta.id] == i, onClick = null)
                                            Text("  ${alternativa.texto}", style = MaterialTheme.typography.bodyLarge)
                                        }
                                    }
                                }
                            }
                        }

                        if (errorIncompleta) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Filled.Error, contentDescription = "Dato con error", tint = MaterialTheme.colorScheme.error)
                                Text(ReglasEvaluacion.MENSAJE_INCOMPLETA, color = MaterialTheme.colorScheme.error)
                            }
                        }

                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            onClick = {
                                if (preguntas.any { it.id !in respuestas }) {
                                    errorIncompleta = true
                                } else {
                                    alcance.launch {
                                        val r = viewModel.enviarEvaluacion(estudianteId, contenidoId.orEmpty(), respuestas)
                                        if (r.exito) {
                                            vibrarConfirmacion(contexto)
                                            finalizada = r
                                        } else {
                                            resultado = r
                                        }
                                    }
                                }
                            }
                        ) { Text("Enviar evaluación") }
                    }
                    MensajeResultado(resultado)
                }
            }
        }

        finalizada?.let { r ->
            AlertDialog(
                onDismissRequest = { },
                title = { Text("¡Terminaste la evaluación!") },
                text = { Text(r.mensaje) },
                confirmButton = {
                    TextButton(onClick = {
                        finalizada = null
                        respuestas = emptyMap()
                        resultado = null
                    }) { Text("Aceptar") }
                }
            )
        }
    }
}
