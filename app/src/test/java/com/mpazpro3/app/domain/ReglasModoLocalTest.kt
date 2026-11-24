package com.mpazpro3.app.domain

import com.mpazpro3.app.model.EstadoAvance
import org.junit.Assert.assertEquals
import org.junit.Test

/** Reglas pequeñas que el modo local reutiliza (estado al empezar a trabajar y nombre del informe). */
class ReglasModoLocalTest {

    @Test
    fun alTrabajarPorPrimeraVez_pasaDeNoIniciadoAEnProgreso() {
        assertEquals(EstadoAvance.EN_PROGRESO, ReglasProgreso.estadoAlTrabajar(EstadoAvance.NO_INICIADO))
    }

    @Test
    fun alTrabajarDeNuevo_losOtrosEstadosNoCambian() {
        assertEquals(EstadoAvance.EN_PROGRESO, ReglasProgreso.estadoAlTrabajar(EstadoAvance.EN_PROGRESO))
        assertEquals(EstadoAvance.FINALIZADO, ReglasProgreso.estadoAlTrabajar(EstadoAvance.FINALIZADO))
    }

    @Test
    fun elInformeTieneUnIdentificadorEstablePorEstudiante() {
        assertEquals("INF-EST-002", GeneradorInforme.idInforme("EST-002"))
    }
}
