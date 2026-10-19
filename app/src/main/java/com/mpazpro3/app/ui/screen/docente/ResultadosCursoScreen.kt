package com.mpazpro3.app.ui.screen.docente

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
 * Módulo "Planificación docente" (resultados): avance de cada estudiante del curso y acceso a
 * su informe pedagógico. RF: "revisar, descargar y validar los resultados de sus estudiantes".
 *
 * TODO(equipo): progresoGeneral() hoy devuelve 0 y descargarResultados() no hace nada.
 * Solo identificadores y nombres FICTICIOS.
 */
@Composable
fun ResultadosCursoScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val estudiantes by viewModel.estudiantes.collectAsState()
    val curso = usuario?.curso.orEmpty()

    Scaffold(topBar = { BarraSuperior("Resultados del curso", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.descargarResultados(curso) }
            ) { Text("Descargar resultados del curso") }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(estudiantes) { estudiante ->
                    val avance = viewModel.progresoGeneral(estudiante.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.seleccionarEstudiante(estudiante.id)
                                navController.navigate(Rutas.DOCENTE_INFORME)
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(estudiante.nombreFicticio, style = MaterialTheme.typography.titleSmall)
                            LinearProgressIndicator(progress = { avance }, modifier = Modifier.fillMaxWidth())
                            Text(
                                "Avance: ${(avance * 100).toInt()}% · Toca para ver su informe",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}
