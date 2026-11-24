package com.mpazpro3.app.ui.screen.utp

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.navigation.Rutas
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.ui.components.PantallaCentrada
import com.mpazpro3.app.ui.components.SelectorDesplegable
import com.mpazpro3.app.viewmodel.MpazProViewModel

/**
 * RF-02: pantalla principal de Jefatura UTP. Lista los contenidos curriculares con filtros por
 * asignatura y por estado (los pendientes salen primero). Tocar un contenido abre su detalle para
 * aprobarlo o rechazarlo (RF-01).
 */
@Composable
fun ContenidosUtpScreen(
    navController: NavHostController,
    viewModel: MpazProViewModel,
    onCerrarSesion: () -> Unit
) {
    val todos by viewModel.contenidos.collectAsState()

    // Al abrir la pantalla se pide al servidor la información más reciente (API -> Room -> pantalla).
    LaunchedEffect(Unit) { viewModel.actualizar() }
    var asignatura by remember { mutableStateOf<Asignatura?>(null) }
    var estado by remember { mutableStateOf<EstadoAprobacion?>(null) }
    val lista = remember(todos, asignatura, estado) { viewModel.contenidosFiltrados(asignatura, estado) }

    val pendientes = todos.count { it.estado == EstadoAprobacion.PENDIENTE }
    val aprobados = todos.count { it.estado == EstadoAprobacion.APROBADO }
    val rechazados = todos.count { it.estado == EstadoAprobacion.RECHAZADO }

    Scaffold(topBar = { BarraSuperior("Autorización de contenidos", navController, onCerrarSesion) }) { padding ->
        PantallaCentrada(padding, desplazable = false, espacio = 8.dp) {
            Text(
                "Pendientes: $pendientes · Aprobados: $aprobados · Rechazados: $rechazados",
                style = MaterialTheme.typography.titleSmall
            )
            SelectorDesplegable<Asignatura?>(
                etiqueta = "Asignatura",
                opciones = listOf<Asignatura?>(null) + Asignatura.entries,
                seleccion = asignatura,
                textoDe = { it?.etiqueta ?: "Todas las asignaturas" },
                onSeleccion = { asignatura = it },
                textoVacio = "Todas las asignaturas"
            )
            SelectorDesplegable<EstadoAprobacion?>(
                etiqueta = "Estado",
                opciones = listOf<EstadoAprobacion?>(null) + EstadoAprobacion.entries,
                seleccion = estado,
                textoDe = { it?.etiqueta ?: "Todos los estados" },
                onSeleccion = { estado = it },
                textoVacio = "Todos los estados"
            )

            if (lista.isEmpty()) {
                Text("No hay contenidos con esos filtros", style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = {
                    asignatura = null
                    estado = null
                }) { Text("Quitar filtros") }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lista, key = { it.id }) { contenido ->
                    val (icono, color) = presentacionDeEstado(contenido.estado)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .clickable {
                                viewModel.seleccionarContenido(contenido.id)
                                navController.navigate(Rutas.UTP_DETALLE)
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(contenido.asignatura.etiqueta, style = MaterialTheme.typography.labelLarge)
                            Text(contenido.unidad, style = MaterialTheme.typography.titleMedium)
                            Text(contenido.objetivoAprendizaje, style = MaterialTheme.typography.bodyMedium)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                                Text(
                                    "Estado: ${contenido.estado.etiqueta}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Ícono y color con que se muestra cada estado de aprobación (el estado nunca depende solo del color). */
@Composable
internal fun presentacionDeEstado(estado: EstadoAprobacion): Pair<ImageVector, Color> = when (estado) {
    EstadoAprobacion.APROBADO -> Icons.Filled.CheckCircle to MaterialTheme.colorScheme.secondary
    EstadoAprobacion.RECHAZADO -> Icons.Filled.Error to MaterialTheme.colorScheme.error
    EstadoAprobacion.PENDIENTE -> Icons.Filled.Info to MaterialTheme.colorScheme.onSurfaceVariant
}
