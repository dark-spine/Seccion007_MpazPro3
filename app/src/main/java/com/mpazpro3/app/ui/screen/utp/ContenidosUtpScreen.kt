package com.mpazpro3.app.ui.screen.utp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.navigation.Rutas
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.ui.components.SelectorDesplegable
import com.mpazpro3.app.viewmodel.MpazProViewModel

/**
 * Módulo "Autorización de contenidos (UTP)" — pantalla principal de Jefatura UTP: lista de
 * contenidos curriculares con filtros por asignatura y por estado. Tocar un contenido abre su
 * detalle para aprobarlo o rechazarlo.
 *
 * TODO(equipo): viewModel.contenidosFiltrados() todavía no filtra.
 */
@Composable
fun ContenidosUtpScreen(
    navController: NavHostController,
    viewModel: MpazProViewModel,
    onCerrarSesion: () -> Unit
) {
    val todos by viewModel.contenidos.collectAsState()
    var asignatura by remember { mutableStateOf<Asignatura?>(null) }
    var estado by remember { mutableStateOf<EstadoAprobacion?>(null) }
    val lista = remember(todos, asignatura, estado) { viewModel.contenidosFiltrados(asignatura, estado) }

    Scaffold(topBar = { BarraSuperior("Autorización de contenidos", navController, onCerrarSesion) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            SelectorDesplegable<Asignatura?>(
                etiqueta = "Asignatura",
                opciones = listOf<Asignatura?>(null) + Asignatura.entries,
                seleccion = asignatura,
                textoDe = { it?.etiqueta ?: "Todas las asignaturas" },
                onSeleccion = { asignatura = it },
                textoVacio = "Todas las asignaturas",
                modifier = Modifier.padding(top = 8.dp)
            )
            SelectorDesplegable<EstadoAprobacion?>(
                etiqueta = "Estado",
                opciones = listOf<EstadoAprobacion?>(null) + EstadoAprobacion.entries,
                seleccion = estado,
                textoDe = { it?.etiqueta ?: "Todos los estados" },
                onSeleccion = { estado = it },
                textoVacio = "Todos los estados"
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lista) { contenido ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.seleccionarContenido(contenido.id)
                                navController.navigate(Rutas.UTP_DETALLE)
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(contenido.asignatura.etiqueta, style = MaterialTheme.typography.labelMedium)
                            Text(contenido.unidad, style = MaterialTheme.typography.titleMedium)
                            Text(contenido.objetivoAprendizaje, style = MaterialTheme.typography.bodySmall)
                            Text("Estado: ${contenido.estado.etiqueta}", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
