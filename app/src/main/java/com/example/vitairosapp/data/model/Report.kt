package com.example.vitairosapp.data.model

import java.util.Locale
import java.util.UUID

data class Report(
    val id: String,
    val createdAt: Long = System.currentTimeMillis(),
    val user: String,
    val installId: String,
    val type: String = "",
    val process: String = "",
    val area: String = "",
    val shift: String = "",
    val notes: String = "",
    val attachments: List<Attachment> = emptyList(),
    val status: ReportStatus = ReportStatus.BORRADOR,
    val attempts: Int = 0,
    val version: Int = 1,
    val log: List<LogEntry> = emptyList(),
    val syncedAt: Long? = null,
    val receipt: String? = null,
    val nextRetryAt: Long? = null,
    val updatedAt: Long = createdAt,
) {
    fun withLog(message: String): Report =
        copy(log = log + LogEntry(message = message), updatedAt = System.currentTimeMillis())

    companion object {
        /** ID legible: VIT-<marca de tiempo base36>-<sufijo aleatorio>. */
        fun newId(): String {
            val time = java.lang.Long.toString(System.currentTimeMillis(), 36).uppercase(Locale.ROOT)
            val suffix = UUID.randomUUID().toString().replace("-", "").take(4).uppercase(Locale.ROOT)
            return "VIT-$time-$suffix"
        }
    }
}
