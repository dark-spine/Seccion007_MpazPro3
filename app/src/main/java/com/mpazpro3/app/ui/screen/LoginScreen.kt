package com.mpazpro3.app.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import com.mpazpro3.app.R
import com.mpazpro3.app.model.Perfil
import com.mpazpro3.app.ui.components.ServidorDialog
import com.mpazpro3.app.viewmodel.MpazProViewModel
import kotlinx.coroutines.launch

/**
 * RF-00: inicio de sesión por perfil. Según el perfil del usuario, AppNavigation lo lleva a la
 * pantalla principal de UTP, Docente o Estudiante.
 *
 * La pantalla solo dibuja y llama a viewModel.login(): las reglas (campos obligatorios, mensaje
 * genérico si las credenciales no coinciden) viven en ReglasAcceso. Cada campo muestra su propio
 * error (isError + supportingText + ícono) y la clave está oculta por defecto (RNF-08).
 */
@Composable
fun LoginScreen(
    viewModel: MpazProViewModel,
    onIngresar: (Perfil) -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var claveVisible by remember { mutableStateOf(false) }
    var ingresando by remember { mutableStateOf(false) }
    var mostrarServidor by remember { mutableStateOf(false) }
    val errores by viewModel.erroresLogin.collectAsState()
    val urlServidor by viewModel.urlServidor.collectAsState()
    val modoLocal by viewModel.modoLocal.collectAsState()

    var intentoFallido by remember { mutableIntStateOf(0) }
    val desplazamiento = remember { Animatable(0f) }
    LaunchedEffect(intentoFallido) {
        if (intentoFallido > 0) {
            for (x in listOf(-8f, 8f, -5f, 5f, 0f)) {
                desplazamiento.animateTo(x, tween(65))
            }
        }
    }
    val alcance = rememberCoroutineScope()
    val foco = LocalFocusManager.current

    fun intentarIngresar() {
        if (ingresando) return
        foco.clearFocus()
        alcance.launch {
            ingresando = true
            val exito = viewModel.login(usuario.trim(), clave)
            ingresando = false
            if (exito) viewModel.usuarioActivo.value?.perfil?.let(onIngresar)
            else intentoFallido++
        }
    }

    // Un error general (credenciales incorrectas) marca los dos campos en rojo.
    val errorUsuario = errores.usuario
    val errorClave = errores.clave ?: errores.general

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_escuela),
                contentDescription = "Logo Escuela Escritora Marcela Paz",
                modifier = Modifier.size(110.dp)
            )
            Text("MPAZ PRO 3°", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Escuela Escritora Marcela Paz",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .offset(x = desplazamiento.value.dp)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = usuario,
                    onValueChange = {
                        usuario = it
                        viewModel.limpiarErroresLogin()
                    },
                    label = { Text("Usuario o correo") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    trailingIcon = {
                        if (errorUsuario != null || errores.general != null) {
                            Icon(
                                Icons.Filled.Error,
                                contentDescription = "Dato con error",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    isError = errorUsuario != null || errores.general != null,
                    supportingText = if (errorUsuario != null) {
                        { Text(errorUsuario) }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { foco.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = clave,
                    onValueChange = {
                        clave = it
                        viewModel.limpiarErroresLogin()
                    },
                    label = { Text("Clave") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { claveVisible = !claveVisible }) {
                            Icon(
                                imageVector = if (claveVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (claveVisible) "Ocultar clave" else "Mostrar clave"
                            )
                        }
                    },
                    isError = errorClave != null,
                    supportingText = if (errorClave != null) {
                        { Text(errorClave) }
                    } else {
                        null
                    },
                    visualTransformation = if (claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { intentarIngresar() }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !ingresando,
                    onClick = { intentarIngresar() }
                ) {
                    if (ingresando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Ingresar", style = MaterialTheme.typography.titleMedium)
                    }
                }

                // Experiencia 3: aquí se cambia entre modo local (sin servidor) y modo servidor, y se edita la dirección.
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !ingresando,
                    onClick = { mostrarServidor = true }
                ) {
                    Icon(Icons.Filled.Dns, contentDescription = null)
                    Text(
                        if (modoLocal) "  Modo local (sin servidor) · cambiar" else "  Servidor: $urlServidor",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    if (mostrarServidor) {
        ServidorDialog(viewModel = viewModel, onCerrar = { mostrarServidor = false })
    }
}
