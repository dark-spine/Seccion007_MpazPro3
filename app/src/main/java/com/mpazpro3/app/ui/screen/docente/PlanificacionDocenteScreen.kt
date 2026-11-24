package com.mpazpro3.app.ui.screen.docente

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.domain.ReglasPlanificacion
import com.mpazpro3.app.model.Resultado
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.ui.components.MensajeResultado
import com.mpazpro3.app.ui.components.PantallaCentrada
import com.mpazpro3.app.ui.components.vibrarConfirmacion
import com.mpazpro3.app.viewmodel.MpazProViewModel
import kotlinx.coroutines.launch

/**
 * RF-03, RF-04 y RF-05: el docente ve solo los contenidos autorizados por UTP, elige cuáles
 * asignar al curso y define el máximo de intentos de la evaluación final (1 a 10).
 */
@Composable
fun PlanificacionDocenteScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val contenidos by viewModel.contenidos.collectAsState()
    val asignaciones by viewModel.asignaciones.collectAsState()
    val curso = usuario?.curso

    val disponibles = remember(contenidos) { viewModel.contenidosDisponiblesParaAsignar() }
    val yaAsignados = remember(asignaciones, curso) {
        curso?.let { viewModel.asignacionesDelCurso(it) }.orEmpty().associateBy { it.contenidoId }
    }

    var seleccion by remember { mutableStateOf(setOf<String>()) }
    var maxIntentos by remember { mutableIntStateOf(2) }
    var resultado by remember { mutableStateOf<Resultado?>(null) }
    val errorIntentos = ReglasPlanificacion.errorMaxIntentos(maxIntentos)

    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()

    Scaffold(topBar = { BarraSuperior("Planificar actividades", navController) }) { padding ->
        PantallaCentrada(padding) {
            if (curso == null) {
                Text(ReglasPlanificacion.MENSAJE_SIN_CURSO, style = MaterialTheme.typography.bodyLarge)
                return@PantallaCentrada
            }
            Text("Curso: $curso", style = MaterialTheme.typography.titleMedium)

            if (disponibles.isEmpty()) {
                Text("UTP todavía no ha autorizado contenidos", style = MaterialTheme.typography.bodyLarge)
                return@PantallaCentrada
            }

            Text("Contenidos autorizados por UTP", style = MaterialTheme.typography.titleMedium)
            disponibles.forEach { contenido ->
                val asignacion = yaAsignados[contenido.id]
                val marcado = contenido.id in seleccion
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = marcado,
                            onCheckedChange = { activo ->
                                seleccion = if (activo) seleccion + contenido.id else seleccion - contenido.id
                                resultado = null
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(contenido.asignatura.etiqueta, style = MaterialTheme.typography.labelLarge)
                            Text(contenido.unidad, style = MaterialTheme.typography.titleSmall)
                            Text(contenido.objetivoAprendizaje, style = MaterialTheme.typography.bodySmall)
                            if (asignacion != null) {
                                Text(
                                    "Ya asignado · ${asignacion.maxIntentosEvaluacion} intento(s) en la evaluación",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            Text("Máximo de intentos de la evaluación final", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilledTonalIconButton(onClick = {
                    maxIntentos = (maxIntentos - 1).coerceAtLeast(ReglasPlanificacion.MIN_INTENTOS - 1)
                    resultado = null
                }) { Icon(Icons.Filled.Remove, contentDescription = "Restar un intento") }
                Text("$maxIntentos", style = MaterialTheme.typography.headlineMedium)
                FilledTonalIconButton(onClick = {
                    maxIntentos = (maxIntentos + 1).coerceAtMost(ReglasPlanificacion.MAX_INTENTOS + 1)
                    resultado = null
                }) { Icon(Icons.Filled.Add, contentDescription = "Sumar un intento") }
            }
            AnimatedVisibility(visible = errorIntentos != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.Error, contentDescription = "Dato con error", tint = MaterialTheme.colorScheme.error)
                    Text(errorIntentos.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = errorIntentos == null,
                onClick = {
                    alcance.launch {
                        val r = viewModel.asignarAlCurso(curso, seleccion.toList(), maxIntentos)
                        resultado = r
                        if (r.exito) {
                            vibrarConfirmacion(contexto)
                            seleccion = emptySet()
                        }
                    }
                }
            ) { Text("Asignar al curso") }

            MensajeResultado(resultado)
        }
    }
}
