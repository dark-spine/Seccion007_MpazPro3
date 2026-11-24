package com.mpazpro3.app.ui.screen.docente

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.domain.ExportadorResultados
import com.mpazpro3.app.domain.ReglasProgreso
import com.mpazpro3.app.model.Estudiante
import com.mpazpro3.app.model.Resultado
import com.mpazpro3.app.navigation.Rutas
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.ui.components.MensajeResultado
import com.mpazpro3.app.ui.components.PantallaCentrada
import com.mpazpro3.app.viewmodel.MpazProViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * RF-06: avance general de cada estudiante del curso y descarga de los resultados en CSV.
 *
 * Recursos nativos: selector de archivos del sistema (SAF, CreateDocument) para guardar el CSV
 * donde el docente elija, y hoja para compartir (ACTION_SEND). Ninguno pide permisos.
 */
@Composable
fun ResultadosCursoScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val usuario by viewModel.usuarioActivo.collectAsState()

    // Al abrir la pantalla se pide al servidor la información más reciente (API -> Room -> pantalla).
    LaunchedEffect(Unit) { viewModel.actualizar() }
    val estudiantes by viewModel.estudiantes.collectAsState()
    val contenidos by viewModel.contenidos.collectAsState()
    val asignaciones by viewModel.asignaciones.collectAsState()
    val actividades by viewModel.actividades.collectAsState()
    val seguimientos by viewModel.seguimientos.collectAsState()
    val curso = usuario?.curso

    val delCurso = remember(estudiantes, curso) { curso?.let { viewModel.estudiantesDelCurso(it) }.orEmpty() }
    val avances = remember(delCurso, contenidos, asignaciones, actividades, seguimientos) {
        delCurso.associate { it.id to viewModel.progresoGeneral(it.id) }
    }

    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    var resultado by remember { mutableStateOf<Resultado?>(null) }
    var csvPendiente by remember { mutableStateOf<String?>(null) }

    val guardarArchivo = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        val csv = csvPendiente
        csvPendiente = null
        if (uri != null && csv != null) {
            alcance.launch {
                resultado = withContext(Dispatchers.IO) { escribirCsv(contexto, uri, csv) }
            }
        }
    }

    Scaffold(topBar = { BarraSuperior("Resultados del curso", navController) }) { padding ->
        PantallaCentrada(padding) {
            if (curso == null || delCurso.isEmpty()) {
                Text("Sin estudiantes", style = MaterialTheme.typography.bodyLarge)
                return@PantallaCentrada
            }
            Text("Curso: $curso", style = MaterialTheme.typography.titleMedium)

            delCurso.forEach { estudiante ->
                FilaEstudiante(
                    estudiante = estudiante,
                    avance = avances[estudiante.id] ?: 0f,
                    onVerInforme = {
                        viewModel.seleccionarEstudiante(estudiante.id)
                        navController.navigate(Rutas.DOCENTE_INFORME)
                    }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    onClick = {
                        alcance.launch {
                            val csv = viewModel.descargarResultados(curso)
                            if (csv == null) {
                                resultado = Resultado.error("No hay resultados para exportar.")
                            } else {
                                csvPendiente = csv
                                guardarArchivo.launch(ExportadorResultados.nombreArchivo(curso))
                            }
                        }
                    }
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null)
                    Text("  Guardar CSV")
                }
                OutlinedButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    onClick = {
                        alcance.launch {
                            val csv = viewModel.descargarResultados(curso)
                            if (csv == null) {
                                resultado = Resultado.error("No hay resultados para exportar.")
                            } else {
                                compartirTexto(contexto, "Resultados del curso $curso", csv)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Text("  Compartir")
                }
            }
            MensajeResultado(resultado)
        }
    }
}

@Composable
private fun FilaEstudiante(estudiante: Estudiante, avance: Float, onVerInforme: () -> Unit) {
    val animado by animateFloatAsState(targetValue = avance, animationSpec = tween(700), label = "avance")
    val porcentaje = ReglasProgreso.porcentaje(avance)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(estudiante.nombreFicticio, style = MaterialTheme.typography.titleMedium)
            LinearProgressIndicator(
                progress = { animado },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .semantics { contentDescription = "Avance general $porcentaje por ciento" }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Avance general: $porcentaje %", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onVerInforme) { Text("Ver informe") }
            }
        }
    }
}

private fun escribirCsv(contexto: Context, uri: Uri, csv: String): Resultado = try {
    val flujo = contexto.contentResolver.openOutputStream(uri)
    if (flujo == null) {
        Resultado.error("No se pudo abrir el archivo elegido.")
    } else {
        flujo.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
        Resultado.ok("Archivo guardado correctamente.")
    }
} catch (e: java.io.IOException) {
    Resultado.error("No se pudo guardar el archivo.")
}

private fun compartirTexto(contexto: Context, asunto: String, texto: String) {
    val envio = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, asunto)
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    contexto.startActivity(Intent.createChooser(envio, "Compartir resultados"))
}
