package com.example.vitairosapp.datos

import com.example.vitairosapp.data.local.db.dao.ReportDao
import com.example.vitairosapp.data.local.db.entity.AttachmentEntity
import com.example.vitairosapp.data.local.db.entity.DraftEntity
import com.example.vitairosapp.data.local.db.entity.LogEntryEntity
import com.example.vitairosapp.data.local.db.entity.ReportEntity
import com.example.vitairosapp.data.local.db.entity.ReportWithDetails

class DaoReportesEnMemoria : ReportDao {

    private val reportes = linkedMapOf<String, ReportEntity>()
    private val adjuntos = mutableListOf<AttachmentEntity>()
    private val eventos = mutableListOf<LogEntryEntity>()
    private var borrador: DraftEntity? = null

    override suspend fun getAll(): List<ReportWithDetails> =
        reportes.values.sortedByDescending { it.createdAt }.map { reporte ->
            ReportWithDetails(
                report = reporte,
                attachments = adjuntos.filter { it.reportId == reporte.id },
                log = eventos.filter { it.reportId == reporte.id },
            )
        }

    override suspend fun count(): Int = reportes.size

    override suspend fun upsertReport(report: ReportEntity) {
        reportes[report.id] = report
    }

    override suspend fun deleteAttachments(reportId: String) {
        adjuntos.removeAll { it.reportId == reportId }
    }

    override suspend fun insertAttachments(attachments: List<AttachmentEntity>) {
        adjuntos.addAll(attachments)
    }

    override suspend fun deleteLog(reportId: String) {
        eventos.removeAll { it.reportId == reportId }
    }

    override suspend fun insertLog(entries: List<LogEntryEntity>) {
        eventos.addAll(entries)
    }

    override suspend fun delete(id: String) {
        reportes.remove(id)
        deleteAttachments(id)
        deleteLog(id)
    }

    override suspend fun deleteAll() {
        reportes.clear()
        adjuntos.clear()
        eventos.clear()
    }

    override suspend fun getDraft(): DraftEntity? = borrador

    override suspend fun saveDraft(draft: DraftEntity) {
        borrador = draft
    }

    override suspend fun clearDraft() {
        borrador = null
    }
}
