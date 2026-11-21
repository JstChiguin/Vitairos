package com.example.vitairosapp.modelo

import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.ReportStatus
import com.example.vitairosapp.ui.viewmodel.AppViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EstadosReporteTest {

    @Test
    fun `solo borrador, en cola y error son editables`() {
        val editables = ReportStatus.entries.filter { it.isEditable }.toSet()
        assertEquals(setOf(ReportStatus.BORRADOR, ReportStatus.EN_COLA, ReportStatus.ERROR), editables)
    }

    @Test
    fun `solo borrador y en cola se pueden eliminar`() {
        val eliminables = ReportStatus.entries.filter { it.isDeletable }.toSet()
        assertEquals(setOf(ReportStatus.BORRADOR, ReportStatus.EN_COLA), eliminables)
    }

    @Test
    fun `se consideran pendientes en cola, enviando y error`() {
        val pendientes = ReportStatus.entries.filter { it.isPending }.toSet()
        assertEquals(
            setOf(ReportStatus.EN_COLA, ReportStatus.ENVIANDO, ReportStatus.ERROR),
            pendientes
        )
    }

    @Test
    fun `un reporte sincronizado no es editable, eliminable ni pendiente`() {
        val estado = ReportStatus.SINCRONIZADO
        assertFalse(estado.isEditable)
        assertFalse(estado.isDeletable)
        assertFalse(estado.isPending)
    }

    @Test
    fun `un reporte nuevo nace como borrador sin intentos y en version uno`() {
        val reporte = Report(id = "R1", user = "Usuario ficticio", installId = "INST-TEST")
        assertEquals(ReportStatus.BORRADOR, reporte.status)
        assertEquals(0, reporte.attempts)
        assertEquals(1, reporte.version)
        assertTrue(reporte.log.isEmpty())
    }

    @Test
    fun `withLog agrega el evento al final y conserva los anteriores`() {
        val reporte = Report(id = "R1", user = "Usuario ficticio", installId = "INST-TEST")
            .withLog("primero")
            .withLog("segundo")
        assertEquals(listOf("primero", "segundo"), reporte.log.map { it.message })
    }

    @Test
    fun `los identificadores generados tienen formato legible y son distintos`() {
        val primero = Report.newId()
        val segundo = Report.newId()
        assertTrue(Regex("VIT-[0-9A-Z]+-[0-9A-F]{4}").matches(primero))
        assertNotEquals(primero, segundo)
    }

    @Test
    fun `la politica de reintentos tiene una pausa por cada reintento`() {
        assertEquals(4, AppViewModel.MAX_ATTEMPTS)
        assertEquals(AppViewModel.MAX_ATTEMPTS - 1, AppViewModel.BACKOFF_MS.size)
    }

    @Test
    fun `las pausas entre reintentos son crecientes`() {
        val pausas = AppViewModel.BACKOFF_MS.toList()
        assertEquals(listOf(5_000L, 15_000L, 30_000L), pausas)
        assertEquals(pausas.sorted(), pausas)
    }
}
