package com.example.vitairosapp.data.local.db.entity

import androidx.room.Embedded
import androidx.room.Relation

/** Reporte con sus adjuntos y su trazabilidad (consulta con @Relation). */
data class ReportWithDetails(
    @Embedded val report: ReportEntity,
    @Relation(parentColumn = "id", entityColumn = "reportId")
    val attachments: List<AttachmentEntity>,
    @Relation(parentColumn = "id", entityColumn = "reportId")
    val log: List<LogEntryEntity>,
)
