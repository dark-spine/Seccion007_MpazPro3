package com.mpazpro3.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mpazpro3.app.model.Perfil
import com.mpazpro3.app.ui.components.AvisoConexion
import com.mpazpro3.app.ui.components.FranjaModo
import com.mpazpro3.app.ui.screen.LoginScreen
import com.mpazpro3.app.ui.screen.docente.InformeDocenteScreen
import com.mpazpro3.app.ui.screen.docente.PanelDocenteScreen
import com.mpazpro3.app.ui.screen.docente.PlanificacionDocenteScreen
import com.mpazpro3.app.ui.screen.docente.ResultadosCursoScreen
import com.mpazpro3.app.ui.screen.estudiante.DesafiosScreen
import com.mpazpro3.app.ui.screen.estudiante.EvaluacionFinalScreen
import com.mpazpro3.app.ui.screen.estudiante.InicioEstudianteScreen
import com.mpazpro3.app.ui.screen.estudiante.MaterialScreen
import com.mpazpro3.app.ui.screen.estudiante.RevisionRespuestasScreen
import com.mpazpro3.app.ui.screen.utp.ContenidosUtpScreen
import com.mpazpro3.app.ui.screen.utp.DetalleContenidoUtpScreen
import com.mpazpro3.app.viewmodel.MpazProViewModel

/** Rutas de la aplicación (un solo grafo de navegación). */
object Rutas {
    const val LOGIN = "login"

    const val UTP_HOME = "utp/contenidos"
    const val UTP_DETALLE = "utp/detalle"

    const val DOCENTE_HOME = "docente/panel"
    const val DOCENTE_PLANIFICACION = "docente/planificacion"
    const val DOCENTE_RESULTADOS = "docente/resultados"
    const val DOCENTE_INFORME = "docente/informe"

    const val EST_HOME = "estudiante/inicio"
    const val EST_MATERIAL = "estudiante/material"
    const val EST_DESAFIOS = "estudiante/desafios"
    const val EST_EVALUACION = "estudiante/evaluacion"
    const val EST_REVISION = "estudiante/revision"

    /** Pantalla principal de cada perfil. */
    fun inicioDe(perfil: Perfil): String = when (perfil) {
        Perfil.UTP -> UTP_HOME
        Perfil.DOCENTE -> DOCENTE_HOME
        Perfil.ESTUDIANTE -> EST_HOME
    }
}

/**
 * Navegación única de la app. El ViewModel es uno solo y se comparte entre todas las pantallas.
 *
 * Control de acceso por ruta (RN-01): cada pantalla protegida verifica que haya una sesión y que
 * el perfil sea el que corresponde; si no, vuelve al login. Además el ViewModel valida cada
 * acción por su cuenta.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: MpazProViewModel = viewModel()

    val cerrarSesion: () -> Unit = {
        navController.navigate(Rutas.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
        viewModel.logout()
    }

    // La franja de modo (local / servidor) va fija arriba y ocupa la zona de la barra de estado; las pantallas
    // quedan debajo y no vuelven a reservar ese espacio. El aviso de conexión flota sobre cualquier pantalla.
    Column(modifier = Modifier.fillMaxSize()) {
        FranjaModo(viewModel)
        Box(modifier = Modifier.weight(1f).fillMaxWidth().consumeWindowInsets(WindowInsets.statusBars)) {
            NavHost(navController = navController, startDestination = Rutas.LOGIN, modifier = Modifier.fillMaxSize()) {
                composable(Rutas.LOGIN) {
                    LoginScreen(viewModel = viewModel, onIngresar = { perfil ->
                        navController.navigate(Rutas.inicioDe(perfil)) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                    })
                }

                composable(Rutas.UTP_HOME) {
                    SoloPerfil(Perfil.UTP, navController, viewModel) {
                        ContenidosUtpScreen(navController, viewModel, cerrarSesion)
                    }
                }
                composable(Rutas.UTP_DETALLE) {
                    SoloPerfil(Perfil.UTP, navController, viewModel) {
                        DetalleContenidoUtpScreen(navController, viewModel)
                    }
                }

                composable(Rutas.DOCENTE_HOME) {
                    SoloPerfil(Perfil.DOCENTE, navController, viewModel) {
                        PanelDocenteScreen(navController, viewModel, cerrarSesion)
                    }
                }
                composable(Rutas.DOCENTE_PLANIFICACION) {
                    SoloPerfil(Perfil.DOCENTE, navController, viewModel) {
                        PlanificacionDocenteScreen(navController, viewModel)
                    }
                }
                composable(Rutas.DOCENTE_RESULTADOS) {
                    SoloPerfil(Perfil.DOCENTE, navController, viewModel) {
                        ResultadosCursoScreen(navController, viewModel)
                    }
                }
                composable(Rutas.DOCENTE_INFORME) {
                    SoloPerfil(Perfil.DOCENTE, navController, viewModel) {
                        InformeDocenteScreen(navController, viewModel)
                    }
                }

                composable(Rutas.EST_HOME) {
                    SoloPerfil(Perfil.ESTUDIANTE, navController, viewModel) {
                        InicioEstudianteScreen(navController, viewModel, cerrarSesion)
                    }
                }
                composable(Rutas.EST_MATERIAL) {
                    SoloPerfil(Perfil.ESTUDIANTE, navController, viewModel) {
                        MaterialScreen(navController, viewModel)
                    }
                }
                composable(Rutas.EST_DESAFIOS) {
                    SoloPerfil(Perfil.ESTUDIANTE, navController, viewModel) {
                        DesafiosScreen(navController, viewModel)
                    }
                }
                composable(Rutas.EST_EVALUACION) {
                    SoloPerfil(Perfil.ESTUDIANTE, navController, viewModel) {
                        EvaluacionFinalScreen(navController, viewModel)
                    }
                }
                composable(Rutas.EST_REVISION) {
                    SoloPerfil(Perfil.ESTUDIANTE, navController, viewModel) {
                        RevisionRespuestasScreen(navController, viewModel)
                    }
                }
            }
            AvisoConexion(
                viewModel = viewModel,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .widthIn(max = 640.dp)
            )
        }
    }
}

/** Muestra el contenido solo si la sesión activa tiene el perfil indicado; si no, vuelve al login. */
@Composable
private fun SoloPerfil(
    perfil: Perfil,
    navController: NavHostController,
    viewModel: MpazProViewModel,
    contenido: @Composable () -> Unit
) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val autorizado = usuario?.perfil == perfil

    if (autorizado) {
        contenido()
    } else {
        LaunchedEffect(usuario) {
            if (navController.currentDestination?.route != Rutas.LOGIN) {
                navController.navigate(Rutas.LOGIN) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        }
    }
}
