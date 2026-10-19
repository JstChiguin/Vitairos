package com.example.vitairosapp.data

import java.util.Locale
import java.util.UUID

enum class ReportStatus(val label: String) {
    BORRADOR("Borrador"),
    EN_COLA("En cola"),
    ENVIANDO("Enviando"),
    SINCRONIZADO("Sincronizado"),
    ERROR("Error");

    val isEditable: Boolean get() = this == BORRADOR || this == EN_COLA || this == ERROR
    val isDeletable: Boolean get() = this == BORRADOR || this == EN_COLA
    val isPending: Boolean get() = this == EN_COLA || this == ENVIANDO || this == ERROR
}

enum class AttachmentSource(val label: String) {
    CAMARA("Cámara"),
    GALERIA("Galería"),
    PDF("PDF"),
    DEMO("Referencia simulada")
}

data class Attachment(
    val id: String,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val originalSizeBytes: Long = sizeBytes,
    /** Ruta en el almacenamiento privado; null si es una referencia simulada o ya se liberó. */
    val path: String? = null,
    val sha256: String,
    val source: AttachmentSource,
    val addedAt: Long = System.currentTimeMillis(),
    val width: Int = 0,
    val height: Int = 0,
    val pages: Int = 0,
) {
    val isImage: Boolean get() = mimeType.startsWith("image/")
    val isPdf: Boolean get() = mimeType == "application/pdf"
}

data class LogEntry(
    val at: Long = System.currentTimeMillis(),
    val message: String,
)

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

sealed class SyncResult {
    data class Accepted(val receipt: String, val message: String) : SyncResult()
    data class Rejected(val code: Int, val message: String) : SyncResult()
    data object NoConnection : SyncResult()
}

sealed class ImportResult {
    data class Ok(val attachment: Attachment, val warning: String? = null) : ImportResult()
    data class Rejected(val reason: String) : ImportResult()
}
