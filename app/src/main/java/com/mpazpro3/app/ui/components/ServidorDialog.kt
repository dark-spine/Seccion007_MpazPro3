package com.mpazpro3.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mpazpro3.app.model.Resultado
import com.mpazpro3.app.viewmodel.MpazProViewModel
import kotlinx.coroutines.launch

/**
 * Diálogo "Servidor" (se abre desde el login). Tiene dos partes:
 *  - el interruptor "Modo local (sin servidor)": con él encendido la app usa solo la base del dispositivo
 *    (respaldo para demostrar sin acceso al servicio); apagado, usa el servidor. El cambio es inmediato;
 *  - la dirección de la API, que se puede cambiar sin recompilar (por ejemplo a la URL de VS Code Port
 *    Forwarding el día de la presentación): se valida mientras se escribe, se prueba contra GET /health
 *    y queda guardada en el dispositivo. Si "Probar conexión" falla se ofrece "Usar modo local".
 */
@Composable
fun ServidorDialog(viewModel: MpazProViewModel, onCerrar: () -> Unit) {
    val guardada by viewModel.urlServidor.collectAsState()
    val modoLocal by viewModel.modoLocal.collectAsState()
    var texto by remember { mutableStateOf(guardada) }
    var resultado by remember { mutableStateOf<Resultado?>(null) }
    var probando by remember { mutableStateOf(false) }
    var fallaLaPrueba by remember { mutableStateOf(false) }
    var confirmarReinicio by remember { mutableStateOf(false) }
    val alcance = rememberCoroutineScope()

    val error = remember(texto) { viewModel.errorDeUrlServidor(texto) }

    fun cambiarModo(local: Boolean) {
        viewModel.cambiarModoLocal(local)
        fallaLaPrueba = false
        confirmarReinicio = false
        resultado = Resultado.ok(
            if (local) {
                "Modo local activado: la app trabaja solo con los datos del dispositivo."
            } else {
                "Modo servidor activado: la app usa el servidor de la dirección de abajo."
            }
        )
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Servidor") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(value = modoLocal, role = Role.Switch, onValueChange = { cambiarModo(it) }),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Modo local (sin servidor)", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Usa solo la base de datos del dispositivo, con las mismas reglas de negocio. Es un " +
                                "respaldo para demostrar sin acceso al servicio. Los datos locales y los del " +
                                "servidor son independientes: lo que crees en modo local no se envía al servidor.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(checked = modoLocal, onCheckedChange = null)
                }

                if (modoLocal) {
                    TextButton(
                        onClick = {
                            if (!confirmarReinicio) {
                                confirmarReinicio = true
                            } else {
                                alcance.launch {
                                    resultado = viewModel.restablecerDatosLocales()
                                    confirmarReinicio = false
                                }
                            }
                        }
                    ) {
                        Text(if (confirmarReinicio) "Confirmar: borrar lo creado en modo local" else "Restablecer datos locales")
                    }
                    if (confirmarReinicio) {
                        Text(
                            "Se borra lo creado en modo local y vuelven los datos de ejemplo. No toca los datos del servidor.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    "Dirección de la API. En el emulador: http://10.0.2.2:3000/. " +
                        "Con VS Code Port Forwarding, pega la URL https pública.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = texto,
                    onValueChange = {
                        texto = it
                        resultado = null
                        fallaLaPrueba = false
                    },
                    label = { Text("URL del servidor") },
                    isError = error != null,
                    supportingText = { Text(error ?: "Ejemplo: http://192.168.1.20:3000/") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        enabled = error == null && !probando,
                        onClick = {
                            alcance.launch {
                                probando = true
                                val r = viewModel.probarConexion(texto)
                                resultado = r
                                fallaLaPrueba = !r.exito
                                probando = false
                            }
                        }
                    ) {
                        if (probando) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Probar conexión")
                        }
                    }
                    TextButton(onClick = {
                        texto = viewModel.restablecerUrlServidor()
                        fallaLaPrueba = false
                        resultado = Resultado.ok("Servidor restablecido a la dirección inicial.")
                    }) { Text("Restablecer URL") }
                }
                MensajeResultado(resultado)
                if (fallaLaPrueba && !modoLocal) {
                    OutlinedButton(onClick = { cambiarModo(true) }) { Text("Usar modo local") }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = error == null,
                onClick = {
                    val r = viewModel.guardarUrlServidor(texto)
                    if (r.exito) onCerrar() else resultado = r
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } }
    )
}
