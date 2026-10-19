package com.mpazpro3.app.ui.screen.utp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.viewmodel.MpazProViewModel

/**
 * Detalle de un contenido para UTP: datos curriculares completos, recursos didácticos asociados,
 * observación y botones Aprobar / Rechazar.
 *
 * TODO(equipo): aprobarContenido() y rechazarContenido() todavía no cambian el estado.
 */
@Composable
fun DetalleContenidoUtpScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val seleccionadoId by viewModel.contenidoSeleccionadoId.collectAsState()
    val contenidos by viewModel.contenidos.collectAsState()
    val contenido = contenidos.find { it.id == seleccionadoId }

    Scaffold(topBar = { BarraSuperior("Revisar contenido", navController) }) { padding ->
        if (contenido == null) {
            Text("Contenido no encontrado.", modifier = Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }
        val recursos = viewModel.recursosDe(contenido.id)
        var observacion by remember(contenido.id) { mutableStateOf(contenido.observacionUtp) }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(contenido.unidad, style = MaterialTheme.typography.titleLarge)
                    Dato("Nivel", contenido.nivel)
                    Dato("Asignatura", contenido.asignatura.etiqueta)
                    Dato("Objetivo de Aprendizaje", contenido.objetivoAprendizaje)
                    Dato("Código OA", contenido.codigoOA ?: "Sin código")
                    Dato("Eje curricular", contenido.ejeCurricular)
                    Dato("Recurso de referencia", contenido.recursoReferencia)
                    Dato("Estado", contenido.estado.etiqueta)
                }
            }

            Text("Recursos didácticos (${recursos.size})", style = MaterialTheme.typography.titleSmall)
            if (recursos.isEmpty()) {
                Text("Este contenido todavía no tiene recursos.", style = MaterialTheme.typography.bodySmall)
            }
            recursos.forEach { recurso ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(recurso.titulo, style = MaterialTheme.typography.titleSmall)
                        Text(recurso.explicacion, style = MaterialTheme.typography.bodySmall)
                        Text(
                            if (recurso.audioDisponible) "Incluye audio de apoyo" else "Sin audio de apoyo",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            OutlinedTextField(
                value = observacion,
                onValueChange = { observacion = it },
                label = { Text("Observación de UTP") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.aprobarContenido(contenido.id, observacion.trim()) }
                ) { Text("Aprobar") }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.rechazarContenido(contenido.id, observacion.trim()) }
                ) { Text("Rechazar") }
            }
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Text("$etiqueta: $valor", style = MaterialTheme.typography.bodyMedium)
}
