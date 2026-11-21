package com.example.vitairosapp.data.remote

import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.SyncResult
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class ApiRemotaSincronizacion(
    private val urlBase: String = URL_BASE_JSONPLACEHOLDER,
    private val tiempoConexionMs: Int = TIEMPO_ESPERA_PREDETERMINADO_MS,
    private val tiempoLecturaMs: Int = TIEMPO_ESPERA_PREDETERMINADO_MS,
) : ApiSincronizacion {

    override val descripcion: String = "API REST JSONPlaceholder"

    override fun armarCuerpo(reporte: Report): JSONObject = JSONObject().apply {
        put("title", "Reporte ${reporte.id}")
        put("body", "${reporte.type} / ${reporte.process}")
        put("userId", USUARIO_FICTICIO)
        put("reportId", reporte.id)
        put("version", reporte.version)
        put("type", reporte.type)
        put("process", reporte.process)
        put("shift", reporte.shift)
        put("attachmentCount", reporte.attachments.size)
    }

    override suspend fun enviar(reporte: Report, estaEnLinea: () -> Boolean, forzarFallo: Boolean): SyncResult {
        if (!estaEnLinea()) return SyncResult.NoConnection
        if (forzarFallo) return SyncResult.Rejected(503, "Error de envío forzado desde los controles de demostración")
        val cuerpoPeticion = armarCuerpo(reporte)
        return withContext(Dispatchers.IO) { publicar(reporte.id, cuerpoPeticion) }
    }

    private fun publicar(idReporte: String, cuerpoPeticion: JSONObject): SyncResult {
        val conexion = URL("$urlBase/posts").openConnection() as HttpURLConnection
        return try {
            conexion.requestMethod = "POST"
            conexion.connectTimeout = tiempoConexionMs
            conexion.readTimeout = tiempoLecturaMs
            conexion.doOutput = true
            conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conexion.setRequestProperty("Accept", "application/json")
            conexion.outputStream.use { it.write(cuerpoPeticion.toString().toByteArray(Charsets.UTF_8)) }
            val codigo = conexion.responseCode
            if (codigo in 200..299) {
                interpretarRespuesta(idReporte, conexion.inputStream.bufferedReader().use { it.readText() })
            } else {
                SyncResult.Rejected(codigo, "El servidor respondió HTTP $codigo")
            }
        } catch (excepcion: SocketTimeoutException) {
            SyncResult.Rejected(408, "Tiempo de espera agotado al contactar el servidor")
        } catch (excepcion: UnknownHostException) {
            SyncResult.NoConnection
        } catch (excepcion: ConnectException) {
            SyncResult.NoConnection
        } catch (excepcion: NoRouteToHostException) {
            SyncResult.NoConnection
        } catch (excepcion: IOException) {
            SyncResult.Rejected(503, "Error de red: ${excepcion.javaClass.simpleName}")
        } finally {
            conexion.disconnect()
        }
    }

    private fun interpretarRespuesta(idReporte: String, cuerpo: String): SyncResult {
        val identificador = try {
            JSONObject(cuerpo).opt("id")
        } catch (excepcion: JSONException) {
            null
        }
        if (identificador == null || identificador == JSONObject.NULL) {
            return SyncResult.Rejected(502, "Respuesta inesperada del servidor")
        }
        return SyncResult.Accepted(
            receipt = "JSP-$identificador",
            message = "Recepción confirmada por JSONPlaceholder para $idReporte"
        )
    }

    companion object {
        const val URL_BASE_JSONPLACEHOLDER = "https://jsonplaceholder.typicode.com"
        const val TIEMPO_ESPERA_PREDETERMINADO_MS = 8_000
        private const val USUARIO_FICTICIO = 1
    }
}
