package com.mpazpro3.app.domain

import com.mpazpro3.app.domain.ReglasAcceso.Accion
import com.mpazpro3.app.model.Perfil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasAccesoTest {

    @Test fun formatosDeUsuario() {
        listOf("est001", "utp@mpazpro3.cl", "docente.3basicoA@mpazpro3.cl", " EST001 ").forEach {
            assertFalse(ReglasAcceso.validarCampos(it, "1234").hayErrores)
        }
        listOf("ab", "a b", "correo@", "correo@dominio", "a/b").forEach {
            assertEquals(ReglasAcceso.MENSAJE_FORMATO_USUARIO, ReglasAcceso.validarCampos(it, "1234").usuario)
        }
    }

    @Test
    fun hashClave_esDeterministaYNoGuardaTextoPlano() {
        val hash = ReglasAcceso.hashClave("docente123")
        assertEquals(hash, ReglasAcceso.hashClave("docente123"))
        assertEquals(64, hash.length)
        assertFalse(hash.contains("docente123"))
    }

    @Test
    fun hashClave_clavesDistintasDanHashDistinto() {
        assertNotEquals(ReglasAcceso.hashClave("1234"), ReglasAcceso.hashClave("12345"))
    }

    @Test
    fun validarCampos_vaciosTienenMensajePorCampo() {
        val errores = ReglasAcceso.validarCampos("   ", "")
        assertEquals(ReglasAcceso.MENSAJE_USUARIO_VACIO, errores.usuario)
        assertEquals(ReglasAcceso.MENSAJE_CLAVE_VACIA, errores.clave)
        assertTrue(errores.hayErrores)
    }

    @Test
    fun validarCampos_soloFallaElCampoVacio() {
        val errores = ReglasAcceso.validarCampos("est001", "")
        assertNull(errores.usuario)
        assertEquals(ReglasAcceso.MENSAJE_CLAVE_VACIA, errores.clave)
    }

    @Test
    fun validarCampos_completosNoTienenErrores() {
        assertFalse(ReglasAcceso.validarCampos("est001", "1234").hayErrores)
    }

    @Test
    fun normalizarUsuario_ignoraMayusculasYEspacios() {
        assertEquals("utp@mpazpro3.cl", ReglasAcceso.normalizarUsuario("  UTP@MpazPro3.cl "))
    }

    @Test
    fun matrizDePermisos_cadaPerfilSoloHaceLoSuyo() {
        assertTrue(ReglasAcceso.puede(Perfil.UTP, Accion.REVISAR_CONTENIDOS))
        assertFalse(ReglasAcceso.puede(Perfil.DOCENTE, Accion.REVISAR_CONTENIDOS))
        assertFalse(ReglasAcceso.puede(Perfil.ESTUDIANTE, Accion.REVISAR_CONTENIDOS))

        assertTrue(ReglasAcceso.puede(Perfil.DOCENTE, Accion.PLANIFICAR_CURSO))
        assertTrue(ReglasAcceso.puede(Perfil.DOCENTE, Accion.VER_RESULTADOS))
        assertTrue(ReglasAcceso.puede(Perfil.DOCENTE, Accion.GESTIONAR_INFORMES))
        assertFalse(ReglasAcceso.puede(Perfil.UTP, Accion.PLANIFICAR_CURSO))
        assertFalse(ReglasAcceso.puede(Perfil.UTP, Accion.VER_RESULTADOS))
        assertFalse(ReglasAcceso.puede(Perfil.UTP, Accion.GESTIONAR_INFORMES))

        assertTrue(ReglasAcceso.puede(Perfil.ESTUDIANTE, Accion.TRABAJAR_COMO_ESTUDIANTE))
        assertFalse(ReglasAcceso.puede(Perfil.DOCENTE, Accion.TRABAJAR_COMO_ESTUDIANTE))
    }

    @Test
    fun estudianteNuncaAccedeAlInformeNiASusResultadosDeCurso() {
        assertFalse(ReglasAcceso.puede(Perfil.ESTUDIANTE, Accion.GESTIONAR_INFORMES))
        assertFalse(ReglasAcceso.puede(Perfil.ESTUDIANTE, Accion.VER_RESULTADOS))
    }

    @Test
    fun sinSesionNoSePermiteNada() {
        Accion.entries.forEach { assertFalse(ReglasAcceso.puede(null, it)) }
    }
}
