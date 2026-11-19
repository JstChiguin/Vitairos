package com.example.vitairosapp.data.remote

import com.example.vitairosapp.data.model.CondicionesClima
import com.example.vitairosapp.data.model.SitioClima
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class ApiClimaOpenMeteo(
    private val urlBase: String = URL_BASE_OPEN_METEO,
    private val tiempoConexionMs: Int = TIEMPO_ESPERA_PREDETERMINADO_MS,
    private val tiempoLecturaMs: Int = TIEMPO_ESPERA_PREDETERMINADO_MS,
) : ApiClima {

    override suspend fun consultar(sitio: SitioClima): ResultadoClima =
        withContext(Dispatchers.IO) { solicitar(construirUrl(sitio)) }

    private fun construirUrl(sitio: SitioClima): String {
        val latitud = String.format(Locale.US, "%.4f", sitio.latitud)
        val longitud = String.format(Locale.US, "%.4f", sitio.longitud)
        return "$urlBase/v1/forecast?latitude=$latitud&longitude=$longitud" +
            "&current=temperature_2m,relative_humidity_2m,wind_speed_10m" +
            "&daily=precipitation_probability_max&forecast_days=1&timezone=auto"
    }

    private fun solicitar(direccion: String): ResultadoClima {
        val conexion = URL(direccion).openConnection() as HttpURLConnection
        return try {
            conexion.requestMethod = "GET"
            conexion.connectTimeout = tiempoConexionMs
            conexion.readTimeout = tiempoLecturaMs
            conexion.setRequestProperty("Accept", "application/json")
            val codigo = conexion.responseCode
            if (codigo in 200..299) {
                interpretarRespuesta(conexion.inputStream.bufferedReader().use { it.readText() })
            } else {
                ResultadoClima.Fallo(codigo, "El servicio de clima respondió HTTP $codigo")
            }
        } catch (excepcion: SocketTimeoutException) {
            ResultadoClima.Fallo(408, "Tiempo de espera agotado al consultar el clima")
        } catch (excepcion: UnknownHostException) {
            ResultadoClima.SinConexion
        } catch (excepcion: ConnectException) {
            ResultadoClima.SinConexion
        } catch (excepcion: NoRouteToHostException) {
            ResultadoClima.SinConexion
        } catch (excepcion: IOException) {
            ResultadoClima.Fallo(503, "Error de red: ${excepcion.javaClass.simpleName}")
        } finally {
            conexion.disconnect()
        }
    }

    private fun interpretarRespuesta(cuerpo: String): ResultadoClima {
        val condiciones = try {
            extraerCondiciones(JSONObject(cuerpo))
        } catch (excepcion: JSONException) {
            null
        }
        return condiciones?.let { ResultadoClima.Exitoso(it) }
            ?: ResultadoClima.Fallo(502, "Respuesta inesperada del servicio de clima")
    }

    private fun extraerCondiciones(raiz: JSONObject): CondicionesClima? {
        val actual = raiz.optJSONObject("current") ?: return null
        val probabilidades = raiz.optJSONObject("daily")?.optJSONArray("precipitation_probability_max")
            ?: return null
        val temperatura = leerNumero(actual, "temperature_2m") ?: return null
        val viento = leerNumero(actual, "wind_speed_10m") ?: return null
        val humedad = leerNumero(actual, "relative_humidity_2m") ?: return null
        if (probabilidades.length() == 0 || probabilidades.isNull(0)) return null
        val probabilidad = probabilidades.optDouble(0, Double.NaN)
        if (probabilidad.isNaN()) return null
        return CondicionesClima(
            temperaturaC = temperatura,
            vientoKmh = viento,
            humedadPorcentaje = humedad.roundToInt(),
            probabilidadLluviaPorcentaje = probabilidad.roundToInt(),
            medidoEn = actual.optString("time"),
        )
    }

    private fun leerNumero(objeto: JSONObject, clave: String): Double? {
        if (!objeto.has(clave) || objeto.isNull(clave)) return null
        return objeto.optDouble(clave, Double.NaN).takeUnless { it.isNaN() }
    }

    companion object {
        const val URL_BASE_OPEN_METEO = "https://api.open-meteo.com"
        const val TIEMPO_ESPERA_PREDETERMINADO_MS = 8_000
    }
}
