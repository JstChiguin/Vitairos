package com.example.vitairosapp.remoto

import com.example.vitairosapp.data.model.Attachment
import com.example.vitairosapp.data.model.AttachmentSource
import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.SyncResult
import com.example.vitairosapp.data.remote.MockSyncApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiSimuladaTest {

    private val api = MockSyncApi()

    private val reporte = Report(
        id = "R1",
        user = "Usuario ficticio",
        installId = "INST-TEST",
        type = "Tipo ficticio",
        process = "Proceso ficticio",
        attachments = listOf(
            Attachment(
                id = "A1",
                name = "ejemplo.pdf",
                mimeType = "application/pdf",
                sizeBytes = 4_000,
                sha256 = "hash",
                source = AttachmentSource.DEMO,
            )
        ),
    )

    @Test
    fun `sin conexion el envio queda sin conexion`() = runTest {
        assertEquals(SyncResult.NoConnection, api.enviar(reporte, { false }, false))
    }

    @Test
    fun `con conexion el envio es aceptado con comprobante`() = runTest {
        val resultado = api.enviar(reporte, { true }, false)
        assertTrue(resultado is SyncResult.Accepted)
        val aceptado = resultado as SyncResult.Accepted
        assertTrue(Regex("RCV-[0-9A-F]{8}").matches(aceptado.receipt))
        assertTrue(aceptado.message.contains("R1"))
    }

    @Test
    fun `el error forzado responde rechazo 503`() = runTest {
        val resultado = api.enviar(reporte, { true }, true)
        assertTrue(resultado is SyncResult.Rejected)
        assertEquals(503, (resultado as SyncResult.Rejected).code)
    }

    @Test
    fun `si se pierde la conexion durante el envio queda sin conexion`() = runTest {
        var consultas = 0
        val resultado = api.enviar(reporte, { ++consultas == 1 }, false)
        assertEquals(SyncResult.NoConnection, resultado)
    }

    @Test
    fun `el cuerpo simulado incluye la metadata y los adjuntos`() {
        val cuerpoPeticion = api.armarCuerpo(reporte)
        assertEquals("R1", cuerpoPeticion.getString("reportId"))
        assertEquals(1, cuerpoPeticion.getJSONArray("attachments").length())
        assertEquals(4_000L, cuerpoPeticion.getJSONArray("attachments").getJSONObject(0).getLong("bytes"))
    }
}
