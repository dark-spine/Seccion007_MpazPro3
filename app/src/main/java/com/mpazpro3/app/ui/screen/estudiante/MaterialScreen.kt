package com.mpazpro3.app.ui.screen.estudiante

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.R
import com.mpazpro3.app.navigation.Rutas
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.ui.components.ReproductorAudio
import com.mpazpro3.app.viewmodel.MpazProViewModel

/**
 * Material de estudio del contenido elegido: explicación, imagen, audio de apoyo, ejemplo e
 * instrucciones. RF-07: "consultar el material asignado".
 *
 * El audio de apoyo usa MediaPlayer (recurso nativo) a través de ReproductorAudio.
 * El texto escrito siempre se muestra: el audio es complemento, no reemplazo (RNF-02).
 */
@Composable
fun MaterialScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val contenidoId by viewModel.contenidoSeleccionadoId.collectAsState()
    val usuario by viewModel.usuarioActivo.collectAsState()
    val contenido = viewModel.contenidoPorId(contenidoId)
    val recursos = viewModel.recursosDe(contenidoId)

    // Controla el diálogo de confirmación al terminar de revisar el material (RF-11).
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Scaffold(topBar = { BarraSuperior(contenido?.asignatura?.etiqueta ?: "Material", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (recursos.isEmpty()) {
                Text("Todavía no hay material para este contenido.", style = MaterialTheme.typography.bodyLarge)
            }

            recursos.forEach { recurso ->

                // Cada recurso decide por separado si muestra su ejemplo e instrucciones.
                var mostrarEjemplo by remember(recurso.id) { mutableStateOf(false) }

                // La flecha gira 180° al desplegar, para indicar el estado del bloque.
                val giroFlecha by animateFloatAsState(
                    targetValue = if (mostrarEjemplo) 180f else 0f,
                    animationSpec = tween(300),
                    label = "giro"
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(recurso.titulo, style = MaterialTheme.typography.headlineSmall)
                        Text(recurso.explicacion, style = MaterialTheme.typography.bodyLarge)

                        /*
                         * Imagen del recurso (RF-07). Si existe un dibujo para este recurso se
                         * muestra; si no, se deja el recuadro con la descripción escrita.
                         *
                         * contentDescription no es decorativo: es lo que lee el lector de
                         * pantalla, por eso usa la descripción del propio recurso.
                         */
                        val imagenResId = imagenDelRecurso(recurso.id)
                        if (imagenResId != null) {
                            Image(
                                painter = painterResource(id = imagenResId),
                                contentDescription = recurso.descripcionImagen,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else if (recurso.descripcionImagen != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(recurso.descripcionImagen, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        // Audio de apoyo: solo si el recurso lo declara Y existe el archivo.
                        val audioResId = audioDelRecurso(recurso.id)
                        if (recurso.audioDisponible && audioResId != null) {
                            ReproductorAudio(audioResId = audioResId)
                        }

                        /*
                         * Ejemplo e instrucciones van ocultos al entrar.
                         * RNF-01: mostrar menos texto de una vez ayuda a un lector de 3° básico;
                         * el estudiante decide cuándo ver el detalle.
                         */
                        TextButton(onClick = { mostrarEjemplo = !mostrarEjemplo }) {
                            Text(
                                if (mostrarEjemplo) "Ocultar ejemplo e instrucciones"
                                else "Ver ejemplo e instrucciones",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Icon(
                                imageVector = Icons.Filled.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(giroFlecha)
                            )
                        }

                        AnimatedVisibility(
                            visible = mostrarEjemplo,
                            enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                            exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Ejemplo", style = MaterialTheme.typography.titleMedium)
                                Text(recurso.ejemplo, style = MaterialTheme.typography.bodyLarge)
                                Text("Instrucciones", style = MaterialTheme.typography.titleMedium)
                                Text(recurso.instrucciones, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = {
                    val id = contenidoId
                    if (id != null) {
                        viewModel.confirmarMaterialRevisado(usuario?.id.orEmpty(), id)
                        mostrarConfirmacion = true
                    } else {
                        navController.navigate(Rutas.ESTUDIANTE_DESAFIOS)
                    }
                }
            ) { Text("¡Listo! Ir a los desafíos", style = MaterialTheme.typography.titleMedium) }
        }
    }

    /*
     * RF-11: "confirmar la finalización de cada actividad".
     * El diálogo avisa que el avance quedó guardado antes de continuar.
     */
    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("¡Terminaste de revisar el material!") },
            text = {
                Text(
                    "Tu avance quedó guardado. Ahora puedes practicar con los desafíos.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacion = false
                    navController.navigate(Rutas.ESTUDIANTE_DESAFIOS)
                }) { Text("Ir a los desafíos") }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarConfirmacion = false
                    navController.popBackStack()
                }) { Text("Volver a mis actividades") }
            }
        )
    }
}

/**
 * Relaciona cada recurso con su archivo de audio en res/raw.
 *
 * Se resuelve acá y no en la base de datos porque el id de un recurso de Android
 * (R.raw.*) se genera al compilar y no tiene sentido guardarlo en una tabla.
 * Si a futuro se agregan más audios, basta con sumar una línea al when.
 */
private fun audioDelRecurso(recursoId: String): Int? = when (recursoId) {
    "R1" -> R.raw.centenas_explicacion
    else -> null
}

/**
 * Relaciona cada recurso con su ilustración en res/drawable.
 *
 * Mismo criterio que audioDelRecurso(): los identificadores R.drawable.* se generan al
 * compilar, así que no tiene sentido guardarlos en la base de datos.
 */
private fun imagenDelRecurso(recursoId: String): Int? = when (recursoId) {
    "R1" -> R.drawable.bloques_base_diez
    else -> null
}