package com.example.vitairosapp.data.local

import com.example.vitairosapp.data.model.Attachment
import com.example.vitairosapp.data.model.AttachmentSource
import com.example.vitairosapp.data.model.LogEntry
import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.ReportStatus
import org.json.JSONArray
import org.json.JSONObject

/** Serialización manual a JSON (sin dependencias externas). */
object ReportJson {

    fun toJson(r: Report): JSONObject = JSONObject().apply {
        put("id", r.id)
        put("createdAt", r.createdAt)
        put("user", r.user)
        put("installId", r.installId)
        put("type", r.type)
        put("process", r.process)
        put("area", r.area)
        put("shift", r.shift)
        put("notes", r.notes)
        put("status", r.status.name)
        put("attempts", r.attempts)
        put("version", r.version)
        put("updatedAt", r.updatedAt)
        r.syncedAt?.let { put("syncedAt", it) }
        r.receipt?.let { put("receipt", it) }
        r.nextRetryAt?.let { put("nextRetryAt", it) }
        put("attachments", JSONArray().apply { r.attachments.forEach { put(attachmentToJson(it)) } })
        put("log", JSONArray().apply {
            r.log.forEach { put(JSONObject().put("at", it.at).put("message", it.message)) }
        })
    }

    fun fromJson(o: JSONObject): Report {
        val attachments = o.optJSONArray("attachments")?.let { arr ->
            (0 until arr.length()).map { attachmentFromJson(arr.getJSONObject(it)) }
        } ?: emptyList()
        val log = o.optJSONArray("log")?.let { arr ->
            (0 until arr.length()).map {
                val e = arr.getJSONObject(it)
                LogEntry(e.optLong("at"), e.getString("message"))
            }
        } ?: emptyList()
        val createdAt = o.optLong("createdAt", System.currentTimeMillis())
        return Report(
            id = o.getString("id"),
            createdAt = createdAt,
            user = o.optString("user"),
            installId = o.optString("installId"),
            type = o.optString("type"),
            process = o.optString("process"),
            area = o.optString("area"),
            shift = o.optString("shift"),
            notes = o.optString("notes"),
            attachments = attachments,
            status = runCatching { ReportStatus.valueOf(o.optString("status")) }
                .getOrDefault(ReportStatus.BORRADOR),
            attempts = o.optInt("attempts"),
            version = o.optInt("version", 1),
            log = log,
            syncedAt = if (o.has("syncedAt")) o.getLong("syncedAt") else null,
            receipt = if (o.has("receipt")) o.getString("receipt") else null,
            nextRetryAt = if (o.has("nextRetryAt")) o.getLong("nextRetryAt") else null,
            updatedAt = o.optLong("updatedAt", createdAt),
        )
    }

    fun attachmentToJson(a: Attachment): JSONObject = JSONObject().apply {
        put("id", a.id)
        put("name", a.name)
        put("mimeType", a.mimeType)
        put("sizeBytes", a.sizeBytes)
        put("originalSizeBytes", a.originalSizeBytes)
        a.path?.let { put("path", it) }
        put("sha256", a.sha256)
        put("source", a.source.name)
        put("addedAt", a.addedAt)
        put("width", a.width)
        put("height", a.height)
        put("pages", a.pages)
    }

    fun attachmentFromJson(o: JSONObject): Attachment = Attachment(
        id = o.getString("id"),
        name = o.optString("name"),
        mimeType = o.optString("mimeType"),
        sizeBytes = o.optLong("sizeBytes"),
        originalSizeBytes = o.optLong("originalSizeBytes", o.optLong("sizeBytes")),
        path = if (o.has("path")) o.getString("path") else null,
        sha256 = o.optString("sha256"),
        source = runCatching { AttachmentSource.valueOf(o.optString("source")) }
            .getOrDefault(AttachmentSource.GALERIA),
        addedAt = o.optLong("addedAt"),
        width = o.optInt("width"),
        height = o.optInt("height"),
        pages = o.optInt("pages"),
    )

    fun listToJson(reports: List<Report>): JSONArray =
        JSONArray().apply { reports.forEach { put(toJson(it)) } }

    fun listFromJson(array: JSONArray): List<Report> =
        (0 until array.length()).map { fromJson(array.getJSONObject(it)) }
}
