package com.mpazpro3.app.ui.screen.estudiante

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.navigation.Rutas
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.ui.components.PantallaCentrada
import com.mpazpro3.app.ui.components.ReproductorAudio
import com.mpazpro3.app.ui.components.audioDelRecurso
import com.mpazpro3.app.ui.components.imagenDelRecurso
import com.mpazpro3.app.viewmodel.MpazProViewModel
import kotlinx.coroutines.launch

/**
 * RF-07: material didáctico del contenido (imagen, explicación, ejemplo, instrucciones y audio
 * de apoyo opcional). Al terminar, el estudiante confirma que lo revisó y pasa a los desafíos.
 */
@Composable
fun MaterialScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val contenidoId by viewModel.contenidoSeleccionadoId.collectAsState()
    val contenidos by viewModel.contenidos.collectAsState()
    val contenido = contenidos.find { it.id == contenidoId }
    val alcance = rememberCoroutineScope()
    var confirmar by remember { mutableStateOf(false) }

    Scaffold(topBar = { BarraSuperior("Material de estudio", navController) }) { padding ->
        PantallaCentrada(padding) {
            if (contenido == null) {
                Text("Contenido no encontrado.", style = MaterialTheme.typography.bodyLarge)
                return@PantallaCentrada
            }
            Text(contenido.unidad, style = MaterialTheme.typography.headlineSmall)

            val recursos = viewModel.recursosDe(contenido.id)
            if (recursos.isEmpty()) {
                Text("Este contenido todavía no tiene material.", style = MaterialTheme.typography.bodyLarge)
            }
            recursos.forEach { recurso ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(recurso.titulo, style = MaterialTheme.typography.titleMedium)
                        imagenDelRecurso(recurso.id)?.let { imagen ->
                            Image(
                                painter = painterResource(imagen),
                                contentDescription = recurso.descripcionImagen,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        }
                        audioDelRecurso(recurso.id)?.let { ReproductorAudio(it) }
                        Text(recurso.explicacion, style = MaterialTheme.typography.bodyLarge)
                        Text("Ejemplo", style = MaterialTheme.typography.titleSmall)
                        Text(recurso.ejemplo, style = MaterialTheme.typography.bodyMedium)
                        Text("¿Qué tienes que hacer?", style = MaterialTheme.typography.titleSmall)
                        Text(recurso.instrucciones, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Button(
                onClick = { confirmar = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) { Text("Ir a los desafíos") }
        }

        if (confirmar && contenido != null) {
            AlertDialog(
                onDismissRequest = { confirmar = false },
                title = { Text("¿Ya revisaste el material?") },
                text = { Text("Puedes volver a leerlo cuando quieras.") },
                confirmButton = {
                    TextButton(onClick = {
                        confirmar = false
                        alcance.launch {
                            viewModel.confirmarMaterialRevisado(usuario?.id.orEmpty(), contenido.id)
                            navController.navigate(Rutas.EST_DESAFIOS)
                        }
                    }) { Text("Sí, continuar") }
                },
                dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Seguir leyendo") } }
            )
        }
    }
}
