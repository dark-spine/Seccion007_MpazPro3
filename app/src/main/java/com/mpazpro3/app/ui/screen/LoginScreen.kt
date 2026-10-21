package com.mpazpro3.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mpazpro3.app.R
import com.mpazpro3.app.model.Perfil
import com.mpazpro3.app.viewmodel.MpazProViewModel

/**
 * Inicio de sesión (primera pantalla). Según el perfil del usuario, AppNavigation lo lleva a
 * la pantalla principal de UTP, Docente o Estudiante.
 *
 * RF-00: viewModel.login() valida las credenciales contra la tabla usuarios de Room.
 *
 * Validaciones: la decisión de qué campo está mal se toma en el ViewModel; esta pantalla
 * solo dibuja el resultado (campo en rojo, ícono de advertencia y mensaje bajo el campo).
 * Así la lógica queda desacoplada de la interfaz.
 *
 * RNF-08: la clave se muestra oculta por defecto y no existe ninguna forma de entrar sin
 * credenciales (el modo desarrollo fue eliminado antes de la entrega).
 *
 * Usuarios de prueba (datos ficticios, RNF-04):
 *   utp@mpazpro3.cl / utp123
 *   docente.3basicoA@mpazpro3.cl / docente123
 *   est001 / 1234
 *
 * El logo es PROVISIONAL (res/drawable/logo_escuela.xml) — ver README.md.
 */
@Composable
fun LoginScreen(
    viewModel: MpazProViewModel,
    onIngresar: (Perfil) -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var claveVisible by remember { mutableStateOf(false) }

    val errorGeneral by viewModel.errorLogin.collectAsState()
    val errorUsuario by viewModel.errorUsuario.collectAsState()
    val errorClave by viewModel.errorClave.collectAsState()

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

            /*
             * Campo usuario.
             * isError pinta el borde y la etiqueta en rojo; el ícono de advertencia aparece
             * solo cuando hay error; supportingText deja el mensaje bajo este campo y no
             * en un aviso suelto al final del formulario.
             */
            OutlinedTextField(
                value = usuario,
                onValueChange = {
                    usuario = it
                    viewModel.limpiarErroresLogin()
                },
                label = { Text("Usuario o correo") },
                leadingIcon = {
                    Icon(Icons.Filled.Person, contentDescription = null)
                },
                trailingIcon = {
                    if (errorUsuario != null) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Campo con error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                isError = errorUsuario != null,
                supportingText = {
                    val mensaje = errorUsuario
                    if (mensaje != null && mensaje.isNotBlank()) {
                        Text(mensaje, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            /*
             * Campo clave. Conserva el botón de mostrar/ocultar como trailingIcon (RNF-08),
             * así que el error se señala con el borde rojo, el candado teñido y el mensaje.
             */
            OutlinedTextField(
                value = clave,
                onValueChange = {
                    clave = it
                    viewModel.limpiarErroresLogin()
                },
                label = { Text("Clave") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = if (errorClave != null) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                visualTransformation = if (claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = errorClave != null,
                supportingText = {
                    val mensaje = errorClave
                    if (mensaje != null && mensaje.isNotBlank()) {
                        Text(mensaje, color = MaterialTheme.colorScheme.error)
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { claveVisible = !claveVisible }) {
                        Icon(
                            imageVector = if (claveVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (claveVisible) "Ocultar clave" else "Mostrar clave"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            /*
             * Aviso general, solo para el caso de credenciales incorrectas. No dice cuál de
             * los dos datos falló, por seguridad.
             */
            if (errorGeneral != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            errorGeneral.orEmpty(),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = {
                    if (viewModel.login(usuario.trim(), clave)) {
                        viewModel.usuarioActivo.value?.perfil?.let(onIngresar)
                    }
                }
            ) { Text("Ingresar", style = MaterialTheme.typography.titleMedium) }
        }
    }
}