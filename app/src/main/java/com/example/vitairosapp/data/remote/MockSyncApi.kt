package com.example.vitairosapp.data.remote

import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.SyncResult
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject

/**
 * API de recepción simulada. No transmite nada: arma el payload, espera un tiempo
 * proporcional al tamaño y responde según la conectividad simulada.
 */
class MockSyncApi : ApiSincronizacion {

    override val descripcion: String = "API simulada"

    override fun armarCuerpo(reporte: Report): JSONObject = JSONObject().apply {
        put("reportId", reporte.id)
        put("version", reporte.version)
        put("type", reporte.type)
        put("process", reporte.process)
        put("area", reporte.area)
        put("shift", reporte.shift)
        put("notes", reporte.notes)
        put("user", reporte.user)
        put("installId", reporte.installId)
        put("createdAt", reporte.createdAt)
        put("attachments", JSONArray().apply {
            reporte.attachments.forEach {
                put(
                    JSONObject()
                        .put("name", it.name)
                        .put("mimeType", it.mimeType)
                        .put("bytes", it.sizeBytes)
                        .put("sha256", it.sha256)
                )
            }
        })
    }

    override suspend fun enviar(reporte: Report, estaEnLinea: () -> Boolean, forzarFallo: Boolean): SyncResult {
        if (!estaEnLinea()) return SyncResult.NoConnection
        val totalBytes = reporte.attachments.sumOf { it.sizeBytes }
        // Latencia simulada: 600 ms + 1 ms cada 2 KB (máximo 2 s)
        delay((totalBytes / 2000).coerceAtMost(1400) + 600)
        if (!estaEnLinea()) return SyncResult.NoConnection
        if (forzarFallo) return SyncResult.Rejected(503, "Servicio de recepción no disponible (simulado)")
        val receipt = "RCV-" + UUID.randomUUID().toString().take(8).uppercase(Locale.ROOT)
        return SyncResult.Accepted(receipt, "Recepción confirmada para ${reporte.id}")
    }
}
