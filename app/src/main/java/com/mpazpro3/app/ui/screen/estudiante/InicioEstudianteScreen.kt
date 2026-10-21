package com.mpazpro3.app.ui.screen.estudiante

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.navigation.Rutas
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.viewmodel.MpazProViewModel
import kotlinx.coroutines.delay

/**
 * Módulo "Experiencia del estudiante" — pantalla principal del estudiante: sus asignaturas
 * asignadas con su avance. RNF-01: textos grandes, instrucciones directas, botones visibles.
 *
 * RF-07: contenidosDelEstudiante() devuelve solo los contenidos asignados a su curso.
 * RF-11: progreso() calcula el avance guardado en Room.
 */
@Composable
fun InicioEstudianteScreen(
    navController: NavHostController,
    viewModel: MpazProViewModel,
    onCerrarSesion: () -> Unit
) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val todos by viewModel.contenidos.collectAsState()

    // Se observa para que la barra se redibuje sola cuando cambia el avance en la base de datos.
    val avances by viewModel.avances.collectAsState()

    val misContenidos = viewModel.contenidosDelEstudiante(usuario)
    val estudianteId = usuario?.id.orEmpty()

    // Controla la aparición progresiva de las tarjetas al entrar a la pantalla.
    var mostrarLista by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { mostrarLista = true }

    Scaffold(topBar = { BarraSuperior("Mis actividades", navController, onCerrarSesion) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("¡Hola! Elige una asignatura para comenzar.", style = MaterialTheme.typography.titleLarge)
            Text(
                "Contenidos disponibles: ${misContenidos.size} de ${todos.size}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Transición de entrada: las tarjetas aparecen desplegándose en lugar de saltar.
            AnimatedVisibility(
                visible = mostrarLista,
                enter = fadeIn(animationSpec = tween(400)) + expandVertically(animationSpec = tween(400))
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(misContenidos) { contenido ->

                        // avances se lee arriba; al cambiar, progreso() devuelve el nuevo valor.
                        val avanceReal = viewModel.progreso(estudianteId, contenido.id)

                        /*
                         * Animatable da control explícito sobre la animación, a diferencia de
                         * animateFloatAsState, que solo reacciona si el valor objetivo cambia
                         * estando el composable ya en pantalla.
                         *
                         * Secuencia: se fija en 0 de inmediato (snapTo), se espera a que la
                         * tarjeta termine de aparecer, y recién ahí la barra crece hasta el
                         * valor real durante 1,5 segundos.
                         */
                        val barra = remember(contenido.id) { Animatable(0f) }
                        LaunchedEffect(avanceReal) {
                            barra.snapTo(0f)
                            delay(400)
                            barra.animateTo(
                                targetValue = avanceReal,
                                animationSpec = tween(durationMillis = 800)
                            )
                        }

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(contenido.asignatura.etiqueta, style = MaterialTheme.typography.headlineSmall)
                                Text(contenido.unidad, style = MaterialTheme.typography.bodyLarge)

                                LinearProgressIndicator(
                                    progress = { barra.value },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (avanceReal <= 0f) "Sin comenzar" else "Avance",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = "${(barra.value * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    onClick = {
                                        viewModel.seleccionarContenido(contenido.id)
                                        navController.navigate(Rutas.ESTUDIANTE_MATERIAL)
                                    }
                                ) {
                                    Text(
                                        if (avanceReal <= 0f) "Comenzar" else "Continuar",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}