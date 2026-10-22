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
class MockSyncApi {

    fun buildPayload(report: Report): JSONObject = JSONObject().apply {
        put("reportId", report.id)
        put("version", report.version)
        put("type", report.type)
        put("process", report.process)
        put("area", report.area)
        put("shift", report.shift)
        put("notes", report.notes)
        put("user", report.user)
        put("installId", report.installId)
        put("createdAt", report.createdAt)
        put("attachments", JSONArray().apply {
            report.attachments.forEach {
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

    suspend fun upload(report: Report, isOnline: () -> Boolean, fail: Boolean): SyncResult {
        if (!isOnline()) return SyncResult.NoConnection
        val totalBytes = report.attachments.sumOf { it.sizeBytes }
        // Latencia simulada: 600 ms + 1 ms cada 2 KB (máximo 2 s)
        delay((totalBytes / 2000).coerceAtMost(1400) + 600)
        if (!isOnline()) return SyncResult.NoConnection
        if (fail) return SyncResult.Rejected(503, "Servicio de recepción no disponible (simulado)")
        val receipt = "RCV-" + UUID.randomUUID().toString().take(8).uppercase(Locale.ROOT)
        return SyncResult.Accepted(receipt, "Recepción confirmada para ${report.id}")
    }
}
