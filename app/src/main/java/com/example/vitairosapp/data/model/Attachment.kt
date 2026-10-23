package com.example.vitairosapp.data.model


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
