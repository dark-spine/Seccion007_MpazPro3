package com.mpazpro3.app.ui.screen.docente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.viewmodel.MpazProViewModel

/**
 * Módulo "Planificación docente": el docente selecciona los objetivos/contenidos disponibles
 * (autorizados por UTP), define el máximo de intentos de la evaluación final y los asigna a su
 * curso.
 *
 * TODO(equipo):
 *  - contenidosDisponiblesParaAsignar() hoy muestra TODOS los contenidos; debe mostrar solo los
 *    aprobados (regla de negocio).
 *  - asignarAlCurso() todavía no guarda nada.
 */
@Composable
fun PlanificacionDocenteScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val todos by viewModel.contenidos.collectAsState()
    val disponibles = remember(todos) { viewModel.contenidosDisponiblesParaAsignar() }

    var seleccionados by remember { mutableStateOf(setOf<String>()) }
    var maxIntentos by remember { mutableIntStateOf(2) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { BarraSuperior("Planificación docente", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("1. Selecciona los objetivos a trabajar", style = MaterialTheme.typography.titleSmall)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(disponibles) { contenido ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = contenido.id in seleccionados,
                                onCheckedChange = { marcado ->
                                    seleccionados = if (marcado) seleccionados + contenido.id else seleccionados - contenido.id
                                }
                            )
                            Column {
                                Text(contenido.asignatura.etiqueta, style = MaterialTheme.typography.labelMedium)
                                Text(contenido.objetivoAprendizaje, style = MaterialTheme.typography.bodyMedium)
                                Text(contenido.estado.etiqueta, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            Text("2. Máximo de intentos de la evaluación final", style = MaterialTheme.typography.titleSmall)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = { if (maxIntentos > 1) maxIntentos-- }) { Text("−") }
                Text("$maxIntentos", style = MaterialTheme.typography.headlineSmall)
                OutlinedButton(onClick = { if (maxIntentos < 10) maxIntentos++ }) { Text("+") }
            }

            mensaje?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = seleccionados.isNotEmpty(),
                onClick = {
                    viewModel.asignarAlCurso(usuario?.curso.orEmpty(), seleccionados.toList(), maxIntentos)
                    mensaje = "Asignación enviada (se verá en el panel cuando asignarAlCurso() esté implementada)."
                    seleccionados = emptySet()
                }
            ) { Text("3. Asignar a mi curso") }
        }
    }
}
