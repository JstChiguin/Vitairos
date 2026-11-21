package com.example.vitairosapp.data.repository

import android.util.Log
import com.example.vitairosapp.data.local.ReportJson
import com.example.vitairosapp.data.local.db.dao.ReportDao
import com.example.vitairosapp.data.local.db.entity.DraftEntity
import com.example.vitairosapp.data.local.db.attachmentEntities
import com.example.vitairosapp.data.local.db.logEntities
import com.example.vitairosapp.data.local.db.toEntity
import com.example.vitairosapp.data.local.db.toModel
import com.example.vitairosapp.data.model.ModoApp
import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.ReportStatus
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Acceso a los reportes guardados en la base de datos SQLite (Room).
 *
 * Mantiene una copia en memoria ([reports]) para que la interfaz la observe, y cada cambio
 * se escribe en la base dentro de una transacción. La primera vez migra los datos que
 * versiones anteriores guardaban en `reports.json` y `draft.json`.
 */
class ReportRepository(
    private val dao: ReportDao,
    baseDir: File,
    modoInicial: ModoApp = ModoApp.OPERATIVO,
) {

    private val legacyDir = File(baseDir, "data")
    private val mutex = Mutex()

    private var todos: List<Report>
    private val _reports: MutableStateFlow<List<Report>>
    val reports: StateFlow<List<Report>>
    private val borradores: MutableMap<ModoApp, Report?>
    var modo: ModoApp = modoInicial
        private set

    init {
        // Carga inicial (una sola vez al abrir la app) para que la UI arranque con los datos
        val (loaded, savedDrafts) = runBlocking(Dispatchers.IO) {
            migrateLegacyJson()
            loadReports() to ModoApp.entries.associateWith { loadDraftFromDb(it) }
        }
        todos = loaded
        _reports = MutableStateFlow(loaded.filter { it.modo == modo })
        reports = _reports.asStateFlow()
        borradores = savedDrafts.toMutableMap()
    }

    fun find(id: String): Report? = _reports.value.firstOrNull { it.id == id }

    fun idsReferenciados(): Set<String> =
        todos.map { it.id }.toSet() + borradores.values.mapNotNull { it?.id }

    private fun publicar(nuevos: List<Report>) {
        todos = nuevos
        _reports.value = nuevos.filter { it.modo == modo }
    }

    suspend fun cambiarModo(nuevo: ModoApp) = mutex.withLock {
        modo = nuevo
        _reports.value = todos.filter { it.modo == nuevo }
    }

    private suspend fun loadReports(): List<Report> {
        val list = dao.getAll().map { it.toModel() }
        // Un envío que quedó a medias al cerrar la app vuelve a la cola
        return list.map { report ->
            if (report.status == ReportStatus.ENVIANDO) {
                report.copy(status = ReportStatus.EN_COLA, nextRetryAt = null)
                    .withLog("Envío interrumpido al cerrar la app: queda en cola para reintentar")
                    .also { write(it) }
            } else report
        }
    }

    private suspend fun write(report: Report) =
        dao.save(report.toEntity(), report.attachmentEntities(), report.logEntities())

    suspend fun upsert(report: Report) = mutex.withLock {
        publicar(
            if (todos.any { it.id == report.id }) todos.map { if (it.id == report.id) report else it }
            else todos + report
        )
        withContext(Dispatchers.IO) { write(report) }
    }

    /** Aplica [transform] al reporte [id], lo guarda y devuelve el resultado (null si ya no existe). */
    suspend fun update(id: String, transform: (Report) -> Report): Report? = mutex.withLock {
        val current = todos.firstOrNull { it.id == id } ?: return@withLock null
        val updated = transform(current)
        publicar(todos.map { if (it.id == id) updated else it })
        withContext(Dispatchers.IO) { write(updated) }
        updated
    }

    suspend fun delete(id: String) = mutex.withLock {
        if (todos.none { it.id == id && it.modo == modo }) return@withLock
        publicar(todos.filterNot { it.id == id })
        withContext(Dispatchers.IO) { dao.delete(id) }
    }

    suspend fun addAll(reports: List<Report>) = mutex.withLock {
        publicar(todos + reports)
        withContext(Dispatchers.IO) { reports.forEach { write(it) } }
    }

    suspend fun clear() = mutex.withLock {
        val modoActual = modo
        publicar(todos.filterNot { it.modo == modoActual })
        withContext(Dispatchers.IO) { dao.deleteAllByModo(modoActual) }
    }

    // ---------------- Borrador en curso (sobrevive al cierre de la app) ----------------

    fun loadDraft(): Report? = borradores[modo]

    private suspend fun loadDraftFromDb(modo: ModoApp): Report? = try {
        dao.getDraft(modo.ranuraBorrador)?.let { ReportJson.fromJson(JSONObject(it.reportJson)) }
    } catch (e: Exception) {
        Log.w(TAG, "Borrador ilegible; se descarta", e)
        null
    }

    suspend fun saveDraft(value: Report?) = mutex.withLock {
        val ranura = modo.ranuraBorrador
        borradores[modo] = value
        withContext(Dispatchers.IO) {
            if (value == null) dao.clearDraft(ranura)
            else dao.saveDraft(DraftEntity(slot = ranura, reportJson = ReportJson.toJson(value).toString()))
        }
    }

    // ---------------- Migración desde la versión con archivos JSON ----------------

    private suspend fun migrateLegacyJson() {
        val reportsFile = File(legacyDir, "reports.json")
        val draftFile = File(legacyDir, "draft.json")
        if (reportsFile.exists()) {
            try {
                if (dao.count() == 0) {
                    ReportJson.listFromJson(JSONArray(reportsFile.readText())).forEach { write(it) }
                }
                reportsFile.renameTo(File(legacyDir, "reports.migrated.json"))
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo migrar reports.json; se conserva como respaldo", e)
                reportsFile.renameTo(File(legacyDir, "reports.corrupt-${System.currentTimeMillis()}.json"))
            }
        }
        if (draftFile.exists()) {
            try {
                if (dao.getDraft(ModoApp.OPERATIVO.ranuraBorrador) == null) dao.saveDraft(DraftEntity(reportJson = draftFile.readText()))
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo migrar draft.json", e)
            }
            draftFile.delete()
        }
    }

    private companion object {
        const val TAG = "ReportRepository"
    }
}
