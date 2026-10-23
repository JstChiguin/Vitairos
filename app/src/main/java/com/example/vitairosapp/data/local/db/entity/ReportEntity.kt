package com.example.vitairosapp.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.vitairosapp.data.model.ReportStatus

/** Tabla `reports`: datos principales de cada reporte guardado. */
@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val user: String,
    val installId: String,
    val type: String,
    val process: String,
    val area: String,
    val shift: String,
    val notes: String,
    val status: ReportStatus,
    val attempts: Int,
    val version: Int,
    val syncedAt: Long?,
    val receipt: String?,
    val nextRetryAt: Long?,
    val updatedAt: Long,
)
