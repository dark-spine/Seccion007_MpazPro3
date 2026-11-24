package com.mpazpro3.app.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mpazpro3.app.navigation.AppNavigation
import com.mpazpro3.app.ui.theme.MpazPro3Theme
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TODO(E3-T6) Prueba de interfaz (Compose UI Test) del ingreso. Se completa en la Parte 4 de la guía y se ejecuta
 * en un emulador o dispositivo: en Android Studio, clic derecho sobre la clase > Run, o
 * `./gradlew connectedDebugAndroidTest`. Para activarla, quita el @Ignore de la clase.
 *
 * Pantalla bajo prueba: LoginScreen dentro de AppNavigation (campos "Usuario o correo" y "Clave", botón "Ingresar").
 *
 * Casos (3):
 *  1. Campos vacíos: tocar "Ingresar" sin escribir nada muestra "Ingresa tu usuario" y "Ingresa tu clave" y sigue en el login.
 *  2. Clave incorrecta: utp@mpazpro3.cl con "mala" muestra "Usuario o clave incorrectos" y NO navega.
 *  3. Ingreso correcto: est001 / 1234 llega a la pantalla "Mis actividades" (barra superior) y no aparece el botón "Ingresar".
 *
 * Pistas: composeRule.onNodeWithText("Usuario o correo").performTextInput("est001");
 * composeRule.onNodeWithText("Ingresar").performClick(); composeRule.waitUntil(5_000) { ... } para esperar al ViewModel;
 * composeRule.onNodeWithText("Mis actividades").assertIsDisplayed(). Si la app está en modo servidor (USAR_API = true o interruptor apagado), el servidor debe estar encendido.
 */
@Ignore("E3: completar")
@RunWith(AndroidJUnit4::class)
class LoginUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun abrirApp() {
        composeRule.setContent { MpazPro3Theme { AppNavigation() } }
    }

    @Test
    fun camposVacios_muestranLosErroresPorCampo() {
        abrirApp()
        // TODO(E3-T6): tocar "Ingresar" y comprobar los dos mensajes
    }

    @Test
    fun claveIncorrecta_muestraElErrorGeneral_ySigueEnElLogin() {
        abrirApp()
        // TODO(E3-T6): escribir utp@mpazpro3.cl y una clave mala; comprobar "Usuario o clave incorrectos"
    }

    @Test
    fun ingresoDeEstudiante_llegaAMisActividades() {
        abrirApp()
        // TODO(E3-T6): escribir est001 / 1234, tocar "Ingresar" y esperar a "Mis actividades"
    }
}
