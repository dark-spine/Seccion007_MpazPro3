package com.mpazpro3.app.data.remote

import org.junit.Ignore
import org.junit.Test

/**
 * TODO(E3-T3) Pruebas de los mapeadores DTO <-> modelo. Se completan después de E3-4.
 *
 * Casos que debe cubrir (usa los datos de ejemplo de DatosIniciales para armar los objetos):
 *  1. Contenido: contenido.aDto().aDominio() == contenido, con codigoOA nulo y con valor.
 *  2. Actividad: conserva alternativas (texto y esCorrecta), dificultad, orden y esEvaluacionFinal.
 *  3. Seguimiento: conserva material revisado, intentos usados, estado y la lista de respuestas.
 *  4. Informe: conserva fortalezas, aspectos a reforzar y estado (BORRADOR / VALIDADO).
 *  5. Usuario: UsuarioDto("U1", ..., perfil = "DOCENTE").aDominio("hash") -> Perfil.DOCENTE con el hash recibido.
 *  6. Un valor desconocido (asignatura = "ARTE", estado = "ARCHIVADO") lanza IllegalArgumentException, y
 *     ApiResult.Exito(dto).transformar { it.aDominio() } la convierte en Error(0, ErroresApi.MSG_RESPUESTA_INVALIDA).
 */
@Ignore("E3: completar")
class MapeadoresTest {

    @Test
    fun contenido_idaYVuelta() {
        // TODO(E3-T3)
    }

    @Test
    fun actividad_conservaLasAlternativas() {
        // TODO(E3-T3)
    }

    @Test
    fun seguimiento_conservaLasRespuestas() {
        // TODO(E3-T3)
    }

    @Test
    fun informe_conservaListasYEstado() {
        // TODO(E3-T3)
    }

    @Test
    fun usuario_usaElHashRecibidoYElPerfil() {
        // TODO(E3-T3)
    }

    @Test
    fun valorDesconocido_esRespuestaInvalida() {
        // TODO(E3-T3)
    }
}
