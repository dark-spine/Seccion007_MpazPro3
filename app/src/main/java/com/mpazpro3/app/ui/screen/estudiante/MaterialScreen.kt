package com.mpazpro3.app.ui.screen.estudiante

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(recurso.titulo, style = MaterialTheme.typography.headlineSmall)
                        Text(recurso.explicacion, style = MaterialTheme.typography.bodyLarge)

                        if (recurso.descripcionImagen != null) {
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

                        Text("Ejemplo", style = MaterialTheme.typography.titleMedium)
                        Text(recurso.ejemplo, style = MaterialTheme.typography.bodyLarge)
                        Text("Instrucciones", style = MaterialTheme.typography.titleMedium)
                        Text(recurso.instrucciones, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = {
                    val id = contenidoId
                    if (id != null) viewModel.confirmarMaterialRevisado(usuario?.id.orEmpty(), id)
                    navController.navigate(Rutas.ESTUDIANTE_DESAFIOS)
                }
            ) { Text("¡Listo! Ir a los desafíos", style = MaterialTheme.typography.titleMedium) }
        }
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