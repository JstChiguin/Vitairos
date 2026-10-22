package com.example.vitairosapp.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.vitairosapp.data.local.db.entity.AttachmentEntity
import com.example.vitairosapp.data.local.db.entity.DraftEntity
import com.example.vitairosapp.data.local.db.entity.LogEntryEntity
import com.example.vitairosapp.data.local.db.entity.ReportEntity
import com.example.vitairosapp.data.local.db.entity.ReportWithDetails

@Dao
interface ReportDao {

    // ---------------- Reportes ----------------

    @Transaction
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    suspend fun getAll(): List<ReportWithDetails>

    @Query("SELECT COUNT(*) FROM reports")
    suspend fun count(): Int

    @Upsert
    suspend fun upsertReport(report: ReportEntity)

    @Query("DELETE FROM attachments WHERE reportId = :reportId")
    suspend fun deleteAttachments(reportId: String)

    @Insert
    suspend fun insertAttachments(attachments: List<AttachmentEntity>)

    @Query("DELETE FROM log_entries WHERE reportId = :reportId")
    suspend fun deleteLog(reportId: String)

    @Insert
    suspend fun insertLog(entries: List<LogEntryEntity>)

    /** Guarda el reporte completo (datos, adjuntos y trazabilidad) en una sola transacción. */
    @Transaction
    suspend fun save(report: ReportEntity, attachments: List<AttachmentEntity>, log: List<LogEntryEntity>) {
        upsertReport(report)
        deleteAttachments(report.id)
        if (attachments.isNotEmpty()) insertAttachments(attachments)
        deleteLog(report.id)
        if (log.isNotEmpty()) insertLog(log)
    }

    /** Elimina el reporte; sus adjuntos y eventos se borran en cascada. */
    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM reports")
    suspend fun deleteAll()

    // ---------------- Borrador en curso ----------------

    @Query("SELECT * FROM draft WHERE slot = 0")
    suspend fun getDraft(): DraftEntity?

    @Upsert
    suspend fun saveDraft(draft: DraftEntity)

    @Query("DELETE FROM draft")
    suspend fun clearDraft()
}
