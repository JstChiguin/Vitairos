package com.example.vitairosapp.remoto

import com.example.vitairosapp.data.model.SitioClima
import com.example.vitairosapp.data.remote.ApiClimaOpenMeteo
import com.example.vitairosapp.data.remote.ResultadoClima
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiClimaOpenMeteoTest {

    private val metodoRecibido = AtomicReference<String?>(null)
    private val rutaRecibida = AtomicReference<String?>(null)
    private val consultaRecibida = AtomicReference<String?>(null)
    private var servidor: HttpServer? = null

    private val sitio = SitioClima("Planta ficticia", -33.45, -70.66)

    private val respuestaValida = """
        {
          "current": {
            "time": "2026-11-19T10:45",
            "temperature_2m": 18.5,
            "relative_humidity_2m": 56,
            "wind_speed_10m": 4.5
          },
          "daily": { "precipitation_probability_max": [33] }
        }
    """.trimIndent()

    @After
    fun detenerServidor() {
        servidor?.stop(0)
    }

    private fun iniciarServidor(respuesta: (HttpExchange) -> Unit): String {
        val servidorHttp = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        servidorHttp.createContext("/") { intercambio ->
            metodoRecibido.set(intercambio.requestMethod)
            rutaRecibida.set(intercambio.requestURI.path)
            consultaRecibida.set(intercambio.requestURI.rawQuery)
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

    private fun api(urlBase: String, esperaMs: Int = 2_000) = ApiClimaOpenMeteo(urlBase, esperaMs, esperaMs)

    private fun fallo(resultado: ResultadoClima): ResultadoClima.Fallo {
        assertTrue("Se esperaba un fallo y llegó $resultado", resultado is ResultadoClima.Fallo)
        return resultado as ResultadoClima.Fallo
    }

    @Test
    fun `una respuesta valida se convierte en condiciones del clima`() = runTest {
        val direccionBase = iniciarServidor { responder(it, 200, respuestaValida) }

        val resultado = api(direccionBase).consultar(sitio)

        assertTrue(resultado is ResultadoClima.Exitoso)
        val condiciones = (resultado as ResultadoClima.Exitoso).condiciones
        assertEquals(18.5, condiciones.temperaturaC, 0.0)
        assertEquals(4.5, condiciones.vientoKmh, 0.0)
        assertEquals(56, condiciones.humedadPorcentaje)
        assertEquals(33, condiciones.probabilidadLluviaPorcentaje)
        assertEquals("2026-11-19T10:45", condiciones.medidoEn)
    }

    @Test
    fun `la solicitud es un GET con coordenadas y variables esperadas`() = runTest {
        val direccionBase = iniciarServidor { responder(it, 200, respuestaValida) }

        api(direccionBase).consultar(sitio)

        assertEquals("GET", metodoRecibido.get())
        assertEquals("/v1/forecast", rutaRecibida.get())
        val consulta = consultaRecibida.get()!!
        assertTrue(consulta.contains("latitude=-33.4500"))
        assertTrue(consulta.contains("longitude=-70.6600"))
        assertTrue(consulta.contains("current=temperature_2m,relative_humidity_2m,wind_speed_10m"))
        assertTrue(consulta.contains("daily=precipitation_probability_max"))
    }

    @Test
    fun `los valores decimales de humedad y lluvia se redondean`() = runTest {
        val cuerpo = """
            {"current":{"time":"t","temperature_2m":1.0,"relative_humidity_2m":55.6,"wind_speed_10m":2.0},
             "daily":{"precipitation_probability_max":[32.4]}}
        """.trimIndent()
        val direccionBase = iniciarServidor { responder(it, 200, cuerpo) }

        val condiciones = (api(direccionBase).consultar(sitio) as ResultadoClima.Exitoso).condiciones

        assertEquals(56, condiciones.humedadPorcentaje)
        assertEquals(32, condiciones.probabilidadLluviaPorcentaje)
    }

    @Test
    fun `un error 500 se informa como fallo con su codigo`() = runTest {
        val direccionBase = iniciarServidor { responder(it, 500, "{}") }

        assertEquals(500, fallo(api(direccionBase).consultar(sitio)).codigo)
    }

    @Test
    fun `un error 429 por exceso de solicitudes se informa con su codigo`() = runTest {
        val direccionBase = iniciarServidor { responder(it, 429, "{}") }

        assertEquals(429, fallo(api(direccionBase).consultar(sitio)).codigo)
    }

    @Test
    fun `un servidor lento se informa como tiempo de espera agotado`() = runTest {
        val direccionBase = iniciarServidor {
            Thread.sleep(1_500)
            responder(it, 200, respuestaValida)
        }

        assertEquals(408, fallo(api(direccionBase, esperaMs = 300).consultar(sitio)).codigo)
    }

    @Test
    fun `un servidor inalcanzable se informa como sin conexion`() = runTest {
        val puertoCerrado = ServerSocket(0).use { it.localPort }

        val resultado = api("http://127.0.0.1:$puertoCerrado").consultar(sitio)

        assertEquals(ResultadoClima.SinConexion, resultado)
    }

    @Test
    fun `una respuesta que no es json se informa como inesperada`() = runTest {
        val direccionBase = iniciarServidor { responder(it, 200, "<html>no es json</html>") }

        assertEquals(502, fallo(api(direccionBase).consultar(sitio)).codigo)
    }

    @Test
    fun `una respuesta sin el bloque current se informa como inesperada`() = runTest {
        val direccionBase = iniciarServidor { responder(it, 200, """{"daily":{"precipitation_probability_max":[10]}}""") }

        assertEquals(502, fallo(api(direccionBase).consultar(sitio)).codigo)
    }

    @Test
    fun `una respuesta sin el bloque daily se informa como inesperada`() = runTest {
        val cuerpo = """{"current":{"time":"t","temperature_2m":1,"relative_humidity_2m":1,"wind_speed_10m":1}}"""
        val direccionBase = iniciarServidor { responder(it, 200, cuerpo) }

        assertEquals(502, fallo(api(direccionBase).consultar(sitio)).codigo)
    }

    @Test
    fun `una probabilidad de lluvia nula se informa como inesperada`() = runTest {
        val cuerpo = """
            {"current":{"time":"t","temperature_2m":1,"relative_humidity_2m":1,"wind_speed_10m":1},
             "daily":{"precipitation_probability_max":[null]}}
        """.trimIndent()
        val direccionBase = iniciarServidor { responder(it, 200, cuerpo) }

        assertEquals(502, fallo(api(direccionBase).consultar(sitio)).codigo)
    }

    @Test
    fun `una respuesta sin humedad se informa como inesperada`() = runTest {
        val cuerpo = """
            {"current":{"time":"t","temperature_2m":1,"wind_speed_10m":1},
             "daily":{"precipitation_probability_max":[10]}}
        """.trimIndent()
        val direccionBase = iniciarServidor { responder(it, 200, cuerpo) }

        assertEquals(502, fallo(api(direccionBase).consultar(sitio)).codigo)
    }

    @Test
    fun `una temperatura no numerica se informa como inesperada`() = runTest {
        val cuerpo = """
            {"current":{"time":"t","temperature_2m":"caliente","relative_humidity_2m":1,"wind_speed_10m":1},
             "daily":{"precipitation_probability_max":[10]}}
        """.trimIndent()
        val direccionBase = iniciarServidor { responder(it, 200, cuerpo) }

        assertEquals(502, fallo(api(direccionBase).consultar(sitio)).codigo)
    }
}
