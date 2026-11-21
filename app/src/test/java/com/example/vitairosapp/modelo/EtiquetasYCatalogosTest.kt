package com.example.vitairosapp.modelo

import com.example.vitairosapp.data.catalog.Catalogs
import com.example.vitairosapp.data.model.ModoApp
import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.ReportStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EtiquetasYCatalogosTest {

    private fun reporte(modo: ModoApp, estado: ReportStatus) =
        Report(id = "R", user = "u", installId = "i", status = estado, modo = modo)

    @Test
    fun `los catalogos operativos no contienen textos de demostracion`() {
        val textos = Catalogs.tipos(ModoApp.OPERATIVO) + Catalogs.procesos(ModoApp.OPERATIVO)
        assertTrue(textos.none { it.contains("demostraci", ignoreCase = true) })
    }

    @Test
    fun `los catalogos de demostracion se identifican como tales`() {
        val textos = Catalogs.tipos(ModoApp.DEMO) + Catalogs.procesos(ModoApp.DEMO)
        assertTrue(textos.all { it.contains("(demostración)") })
    }

    @Test
    fun `ambos catalogos tienen la misma cantidad de opciones`() {
        assertEquals(Catalogs.tipos(ModoApp.OPERATIVO).size, Catalogs.tipos(ModoApp.DEMO).size)
        assertEquals(Catalogs.procesos(ModoApp.OPERATIVO).size, Catalogs.procesos(ModoApp.DEMO).size)
    }

    @Test
    fun `un reporte operativo pendiente se muestra pendiente de sincronizacion`() {
        assertEquals("Pendiente de sincronización", reporte(ModoApp.OPERATIVO, ReportStatus.EN_COLA).etiquetaEstado)
    }

    @Test
    fun `un reporte demo enviado se muestra como envio simulado`() {
        assertEquals("Enviado (simulado)", reporte(ModoApp.DEMO, ReportStatus.SINCRONIZADO).etiquetaEstado)
    }

    @Test
    fun `un reporte operativo antiguo con envio simulado no se presenta como envio real`() {
        assertEquals(
            "Enviado (simulado, histórico)",
            reporte(ModoApp.OPERATIVO, ReportStatus.SINCRONIZADO).etiquetaEstado
        )
    }

    @Test
    fun `un borrador nunca se muestra como enviado`() {
        ModoApp.entries.forEach {
            val etiqueta = reporte(it, ReportStatus.BORRADOR).etiquetaEstado
            assertEquals("Borrador", etiqueta)
            assertFalse(etiqueta.contains("Enviado"))
        }
    }

    @Test
    fun `los estados tienen las etiquetas esperadas`() {
        assertEquals("Borrador", ReportStatus.BORRADOR.label)
        assertEquals("Pendiente", ReportStatus.EN_COLA.label)
        assertEquals("Enviando", ReportStatus.ENVIANDO.label)
        assertEquals("Enviado", ReportStatus.SINCRONIZADO.label)
        assertEquals("Error", ReportStatus.ERROR.label)
    }

    @Test
    fun `la cola solo contiene pendientes, enviando y error`() {
        val cola = ReportStatus.entries.filter { it.isPending }
        assertFalse(ReportStatus.BORRADOR in cola)
        assertFalse(ReportStatus.SINCRONIZADO in cola)
        assertEquals(3, cola.size)
    }

    @Test
    fun `un reporte operativo nunca figura enviado por guardarse`() {
        val guardado = reporte(ModoApp.OPERATIVO, ReportStatus.EN_COLA)
        assertTrue(guardado.status != ReportStatus.SINCRONIZADO)
        assertTrue(guardado.receipt == null && guardado.syncedAt == null)
    }

    @Test
    fun `la etiqueta de modo coincide con el modo del reporte`() {
        assertEquals("Operativo", reporte(ModoApp.OPERATIVO, ReportStatus.BORRADOR).etiquetaModo)
        assertEquals("Demostración", reporte(ModoApp.DEMO, ReportStatus.BORRADOR).etiquetaModo)
    }
}
