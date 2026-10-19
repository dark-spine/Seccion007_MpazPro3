package com.mpazpro3.app.ui.screen.estudiante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
 * Revisión de respuestas: el estudiante ve cuáles de sus respuestas fueron correctas o
 * incorrectas. RF: "revisar cuáles de sus respuestas fueron correctas o incorrectas".
 *
 * Importante: esta pantalla muestra SUS respuestas, no el informe pedagógico (al que el
 * estudiante no tiene acceso).
 *
 * TODO(equipo): respuestasDe() hoy devuelve una lista vacía.
 */
@Composable
fun RevisionRespuestasScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val contenidoId by viewModel.contenidoSeleccionadoId.collectAsState()
    val usuario by viewModel.usuarioActivo.collectAsState()
    val respuestas = viewModel.respuestasDe(usuario?.id.orEmpty(), contenidoId.orEmpty())

    Scaffold(topBar = { BarraSuperior("Mis respuestas", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (respuestas.isEmpty()) {
                Text(
                    "Todavía no hay respuestas para revisar (respuestasDe() está pendiente de implementar).",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(respuestas) { respuesta ->
                    val actividad = viewModel.actividadPorId(respuesta.actividadId)
                    val elegida = actividad?.alternativas?.getOrNull(respuesta.alternativaElegida)?.texto.orEmpty()
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(actividad?.enunciado.orEmpty(), style = MaterialTheme.typography.titleMedium)
                            Text("Tu respuesta: $elegida", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                if (respuesta.esCorrecta) "✔ Correcta" else "✘ Incorrecta",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (respuesta.esCorrecta) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = {
                    navController.navigate(Rutas.ESTUDIANTE_INICIO) {
                        popUpTo(Rutas.ESTUDIANTE_INICIO) { inclusive = true }
                    }
                }
            ) { Text("Volver a mis actividades", style = MaterialTheme.typography.titleMedium) }
        }
    }
}
