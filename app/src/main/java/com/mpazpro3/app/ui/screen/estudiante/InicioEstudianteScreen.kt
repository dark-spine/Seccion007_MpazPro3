package com.mpazpro3.app.ui.screen.estudiante

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.domain.ReglasProgreso
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.navigation.Rutas
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.ui.components.PantallaCentrada
import com.mpazpro3.app.viewmodel.MpazProViewModel

/** RF-07 y RF-11: actividades que el docente asignó al curso y avance del estudiante en cada una. */
@Composable
fun InicioEstudianteScreen(
    navController: NavHostController,
    viewModel: MpazProViewModel,
    onCerrarSesion: () -> Unit
) {
    val usuario by viewModel.usuarioActivo.collectAsState()

    // Al abrir la pantalla se pide al servidor la información más reciente (API -> Room -> pantalla).
    LaunchedEffect(Unit) { viewModel.actualizar() }
    val contenidos by viewModel.contenidos.collectAsState()
    val asignaciones by viewModel.asignaciones.collectAsState()
    val actividades by viewModel.actividades.collectAsState()
    val seguimientos by viewModel.seguimientos.collectAsState()

    val asignados = remember(usuario, contenidos, asignaciones) { viewModel.contenidosDelEstudiante(usuario) }
    val estudianteId = usuario?.id.orEmpty()
    val general = remember(asignados, actividades, seguimientos) { viewModel.progresoGeneral(estudianteId) }
    val animado by animateFloatAsState(targetValue = general, animationSpec = tween(700), label = "avanceGeneral")

    Scaffold(topBar = { BarraSuperior("Mis actividades", navController, onCerrarSesion) }) { padding ->
        PantallaCentrada(padding) {
            Text("¡Hola, ${usuario?.nombre.orEmpty()}!", style = MaterialTheme.typography.headlineSmall)

            if (asignados.isEmpty()) {
                Text("Tu profesor todavía no te ha asignado actividades", style = MaterialTheme.typography.bodyLarge)
                return@PantallaCentrada
            }

            Text("Mi avance general: ${ReglasProgreso.porcentaje(general)} %", style = MaterialTheme.typography.titleMedium)
            LinearProgressIndicator(
                progress = { animado },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .semantics { contentDescription = "Avance general ${ReglasProgreso.porcentaje(general)} por ciento" }
            )

            asignados.forEach { contenido ->
                TarjetaActividad(
                    contenido = contenido,
                    avance = viewModel.progreso(estudianteId, contenido.id),
                    onComenzar = {
                        viewModel.seleccionarContenido(contenido.id)
                        navController.navigate(Rutas.EST_MATERIAL)
                    },
                    materialRevisado = seguimientos.any { it.estudianteId == estudianteId && it.contenidoId == contenido.id && it.materialRevisado },
                    onDesafios = {
                        viewModel.seleccionarContenido(contenido.id)
                        navController.navigate(Rutas.EST_DESAFIOS)
                    },
                    onRespuestas = {
                        viewModel.seleccionarContenido(contenido.id)
                        navController.navigate(Rutas.EST_REVISION)
                    }
                )
            }
        }
    }
}

@Composable
private fun TarjetaActividad(contenido: Contenido, avance: Float, onComenzar: () -> Unit, materialRevisado: Boolean, onDesafios: () -> Unit, onRespuestas: () -> Unit) {
    val animado by animateFloatAsState(targetValue = avance, animationSpec = tween(700), label = "avanceContenido")
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(contenido.asignatura.etiqueta, style = MaterialTheme.typography.labelLarge)
            Text(contenido.unidad, style = MaterialTheme.typography.titleMedium)
            Text(contenido.objetivoAprendizaje, style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(
                progress = { animado },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .semantics { contentDescription = "Avance ${ReglasProgreso.porcentaje(avance)} por ciento" }
            )
            Text("${ReglasProgreso.porcentaje(avance)} % completado", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = onDesafios, enabled = materialRevisado, modifier = Modifier.fillMaxWidth()) { Text("Ir a los desafíos") }
            if (!materialRevisado) Text("Revisa el material para habilitar los desafíos.", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onComenzar, modifier = Modifier.weight(1f).height(52.dp)) { Text("Comenzar") }
                OutlinedButton(onClick = onRespuestas, modifier = Modifier.weight(1f).height(52.dp)) { Text("Mis respuestas") }
            }
        }
    }
}
