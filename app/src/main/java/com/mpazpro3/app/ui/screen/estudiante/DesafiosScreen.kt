package com.mpazpro3.app.ui.screen.estudiante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.navigation.Rutas
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.viewmodel.MpazProViewModel

/**
 * Desafíos de práctica, uno a la vez, antes de la evaluación final. RF: "desarrollar
 * actividades y desafíos de dificultad progresiva".
 *
 * TODO(equipo):
 *  - desafiosDe() hoy devuelve TODAS las actividades (incluida la evaluación final) sin ordenar.
 *  - responderDesafio() hoy devuelve null: implementar el registro de la respuesta y devolver
 *    true/false para mostrar "¡Correcto!" o "Inténtalo de nuevo".
 */
@Composable
fun DesafiosScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val contenidoId by viewModel.contenidoSeleccionadoId.collectAsState()
    val usuario by viewModel.usuarioActivo.collectAsState()
    val desafios = remember(contenidoId) { viewModel.desafiosDe(contenidoId) }

    var indice by remember { mutableIntStateOf(0) }
    var resultado by remember { mutableStateOf<Boolean?>(null) }
    var respondido by remember { mutableStateOf(false) }

    Scaffold(topBar = { BarraSuperior("Desafíos", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (desafios.isEmpty()) {
                Text("No hay desafíos para este contenido.", style = MaterialTheme.typography.bodyLarge)
                return@Column
            }

            val actual = desafios[indice.coerceIn(0, desafios.lastIndex)]

            Text("Desafío ${indice + 1} de ${desafios.size} · ${actual.dificultad.etiqueta}", style = MaterialTheme.typography.labelLarge)
            Text(actual.enunciado, style = MaterialTheme.typography.headlineSmall)

            actual.alternativas.forEachIndexed { posicion, alternativa ->
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    onClick = {
                        resultado = viewModel.responderDesafio(usuario?.id.orEmpty(), actual, posicion)
                        respondido = true
                    }
                ) { Text(alternativa.texto, style = MaterialTheme.typography.titleMedium) }
            }

            if (respondido) {
                val texto = when (resultado) {
                    true -> "¡Correcto!"
                    false -> "Inténtalo de nuevo."
                    null -> "(Retroalimentación pendiente: implementar responderDesafio())"
                }
                Text(texto, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = indice > 0,
                    onClick = {
                        indice--
                        respondido = false
                        resultado = null
                    }
                ) { Text("Anterior") }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = indice < desafios.lastIndex,
                    onClick = {
                        indice++
                        respondido = false
                        resultado = null
                    }
                ) { Text("Siguiente") }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = { navController.navigate(Rutas.ESTUDIANTE_EVALUACION) }
            ) { Text("Ir a la evaluación final", style = MaterialTheme.typography.titleMedium) }
        }
    }
}
