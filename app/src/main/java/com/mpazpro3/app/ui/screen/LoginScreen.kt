package com.mpazpro3.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
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
 * La pantalla está completa, pero viewModel.login() es TODO(equipo): por ahora siempre
 * rechaza. Mientras tanto, los botones "Modo desarrollo" permiten entrar con el usuario de
 * prueba de cada perfil. TODO(equipo): eliminarlos antes de la entrega final.
 *
 * Usuarios de prueba (MockData.usuarios):
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
    val error by viewModel.errorLogin.collectAsState()

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

            OutlinedTextField(
                value = usuario,
                onValueChange = { usuario = it },
                label = { Text("Usuario o correo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = clave,
                onValueChange = { clave = it },
                label = { Text("Clave") },
                singleLine = true,
                visualTransformation = if (claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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

            if (error != null) {
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (viewModel.login(usuario.trim(), clave)) {
                        viewModel.usuarioActivo.value?.perfil?.let(onIngresar)
                    }
                }
            ) { Text("Ingresar") }

            // ---- SOLO PARA DESARROLLO ---- TODO(equipo): eliminar antes de entregar.
            Text(
                "Modo desarrollo (entrar sin login):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Perfil.entries.forEach { perfil ->
                TextButton(onClick = {
                    viewModel.entrarModoDesarrollo(perfil)
                    onIngresar(perfil)
                }) { Text("Entrar como ${perfil.etiqueta}") }
            }
        }
    }
}
