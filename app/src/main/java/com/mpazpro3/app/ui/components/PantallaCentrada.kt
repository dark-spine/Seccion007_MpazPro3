package com.mpazpro3.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Columna base de las pantallas: limita el ancho a 640 dp y la centra, para que en una tableta
 * el contenido no se estire de borde a borde (RNF-07). En un teléfono ocupa todo el ancho.
 *
 * @param padding relleno que entrega el Scaffold (barra superior y barras del sistema).
 * @param desplazable si es true la columna hace scroll vertical; usar false cuando contiene una
 *        LazyColumn (no se puede anidar un scroll vertical dentro de otro).
 */
@Composable
fun PantallaCentrada(
    padding: PaddingValues,
    modifier: Modifier = Modifier,
    desplazable: Boolean = true,
    espacio: Dp = 12.dp,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .padding(padding)
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val base = Modifier
            .widthIn(max = 640.dp)
            .fillMaxSize()
        Column(
            modifier = (if (desplazable) base.verticalScroll(rememberScrollState()) else base).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(espacio),
            content = contenido
        )
    }
}
