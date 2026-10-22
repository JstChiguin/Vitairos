package com.example.vitairosapp.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tabla `log_entries`: eventos de trazabilidad de cada reporte (CASCADE al eliminarlo). */
@Entity(
    tableName = "log_entries",
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
data class LogEntryEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val reportId: String,
    val position: Int,
    val at: Long,
    val message: String,
)
