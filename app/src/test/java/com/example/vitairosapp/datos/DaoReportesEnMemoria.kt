package com.example.vitairosapp.datos

import com.example.vitairosapp.data.local.db.dao.ReportDao
import com.example.vitairosapp.data.local.db.entity.AttachmentEntity
import com.example.vitairosapp.data.local.db.entity.DraftEntity
import com.example.vitairosapp.data.local.db.entity.LogEntryEntity
import com.example.vitairosapp.data.local.db.entity.ReportEntity
import com.example.vitairosapp.data.local.db.entity.ReportWithDetails
import com.example.vitairosapp.data.model.ModoApp

class DaoReportesEnMemoria : ReportDao {

    private val reportes = linkedMapOf<String, ReportEntity>()
    private val adjuntos = mutableListOf<AttachmentEntity>()
    private val eventos = mutableListOf<LogEntryEntity>()
    private val borradores = mutableMapOf<Int, DraftEntity>()

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

    override suspend fun deleteAllByModo(modo: ModoApp) {
        val ids = reportes.values.filter { it.modo == modo }.map { it.id }
        ids.forEach { delete(it) }
    }

    override suspend fun getDraft(slot: Int): DraftEntity? = borradores[slot]

    override suspend fun saveDraft(draft: DraftEntity) {
        borradores[draft.slot] = draft
    }

    override suspend fun clearDraft(slot: Int) {
        borradores.remove(slot)
    }
}
