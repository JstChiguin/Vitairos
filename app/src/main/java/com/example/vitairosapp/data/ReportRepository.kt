package com.example.vitairosapp.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Persistencia local de reportes en JSON dentro del almacenamiento privado de la app.
 * Cada escritura es atómica (archivo temporal + rename) para no corromper datos.
 */
class ReportRepository(baseDir: File) {

    private val dir = File(baseDir, "data").apply { mkdirs() }
    private val reportsFile = File(dir, "reports.json")
    private val draftFile = File(dir, "draft.json")
    private val mutex = Mutex()

    private val _reports = MutableStateFlow(loadReports())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    fun find(id: String): Report? = _reports.value.firstOrNull { it.id == id }

    private fun loadReports(): List<Report> {
        if (!reportsFile.exists()) return emptyList()
        return try {
            val list = ReportJson.listFromJson(JSONArray(reportsFile.readText()))
            // Un envío que quedó a medias al cerrar la app vuelve a la cola
            list.map {
                if (it.status == ReportStatus.ENVIANDO) {
                    it.copy(status = ReportStatus.EN_COLA, nextRetryAt = null)
                        .withLog("Envío interrumpido al cerrar la app: queda en cola para reintentar")
                } else it
            }
        } catch (e: Exception) {
            val backup = File(dir, "reports.corrupt-${System.currentTimeMillis()}.json")
            reportsFile.renameTo(backup)
            Log.w("ReportRepository", "Archivo de reportes ilegible; se respaldó y se inicia vacío", e)
            emptyList()
        }
    }

    private suspend fun mutate(transform: (List<Report>) -> List<Report>) = mutex.withLock {
        val next = transform(_reports.value)
        _reports.value = next
        withContext(Dispatchers.IO) { writeAtomic(reportsFile, ReportJson.listToJson(next).toString()) }
    }

    suspend fun upsert(report: Report) = mutate { list ->
        if (list.any { it.id == report.id }) list.map { if (it.id == report.id) report else it }
        else list + report
    }

    /** Aplica [transform] al reporte [id] y devuelve el resultado (null si ya no existe). */
    suspend fun update(id: String, transform: (Report) -> Report): Report? {
        var result: Report? = null
        mutate { list ->
            list.map {
                if (it.id == id) transform(it).also { r -> result = r } else it
            }
        }
        return result
    }

    suspend fun delete(id: String) = mutate { list -> list.filterNot { it.id == id } }

    suspend fun addAll(reports: List<Report>) = mutate { list -> list + reports }

    suspend fun clear() = mutate { emptyList() }

    // --- Borrador en curso (sobrevive al cierre de la app) ---

    fun loadDraft(): Report? = try {
        if (draftFile.exists()) ReportJson.fromJson(JSONObject(draftFile.readText())) else null
    } catch (e: Exception) {
        null
    }

    suspend fun saveDraft(draft: Report?) = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (draft == null) draftFile.delete()
            else writeAtomic(draftFile, ReportJson.toJson(draft).toString())
        }
    }

    private fun writeAtomic(target: File, content: String) {
        val tmp = File(target.parentFile, target.name + ".tmp")
        try {
            tmp.writeText(content)
            if (!tmp.renameTo(target)) {
                target.delete()
                tmp.renameTo(target)
            }
        } catch (e: Exception) {
            Log.e("ReportRepository", "No se pudo escribir ${target.name}", e)
        }
    }
}
