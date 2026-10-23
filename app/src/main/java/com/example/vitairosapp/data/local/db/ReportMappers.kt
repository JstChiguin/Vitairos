package com.example.vitairosapp.data.local.db

import com.example.vitairosapp.data.local.db.entity.AttachmentEntity
import com.example.vitairosapp.data.local.db.entity.LogEntryEntity
import com.example.vitairosapp.data.local.db.entity.ReportEntity
import com.example.vitairosapp.data.local.db.entity.ReportWithDetails
import com.example.vitairosapp.data.model.Attachment
import com.example.vitairosapp.data.model.LogEntry
import com.example.vitairosapp.data.model.Report

// Conversión entre las tablas de la base de datos y los modelos que usa la app

fun Report.toEntity() = ReportEntity(
    id = id, createdAt = createdAt, user = user, installId = installId,
    type = type, process = process, area = area, shift = shift, notes = notes,
    status = status, attempts = attempts, version = version,
    syncedAt = syncedAt, receipt = receipt, nextRetryAt = nextRetryAt, updatedAt = updatedAt,
)

fun Report.attachmentEntities() = attachments.mapIndexed { i, a ->
    AttachmentEntity(
        reportId = id, id = a.id, position = i, name = a.name, mimeType = a.mimeType,
        sizeBytes = a.sizeBytes, originalSizeBytes = a.originalSizeBytes, path = a.path,
        sha256 = a.sha256, source = a.source, addedAt = a.addedAt,
        width = a.width, height = a.height, pages = a.pages,
    )
}

fun Report.logEntities() = log.mapIndexed { i, e ->
    LogEntryEntity(reportId = id, position = i, at = e.at, message = e.message)
}

fun ReportWithDetails.toModel() = Report(
    id = report.id, createdAt = report.createdAt, user = report.user, installId = report.installId,
    type = report.type, process = report.process, area = report.area, shift = report.shift,
    notes = report.notes,
    attachments = attachments.sortedBy { it.position }.map {
        Attachment(
            id = it.id, name = it.name, mimeType = it.mimeType, sizeBytes = it.sizeBytes,
            originalSizeBytes = it.originalSizeBytes, path = it.path, sha256 = it.sha256,
            source = it.source, addedAt = it.addedAt, width = it.width, height = it.height, pages = it.pages,
        )
    },
    status = report.status, attempts = report.attempts, version = report.version,
    log = log.sortedBy { it.position }.map { LogEntry(it.at, it.message) },
    syncedAt = report.syncedAt, receipt = report.receipt, nextRetryAt = report.nextRetryAt,
    updatedAt = report.updatedAt,
)
