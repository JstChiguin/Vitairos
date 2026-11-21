package com.example.vitairosapp.datos

import com.example.vitairosapp.data.local.ReportJson
import com.example.vitairosapp.data.model.Attachment
import com.example.vitairosapp.data.model.AttachmentSource
import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.ReportStatus
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SerializacionReporteTest {

    private val reporteCompleto = Report(
        id = "R1",
        createdAt = 1_000L,
        user = "Usuario ficticio",
        installId = "INST-TEST",
        type = "Tipo ficticio",
        process = "Proceso ficticio",
        area = "Área ficticia",
        shift = "Día",
        notes = "Notas ficticias",
        attachments = listOf(
            Attachment(
                id = "A1",
                name = "ejemplo.pdf",
                mimeType = "application/pdf",
                sizeBytes = 200,
                path = "ruta/ejemplo.pdf",
                sha256 = "hash",
                source = AttachmentSource.DEMO,
                addedAt = 2_000L,
                pages = 1,
            )
        ),
        status = ReportStatus.SINCRONIZADO,
        attempts = 2,
        version = 3,
        syncedAt = 3_000L,
        receipt = "RCV-TEST",
        nextRetryAt = 4_000L,
        updatedAt = 5_000L,
    ).withLog("evento").let { it.copy(updatedAt = 5_000L) }

    @Test
    fun `un reporte completo sobrevive al ciclo de serializacion`() {
        val recuperado = ReportJson.fromJson(ReportJson.toJson(reporteCompleto))
        assertEquals(reporteCompleto, recuperado)
    }

    @Test
    fun `los campos opcionales ausentes se recuperan como null`() {
        val minimo = Report(id = "R2", createdAt = 1_000L, user = "Usuario ficticio", installId = "INST-TEST")
        val recuperado = ReportJson.fromJson(ReportJson.toJson(minimo))
        assertNull(recuperado.syncedAt)
        assertNull(recuperado.receipt)
        assertNull(recuperado.nextRetryAt)
    }

    @Test
    fun `un estado desconocido se interpreta como borrador`() {
        val json = ReportJson.toJson(reporteCompleto).put("status", "ESTADO_INVENTADO")
        assertEquals(ReportStatus.BORRADOR, ReportJson.fromJson(json).status)
    }

    @Test
    fun `una lista de reportes se serializa y se recupera completa`() {
        val lista = listOf(reporteCompleto, reporteCompleto.copy(id = "R3"))
        val recuperada = ReportJson.listFromJson(ReportJson.listToJson(lista))
        assertEquals(lista.map { it.id }, recuperada.map { it.id })
    }

    @Test
    fun `un adjunto sin ruta conserva la ausencia de ruta`() {
        val adjunto = reporteCompleto.attachments.first().copy(path = null)
        val recuperado = ReportJson.attachmentFromJson(ReportJson.attachmentToJson(adjunto))
        assertNull(recuperado.path)
    }

    @Test(expected = Exception::class)
    fun `un json sin identificador es invalido`() {
        ReportJson.fromJson(JSONObject().put("type", "sin id"))
    }
}
