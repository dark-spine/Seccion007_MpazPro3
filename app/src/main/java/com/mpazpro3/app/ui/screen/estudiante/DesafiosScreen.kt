package com.mpazpro3.app.ui.screen.estudiante

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.navigation.Rutas
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.ui.components.PantallaCentrada
import com.mpazpro3.app.ui.components.vibrarRespuesta
import com.mpazpro3.app.viewmodel.MpazProViewModel
import kotlinx.coroutines.launch

/**
 * RF-08: desafíos de práctica de dificultad progresiva. Cada respuesta se registra, da
 * retroalimentación inmediata (mensaje animado y vibración) y se puede repetir el desafío.
 */
@Composable
fun DesafiosScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val contenidoId by viewModel.contenidoSeleccionadoId.collectAsState()
    val actividades by viewModel.actividades.collectAsState()
    val desafios = remember(actividades, contenidoId) { viewModel.desafiosDe(contenidoId) }

    var indice by remember(contenidoId) { mutableIntStateOf(0) }
    var elegida by remember(contenidoId) { mutableStateOf<Int?>(null) }
    var fueCorrecta by remember(contenidoId) { mutableStateOf<Boolean?>(null) }
    var terminado by remember(contenidoId) { mutableStateOf(false) }

    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()

    Scaffold(topBar = { BarraSuperior("Desafíos", navController) }) { padding ->
        PantallaCentrada(padding) {
            if (desafios.isEmpty()) {
                Text("Este contenido todavía no tiene desafíos.", style = MaterialTheme.typography.bodyLarge)
                return@PantallaCentrada
            }
            val desafio = desafios[indice.coerceIn(0, desafios.lastIndex)]
            Text("Desafío ${indice + 1} de ${desafios.size} · ${desafio.dificultad.etiqueta}", style = MaterialTheme.typography.titleMedium)
            LinearProgressIndicator(
                progress = { (indice + if (fueCorrecta != null) 1 else 0).toFloat() / desafios.size },
                modifier = Modifier.fillMaxWidth()
            )
            Text(desafio.enunciado, style = MaterialTheme.typography.headlineSmall)

            desafio.alternativas.forEachIndexed { i, alternativa ->
                val color by animateColorAsState(
                    targetValue = when {
                        fueCorrecta == null -> MaterialTheme.colorScheme.primary
                        i == elegida && fueCorrecta == true -> MaterialTheme.colorScheme.secondary
                        i == elegida -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.outline
                    },
                    animationSpec = tween(300),
                    label = "colorAlternativa"
                )
                Button(
                    onClick = {
                        alcance.launch {
                            val correcta = viewModel.responderDesafio(usuario?.id.orEmpty(), desafio, i)
                            if (correcta != null) {
                                elegida = i
                                fueCorrecta = correcta
                                vibrarRespuesta(contexto, correcta)
                            }
                        }
                    },
                    enabled = fueCorrecta == null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = color,
                        disabledContainerColor = color,
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                ) { Text(alternativa.texto, style = MaterialTheme.typography.titleMedium) }
            }

            AnimatedVisibility(
                visible = fueCorrecta != null,
                enter = fadeIn(tween(300)) + slideInVertically { it / 2 }
            ) {
                val bien = fueCorrecta == true
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = if (bien) Icons.Filled.CheckCircle else Icons.Filled.Error,
                        contentDescription = null,
                        tint = if (bien) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        if (bien) "¡Muy bien! Respuesta correcta." else "Casi. Puedes intentarlo de nuevo.",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (fueCorrecta != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    if (fueCorrecta == false) {
                        OutlinedButton(
                            onClick = {
                                elegida = null
                                fueCorrecta = null
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Reintentar") }
                    }
                    Button(
                        onClick = {
                            if (indice < desafios.lastIndex) {
                                indice += 1
                                elegida = null
                                fueCorrecta = null
                            } else {
                                terminado = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(if (indice < desafios.lastIndex) "Siguiente" else "Terminar") }
                }
            }
        }

        if (terminado) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("¡Terminaste los desafíos!") },
                text = { Text("Ya puedes rendir la evaluación final.") },
                confirmButton = {
                    TextButton(onClick = {
                        terminado = false
                        navController.navigate(Rutas.EST_EVALUACION) {
                            popUpTo(Rutas.EST_MATERIAL) { inclusive = true }
                        }
                    }) { Text("Ir a la evaluación") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        terminado = false
                        navController.popBackStack(Rutas.EST_HOME, inclusive = false)
                    }) { Text("Volver al inicio") }
                }
            )
        }
    }
}
