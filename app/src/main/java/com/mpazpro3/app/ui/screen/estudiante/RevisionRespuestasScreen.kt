package com.mpazpro3.app.ui.screen.estudiante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.ui.components.PantallaCentrada
import com.mpazpro3.app.viewmodel.MpazProViewModel

/**
 * RF-10: el estudiante revisa sus propias respuestas (la última de cada desafío y las del último
 * intento de la evaluación) con su resultado y, si falló, la respuesta correcta.
 */
@Composable
fun RevisionRespuestasScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val contenidoId by viewModel.contenidoSeleccionadoId.collectAsState()
    val actividades by viewModel.actividades.collectAsState()
    val seguimientos by viewModel.seguimientos.collectAsState()

    val respuestas = remember(usuario, contenidoId, actividades, seguimientos) {
        contenidoId?.let { viewModel.respuestasDe(usuario?.id.orEmpty(), it) }.orEmpty()
    }

    Scaffold(topBar = { BarraSuperior("Mis respuestas", navController) }) { padding ->
        PantallaCentrada(padding) {
            if (respuestas.isEmpty()) {
                Text("Todavía no has respondido actividades de este contenido.", style = MaterialTheme.typography.bodyLarge)
                return@PantallaCentrada
            }
            respuestas.forEach { respuesta ->
                val actividad = actividades.find { it.id == respuesta.actividadId } ?: return@forEach
                val tipo = if (actividad.esEvaluacionFinal) "Evaluación (intento ${respuesta.numeroIntento})" else "Desafío"
                val color = if (respuesta.esCorrecta) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(tipo, style = MaterialTheme.typography.labelLarge)
                        Text(actividad.enunciado, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Tu respuesta: ${actividad.alternativas.getOrNull(respuesta.alternativaElegida)?.texto.orEmpty()}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = if (respuesta.esCorrecta) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                contentDescription = null,
                                tint = color
                            )
                            Text(
                                if (respuesta.esCorrecta) "✔ Correcta" else "✘ Incorrecta",
                                style = MaterialTheme.typography.titleSmall,
                                color = color
                            )
                        }
                        if (!respuesta.esCorrecta) {
                            val correcta = actividad.alternativas.firstOrNull { it.esCorrecta }
                            if (correcta != null) {
                                Text("Respuesta correcta: ${correcta.texto}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
