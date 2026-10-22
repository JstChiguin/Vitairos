package com.example.vitairosapp.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.vitairosapp.data.model.AttachmentSource

/**
 * Tabla `attachments`: metadatos de los documentos de cada reporte.
 * El archivo en sí queda en el almacenamiento privado; aquí solo se guarda su ruta.
 * Al eliminar un reporte se eliminan sus adjuntos (CASCADE).
 */
@Entity(
    tableName = "attachments",
    primaryKeys = ["reportId", "id"],
    foreignKeys = [
        ForeignKey(
            entity = ReportEntity::class,
            parentColumns = ["id"],
            childColumns = ["reportId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reportId")]
)
data class AttachmentEntity(
    val reportId: String,
    val id: String,
    /** Orden en que se agregó dentro del reporte. */
    val position: Int,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val originalSizeBytes: Long,
    val path: String?,
    val sha256: String,
    val source: AttachmentSource,
    val addedAt: Long,
    val width: Int,
    val height: Int,
    val pages: Int,
)
