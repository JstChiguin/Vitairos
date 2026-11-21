package com.example.vitairosapp.remoto

import com.example.vitairosapp.data.model.Attachment
import com.example.vitairosapp.data.model.AttachmentSource
import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.SyncResult
import com.example.vitairosapp.data.remote.ApiRemotaSincronizacion
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiRemotaSincronizacionTest {

    private val solicitudesRecibidas = AtomicInteger(0)
    private val cuerpoRecibido = AtomicReference<String?>(null)
    private val metodoRecibido = AtomicReference<String?>(null)
    private val rutaRecibida = AtomicReference<String?>(null)
    private var servidor: HttpServer? = null

    private val reporte = Report(
        id = "R1",
        user = "Usuario ficticio",
        installId = "INST-TEST",
        type = "Tipo ficticio",
        process = "Proceso ficticio",
        area = "Área ficticia",
        shift = "Día",
        notes = "Notas ficticias que no deben salir del dispositivo",
        attachments = listOf(
            Attachment(
                id = "A1",
                name = "documento_privado.pdf",
                mimeType = "application/pdf",
                sizeBytes = 100,
                sha256 = "hash-secreto",
                source = AttachmentSource.DEMO,
            )
        ),
    )

    @After
    fun detenerServidor() {
        servidor?.stop(0)
    }

    private fun iniciarServidor(respuesta: (HttpExchange) -> Unit): String {
        val servidorHttp = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        servidorHttp.createContext("/") { intercambio ->
            solicitudesRecibidas.incrementAndGet()
            metodoRecibido.set(intercambio.requestMethod)
            rutaRecibida.set(intercambio.requestURI.path)
            cuerpoRecibido.set(intercambio.requestBody.readBytes().toString(Charsets.UTF_8))
            respuesta(intercambio)
            intercambio.close()
        }
        servidorHttp.start()
        servidor = servidorHttp
        return "http://127.0.0.1:${servidorHttp.address.port}"
    }

    private fun responder(intercambio: HttpExchange, codigo: Int, cuerpo: String) {
        val bytes = cuerpo.toByteArray(Charsets.UTF_8)
        intercambio.responseHeaders.add("Content-Type", "application/json")
        intercambio.sendResponseHeaders(codigo, bytes.size.toLong())
        intercambio.responseBody.use { it.write(bytes) }
    }

    private fun api(urlBase: String, esperaMs: Int = 2_000) =
        ApiRemotaSincronizacion(urlBase, esperaMs, esperaMs)

    @Test
    fun `una respuesta 201 con identificador es aceptada con comprobante`() = runTest {
        val direccionBase = iniciarServidor { responder(it, 201, """{"id": 101, "title": "eco"}""") }

        val resultado = api(direccionBase).enviar(reporte, { true }, false)

        assertTrue(resultado is SyncResult.Accepted)
        assertEquals("JSP-101", (resultado as SyncResult.Accepted).receipt)
        assertTrue(resultado.message.contains("R1"))
    }

    @Test
    fun `la solicitud es un POST a posts con contenido json`() = runTest {
        val direccionBase = iniciarServidor { responder(it, 201, """{"id": 101}""") }

        api(direccionBase).enviar(reporte, { true }, false)

        assertEquals("POST", metodoRecibido.get())
        assertEquals("/posts", rutaRecibida.get())
        val enviado = JSONObject(cuerpoRecibido.get()!!)
        assertEquals("R1", enviado.getString("reportId"))
        assertEquals(1, enviado.getInt("attachmentCount"))
    }

    @Test
    fun `no se envian usuario, instalacion, notas, area ni datos de adjuntos`() = runTest {
        val direccionBase = iniciarServidor { responder(it, 201, """{"id": 101}""") }

        api(direccionBase).enviar(reporte, { true }, false)

        val cuerpo = cuerpoRecibido.get()!!
        listOf(
            "Usuario ficticio", "INST-TEST", "Notas ficticias", "Área ficticia",
            "documento_privado", "hash-secreto"
        ).forEach { assertFalse("No debe enviarse: $it", cuerpo.contains(it)) }
    }

    @Test
    fun `un error 500 del servidor se rechaza con su codigo`() = runTest {
        val direccionBase = iniciarServidor { responder(it, 500, """{"error": "falla"}""") }

        val resultado = api(direccionBase).enviar(reporte, { true }, false)

        assertTrue(resultado is SyncResult.Rejected)
        assertEquals(500, (resultado as SyncResult.Rejected).code)
    }

    @Test
    fun `un error 404 del servidor se rechaza con su codigo`() = runTest {
        val direccionBase = iniciarServidor { responder(it, 404, "{}") }

        val resultado = api(direccionBase).enviar(reporte, { true }, false)

        assertEquals(404, (resultado as SyncResult.Rejected).code)
    }

    @Test
    fun `un servidor que no responde a tiempo se rechaza con codigo 408`() = runTest {
        val direccionBase = iniciarServidor {
            Thread.sleep(1_500)
            responder(it, 201, """{"id": 101}""")
        }

        val resultado = api(direccionBase, esperaMs = 300).enviar(reporte, { true }, false)

        assertTrue(resultado is SyncResult.Rejected)
        assertEquals(408, (resultado as SyncResult.Rejected).code)
    }

    @Test
    fun `un servidor inalcanzable se interpreta como sin conexion`() = runTest {
        val puertoCerrado = ServerSocket(0).use { it.localPort }

        val resultado = api("http://127.0.0.1:$puertoCerrado").enviar(reporte, { true }, false)

        assertEquals(SyncResult.NoConnection, resultado)
    }

    @Test
    fun `una respuesta que no es json se rechaza como inesperada`() = runTest {
        val direccionBase = iniciarServidor { responder(it, 201, "<html>no es json</html>") }

        val resultado = api(direccionBase).enviar(reporte, { true }, false)

        assertEquals(502, (resultado as SyncResult.Rejected).code)
    }

    @Test
    fun `una respuesta json sin identificador se rechaza como inesperada`() = runTest {
        val direccionBase = iniciarServidor { responder(it, 201, """{"title": "sin id"}""") }

        val resultado = api(direccionBase).enviar(reporte, { true }, false)

        assertEquals(502, (resultado as SyncResult.Rejected).code)
    }

    @Test
    fun `una respuesta con identificador nulo se rechaza como inesperada`() = runTest {
        val direccionBase = iniciarServidor { responder(it, 201, """{"id": null}""") }

        val resultado = api(direccionBase).enviar(reporte, { true }, false)

        assertEquals(502, (resultado as SyncResult.Rejected).code)
    }

    @Test
    fun `sin conexion simulada no se realiza ninguna solicitud`() = runTest {
        val direccionBase = iniciarServidor { responder(it, 201, """{"id": 101}""") }

        val resultado = api(direccionBase).enviar(reporte, { false }, false)

        assertEquals(SyncResult.NoConnection, resultado)
        assertEquals(0, solicitudesRecibidas.get())
    }

    @Test
    fun `el error forzado responde 503 sin realizar ninguna solicitud`() = runTest {
        val direccionBase = iniciarServidor { responder(it, 201, """{"id": 101}""") }

        val resultado = api(direccionBase).enviar(reporte, { true }, true)

        assertEquals(503, (resultado as SyncResult.Rejected).code)
        assertEquals(0, solicitudesRecibidas.get())
    }

    @Test
    fun `el cuerpo remoto contiene solo campos de clasificacion`() {
        val cuerpoPeticion = ApiRemotaSincronizacion().armarCuerpo(reporte)

        assertEquals(
            setOf("title", "body", "userId", "reportId", "version", "type", "process", "shift", "attachmentCount"),
            cuerpoPeticion.keys().asSequence().toSet()
        )
    }
}
