package com.mpazpro3.app.ui.components

import android.media.MediaPlayer
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Reproductor del audio de apoyo (RF-07 · recurso nativo del dispositivo).
 *
 * Usa android.media.MediaPlayer, que es parte del sistema operativo: no requiere
 * ninguna librería externa ni permiso en el manifest, lo que respeta el RNF-03
 * (sin cámara, GPS, micrófono ni biometría).
 *
 * RNF-02: el audio NO se reproduce solo al abrir la pantalla; el estudiante decide
 * cuándo escucharlo, y el texto escrito siempre está visible.
 *
 * Ciclo de vida: el reproductor se crea al entrar y se libera en onDispose al salir.
 * Sin ese release() el audio seguiría sonando fuera de la pantalla y quedaría
 * ocupando memoria.
 */
@Composable
fun ReproductorAudio(
    @RawRes audioResId: Int,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current

    var reproductor by remember { mutableStateOf<MediaPlayer?>(null) }
    var sonando by remember { mutableStateOf(false) }
    var duracionMs by remember { mutableIntStateOf(0) }
    var posicionMs by remember { mutableIntStateOf(0) }

    // Crea el reproductor al entrar y lo libera al salir de la pantalla.
    DisposableEffect(audioResId) {
        val mp = MediaPlayer.create(contexto, audioResId)
        mp?.setOnCompletionListener { player ->
            // Al terminar, vuelve al inicio y repone el botón en "reproducir".
            sonando = false
            posicionMs = 0
            player.seekTo(0)
        }
        reproductor = mp
        duracionMs = mp?.duration ?: 0

        onDispose {
            mp?.release()
            reproductor = null
            sonando = false
        }
    }

    // Mientras suena, actualiza la barra de progreso cada 200 ms.
    LaunchedEffect(sonando) {
        while (sonando) {
            posicionMs = reproductor?.currentPosition ?: 0
            delay(200)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Audio de apoyo",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledIconButton(
                    modifier = Modifier.size(56.dp), // RNF-01: botón grande y visible
                    onClick = {
                        val mp = reproductor ?: return@FilledIconButton
                        if (sonando) {
                            mp.pause()
                            sonando = false
                        } else {
                            mp.start()
                            sonando = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (sonando) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (sonando) "Pausar el audio" else "Escuchar el audio"
                    )
                }

                Slider(
                    modifier = Modifier.weight(1f),
                    value = posicionMs.toFloat(),
                    valueRange = 0f..(if (duracionMs > 0) duracionMs.toFloat() else 1f),
                    onValueChange = { nueva ->
                        posicionMs = nueva.toInt()
                        reproductor?.seekTo(posicionMs)
                    }
                )
            }

            Text(
                text = "${aMinutosSegundos(posicionMs)} / ${aMinutosSegundos(duracionMs)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/** Convierte milisegundos a un texto m:ss. */
private fun aMinutosSegundos(milisegundos: Int): String {
    val totalSegundos = milisegundos / 1000
    val minutos = totalSegundos / 60
    val segundos = totalSegundos % 60
    return "$minutos:${segundos.toString().padStart(2, '0')}"
}