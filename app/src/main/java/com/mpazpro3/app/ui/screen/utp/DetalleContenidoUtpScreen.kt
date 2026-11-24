package com.mpazpro3.app.ui.screen.utp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.domain.ReglasContenido
import com.mpazpro3.app.model.Resultado
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.ui.components.MensajeResultado
import com.mpazpro3.app.ui.components.PantallaCentrada
import com.mpazpro3.app.ui.components.vibrarConfirmacion
import com.mpazpro3.app.viewmodel.MpazProViewModel
import kotlinx.coroutines.launch

/**
 * RF-01: detalle de un contenido para UTP. Muestra los datos curriculares, los recursos
 * didácticos asociados y la observación; permite Aprobar o Rechazar.
 *
 *  - Rechazar exige un motivo (error en el campo) y pide confirmación antes de rechazar.
 *  - Aprobar respeta RN-04: una sola unidad aprobada por asignatura.
 *  - El resultado se muestra en un aviso animado y se refuerza con una vibración corta.
 */
@Composable
fun DetalleContenidoUtpScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val seleccionadoId by viewModel.contenidoSeleccionadoId.collectAsState()
    val contenidos by viewModel.contenidos.collectAsState()
    val contenido = contenidos.find { it.id == seleccionadoId }

    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()

    Scaffold(topBar = { BarraSuperior("Revisar contenido", navController) }) { padding ->
        if (contenido == null) {
            Text("Contenido no encontrado.", modifier = Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }

        val recursos = viewModel.recursosDe(contenido.id)
        var observacion by remember(contenido.id) { mutableStateOf(contenido.observacionUtp) }
        var errorObservacion by remember(contenido.id) { mutableStateOf<String?>(null) }
        var resultado by remember(contenido.id) { mutableStateOf<Resultado?>(null) }
        var confirmarRechazo by remember { mutableStateOf(false) }

        val colorEstado by animateColorAsState(
            targetValue = presentacionDeEstado(contenido.estado).second,
            animationSpec = tween(durationMillis = 500),
            label = "colorEstado"
        )

        PantallaCentrada(padding, espacio = 10.dp) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(contenido.unidad, style = MaterialTheme.typography.titleLarge)
                    Dato("Nivel", contenido.nivel)
                    Dato("Asignatura", contenido.asignatura.etiqueta)
                    Dato("Objetivo de Aprendizaje", contenido.objetivoAprendizaje)
                    Dato("Código OA", contenido.codigoOA ?: "Sin código")
                    Dato("Eje curricular", contenido.ejeCurricular)
                    Dato("Recurso de referencia", contenido.recursoReferencia)
                    Text(
                        "Estado: ${contenido.estado.etiqueta}",
                        style = MaterialTheme.typography.titleSmall,
                        color = colorEstado
                    )
                }
            }

            Text("Recursos didácticos (${recursos.size})", style = MaterialTheme.typography.titleMedium)
            if (recursos.isEmpty()) {
                Text("Este contenido todavía no tiene recursos.", style = MaterialTheme.typography.bodyMedium)
            }
            recursos.forEach { recurso ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(recurso.titulo, style = MaterialTheme.typography.titleSmall)
                        Text(recurso.explicacion, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (recurso.audioDisponible) "Incluye audio de apoyo" else "Sin audio de apoyo",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            OutlinedTextField(
                value = observacion,
                onValueChange = {
                    observacion = it
                    errorObservacion = null
                },
                label = { Text("Observación de UTP") },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                trailingIcon = {
                    if (errorObservacion != null) {
                        Icon(Icons.Filled.Error, contentDescription = "Dato con error", tint = MaterialTheme.colorScheme.error)
                    }
                },
                isError = errorObservacion != null,
                supportingText = if (errorObservacion != null) {
                    { Text(errorObservacion.orEmpty()) }
                } else {
                    { Text("Obligatoria para rechazar; opcional al aprobar.") }
                },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    onClick = {
                        alcance.launch {
                            val r = viewModel.aprobarContenido(contenido.id, observacion)
                            resultado = r
                            if (r.exito) vibrarConfirmacion(contexto)
                        }
                    }
                ) { Text("Aprobar") }
                OutlinedButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    onClick = {
                        errorObservacion = ReglasContenido.errorObservacionAlRechazar(observacion)
                        if (errorObservacion == null) confirmarRechazo = true
                    }
                ) { Text("Rechazar") }
            }

            MensajeResultado(resultado)
        }

        if (confirmarRechazo) {
            AlertDialog(
                onDismissRequest = { confirmarRechazo = false },
                title = { Text("¿Rechazar este contenido?") },
                text = { Text("El contenido quedará rechazado y los docentes no podrán asignarlo.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            confirmarRechazo = false
                            alcance.launch {
                                val r = viewModel.rechazarContenido(contenido.id, observacion)
                                resultado = r
                                if (r.exito) vibrarConfirmacion(contexto)
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Rechazar") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmarRechazo = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Text("$etiqueta: $valor", style = MaterialTheme.typography.bodyMedium)
}
