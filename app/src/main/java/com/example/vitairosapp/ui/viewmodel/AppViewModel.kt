package com.example.vitairosapp.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitairosapp.data.catalog.Catalogs
import com.example.vitairosapp.data.local.AppPreferences
import com.example.vitairosapp.data.local.AttachmentStore
import com.example.vitairosapp.data.model.Attachment
import com.example.vitairosapp.data.model.AttachmentSource
import com.example.vitairosapp.data.model.ImportResult
import com.example.vitairosapp.data.model.LogEntry
import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.ReportStatus
import com.example.vitairosapp.data.model.SyncResult
import com.example.vitairosapp.data.remote.MockSyncApi
import com.example.vitairosapp.data.local.db.VitairosDatabase
import com.example.vitairosapp.data.repository.ReportRepository
import com.example.vitairosapp.ui.navigation.Screen
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = AppPreferences(app)
    private val repository = ReportRepository(VitairosDatabase.get(app).reportDao(), app.filesDir)
    private val store = AttachmentStore(app)
    private val api = MockSyncApi()

    val reports: StateFlow<List<Report>> = repository.reports
    val installId: String = prefs.installId

    var user by mutableStateOf(prefs.user)
        private set
    private val onlineState = mutableStateOf(prefs.online)
    val online: Boolean get() = onlineState.value
    var failNext by mutableStateOf(prefs.failNext)
        private set
    var screen by mutableStateOf(if (prefs.user == null) Screen.LOGIN else Screen.HOME)
        private set

    /** Reporte en creación/edición (aún no confirmado). */
    var draft by mutableStateOf<Report?>(null)
        private set
    var qualityConfirmed by mutableStateOf(false)
    var importing by mutableStateOf(false)
        private set
    var loggingIn by mutableStateOf(false)
        private set
    var confirmation by mutableStateOf<Confirmation?>(null)
        private set
    var detailId by mutableStateOf<String?>(null)
        private set

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages

    private val syncJobs = mutableMapOf<String, Job>()
    private val retrySignals = mutableMapOf<String, Channel<Unit>>()

    /** True si el borrador corresponde a un reporte ya guardado (edición). */
    val isEditing: Boolean
        get() = draft?.let { d -> reports.value.any { it.id == d.id } } == true

    init {
        val savedDraft = repository.loadDraft()
        savedDraft?.let { saved ->
            draft = saved
            if (user != null) {
                screen = prefs.draftStep?.let { runCatching { Screen.valueOf(it) }.getOrNull() }
                    ?.takeIf { it.isReportFlow } ?: Screen.FORM
                notify("Se recuperó el reporte que estabas creando")
            }
        }
        // Se calcula aquí (no en el hilo de fondo): el estado Compose escrito durante la
        // creación del ViewModel aún no es visible desde otros hilos
        val referenced = reports.value.map { it.id }.toSet() + listOfNotNull(savedDraft?.id)
        viewModelScope.launch(Dispatchers.IO) { store.pruneOrphans(referenced) }
        // Reportes que quedaron en cola se envían al abrir la app si hay conexión
        if (online && user != null) {
            reports.value.filter { it.status == ReportStatus.EN_COLA }.forEach { send(it.id) }
        }
    }

    private fun notify(message: String) {
        _messages.tryEmit(message)
    }

    // ---------------- Sesión ----------------

    fun login(name: String) {
        if (loggingIn) return
        loggingIn = true
        viewModelScope.launch {
            delay(600) // validación simulada
            prefs.user = name
            user = name
            loggingIn = false
            screen = if (draft != null) Screen.FORM else Screen.HOME
            notify("Sesión ficticia iniciada como $name")
        }
    }

    fun logout() {
        prefs.user = null
        user = null
        screen = Screen.LOGIN
        notify("Sesión simulada cerrada")
    }

    // ---------------- Navegación ----------------

    fun navigate(target: Screen) {
        if (target == screen) return
        if (screen.isReportFlow && draft != null && !target.isReportFlow) {
            confirmation = Confirmation.LeaveDraft(target)
            return
        }
        if (target == Screen.FORM) {
            startNewReport()
            return
        }
        screen = target
    }

    private fun goTo(target: Screen) {
        screen = target
        if (target.isReportFlow) prefs.draftStep = target.name
    }

    fun openDetail(id: String) {
        detailId = id
        screen = Screen.DETAIL
    }

    fun startNewReport() {
        val current = draft
        if (current != null) {
            if (!screen.isReportFlow) notify("Retomando el reporte en curso")
            goTo(prefs.draftStep?.let { runCatching { Screen.valueOf(it) }.getOrNull() }
                ?.takeIf { it.isReportFlow } ?: Screen.FORM)
            return
        }
        draft = Report(id = Report.newId(), user = user.orEmpty(), installId = installId)
        qualityConfirmed = false
        persistDraft()
        goTo(Screen.FORM)
    }

    fun editReport(id: String) {
        val report = repository.find(id) ?: return
        val current = draft
        if (current != null && current.id != id) {
            notify("Guarda o descarta el reporte en curso antes de editar otro")
            return
        }
        if (!report.status.isEditable || syncJobs[id]?.isActive == true) {
            notify("Este reporte ya no se puede editar")
            return
        }
        draft = report
        qualityConfirmed = false
        persistDraft()
        goTo(Screen.FORM)
    }

    fun updateDraft(transform: (Report) -> Report) {
        draft = draft?.let(transform)
        persistDraft()
    }

    private fun persistDraft() {
        val snapshot = draft
        viewModelScope.launch { repository.saveDraft(snapshot) }
    }

    fun goToAttachments() {
        val d = draft ?: return
        if (d.type.isBlank() || d.process.isBlank()) {
            notify("Selecciona tipo de reporte y proceso asociado")
            return
        }
        goTo(Screen.FILES)
    }

    fun backToForm() = goTo(Screen.FORM)

    fun goToReview() {
        val d = draft ?: return
        if (d.attachments.isEmpty()) {
            notify("Agrega al menos un documento")
            return
        }
        goTo(Screen.REVIEW)
    }

    fun backToAttachments() = goTo(Screen.FILES)

    // ---------------- Adjuntos ----------------

    fun canAddAttachment(): Boolean {
        val count = draft?.attachments?.size ?: 0
        if (count >= AppPreferences.MAX_ATTACHMENTS) {
            notify("Máximo ${AppPreferences.MAX_ATTACHMENTS} archivos por reporte")
            return false
        }
        return true
    }

    fun remainingSlots(): Int = AppPreferences.MAX_ATTACHMENTS - (draft?.attachments?.size ?: 0)

    fun importAttachments(uris: List<Uri>, source: AttachmentSource) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            for (uri in uris) {
                if (!canAddAttachment()) break
                importOne(uri, source)
            }
        }
    }

    fun onCameraResult(uri: Uri, success: Boolean) {
        viewModelScope.launch {
            if (success) importOne(uri, AttachmentSource.CAMARA)
            else notify("Captura cancelada")
            withContext(Dispatchers.IO) { store.deleteCameraTemp(uri) }
        }
    }

    fun newCameraUri(): Uri = store.newCameraUri()

    private suspend fun importOne(uri: Uri, source: AttachmentSource) {
        val d = draft ?: return
        importing = true
        val result = withContext(Dispatchers.IO) { store.import(uri, d.id, source) }
        importing = false
        when (result) {
            is ImportResult.Rejected -> notify(result.reason)
            is ImportResult.Ok -> addAttachment(result.attachment, result.warning)
        }
    }

    private suspend fun addAttachment(attachment: Attachment, warning: String?) {
        val d = draft ?: return
        if (d.attachments.any { it.sha256 == attachment.sha256 }) {
            withContext(Dispatchers.IO) { attachment.path?.let { java.io.File(it).delete() } }
            notify("Ese documento ya está adjunto en este reporte")
            return
        }
        if (d.attachments.size >= AppPreferences.MAX_ATTACHMENTS) {
            withContext(Dispatchers.IO) { attachment.path?.let { java.io.File(it).delete() } }
            notify("Máximo ${AppPreferences.MAX_ATTACHMENTS} archivos por reporte")
            return
        }
        updateDraft { it.copy(attachments = it.attachments + attachment) }
        qualityConfirmed = false
        notify(
            if (warning != null) "Documento agregado. $warning"
            else "Documento agregado. Revisa su legibilidad en la vista previa."
        )
    }

    fun removeAttachment(attachment: Attachment) {
        updateDraft { d -> d.copy(attachments = d.attachments.filterNot { it.id == attachment.id }) }
        notify("Adjunto quitado")
    }

    // ---------------- Guardado ----------------

    fun requireQualityConfirmation(): Boolean {
        if (!qualityConfirmed) notify("Debes confirmar que los documentos son legibles")
        return qualityConfirmed
    }

    /**
     * Guarda el borrador como reporte. [status] BORRADOR lo deja editable;
     * EN_COLA lo agrega a la cola y, si [sendNow], inicia el envío.
     */
    fun saveDraft(status: ReportStatus, sendNow: Boolean = false, then: Screen? = null) {
        val d = draft ?: return
        if (status == ReportStatus.EN_COLA) {
            if (d.type.isBlank() || d.process.isBlank()) {
                notify("Selecciona tipo de reporte y proceso asociado")
                return
            }
            if (d.attachments.isEmpty()) {
                notify("Se necesita al menos un adjunto")
                return
            }
        }
        viewModelScope.launch {
            val existing = repository.find(d.id)
            var report = d.copy(status = status, nextRetryAt = null, updatedAt = System.currentTimeMillis())
            if (existing != null && existing.attempts > 0) {
                // Corrección de un reporte que ya tuvo intentos de envío
                report = report.copy(version = existing.version + 1)
                report = report.withLog(
                    "Corrección registrada: versión ${report.version} (se conserva el intento anterior)"
                )
            }
            report = report.withLog(
                when {
                    status == ReportStatus.BORRADOR && existing != null -> "Borrador actualizado localmente"
                    status == ReportStatus.BORRADOR -> "Borrador guardado localmente"
                    existing != null -> "Reporte actualizado y guardado en cola de envío"
                    else -> "Reporte guardado localmente en cola de envío"
                }
            )
            repository.upsert(report)
            withContext(Dispatchers.IO) { store.prune(report.id, report.attachments) }
            clearDraftState()

            if (status == ReportStatus.BORRADOR) {
                notify("Borrador guardado en el dispositivo")
                screen = then ?: Screen.HOME
            } else {
                screen = then ?: Screen.QUEUE
                when {
                    !online -> notify("Sin conexión: el reporte quedó pendiente en la cola y se enviará al recuperar la conexión")
                    sendNow -> send(report.id)
                    else -> notify("Reporte agregado a la cola")
                }
            }
        }
    }

    private suspend fun clearDraftState() {
        draft = null
        qualityConfirmed = false
        prefs.draftStep = null
        repository.saveDraft(null)
    }

    fun discardDraft(then: Screen = Screen.HOME) {
        val d = draft ?: return
        viewModelScope.launch {
            val existing = repository.find(d.id)
            withContext(Dispatchers.IO) {
                if (existing != null) store.prune(d.id, existing.attachments)
                else store.deleteReportFiles(d.id)
            }
            clearDraftState()
            screen = then
            notify("Cambios descartados")
        }
    }

    // ---------------- Envío y sincronización ----------------

    fun send(id: String) {
        if (!online) {
            notify("Sin conexión: el reporte permanece pendiente en la cola")
            return
        }
        // Si ya hay un envío esperando el próximo reintento, se adelanta
        if (syncJobs[id]?.isActive == true) {
            retrySignals[id]?.trySend(Unit)
            return
        }
        val signal = Channel<Unit>(Channel.CONFLATED)
        retrySignals[id] = signal
        syncJobs[id] = viewModelScope.launch {
            try {
                runSync(id, signal)
            } finally {
                syncJobs.remove(id)
                retrySignals.remove(id)
            }
        }
    }

    fun sendAll() {
        if (!online) {
            notify("Activa la conexión simulada para enviar")
            return
        }
        val pending = reports.value.filter {
            (it.status == ReportStatus.EN_COLA || it.status == ReportStatus.ERROR) && syncJobs[it.id]?.isActive != true
        }
        if (pending.isEmpty()) {
            notify("No hay reportes en cola")
            return
        }
        notify("Conexión disponible: sincronizando ${pending.size} reporte(s) pendiente(s)")
        pending.forEach { send(it.id) }
    }

    private suspend fun runSync(id: String, signal: Channel<Unit>) {
        val start = repository.update(id) {
            it.copy(status = ReportStatus.ENVIANDO, nextRetryAt = null)
                .withLog("Iniciando sincronización mediante API simulada")
        } ?: return

        val payload = api.buildPayload(start)
        val megabytes = start.attachments.sumOf { it.sizeBytes } / (1024.0 * 1024.0)
        repository.update(id) {
            it.withLog(
                "Payload multipart preparado: metadata (${payload.length()} campos) + " +
                    "${start.attachments.size} adjunto(s), " + String.format(Locale.US, "%.2f MB", megabytes)
            )
        }
        val fail = prefs.failNext

        for (attempt in 1..MAX_ATTEMPTS) {
            val current = repository.update(id) {
                it.copy(status = ReportStatus.ENVIANDO, attempts = it.attempts + 1, nextRetryAt = null)
                    .withLog(
                        if (attempt == 1) "Intento de envío 1 de $MAX_ATTEMPTS"
                        else "Reintento automático ${attempt - 1} de ${MAX_ATTEMPTS - 1}"
                    )
            } ?: return

            when (val result = api.upload(current, { online }, fail)) {
                SyncResult.NoConnection -> {
                    repository.update(id) {
                        it.copy(status = ReportStatus.EN_COLA, nextRetryAt = null)
                            .withLog("Sin conexión durante el envío; queda pendiente hasta recuperar conectividad")
                    }
                    notify("Sin conexión: el reporte quedó pendiente en la cola")
                    return
                }

                is SyncResult.Accepted -> {
                    val now = System.currentTimeMillis()
                    repository.update(id) {
                        it.copy(
                            status = ReportStatus.SINCRONIZADO, syncedAt = now,
                            receipt = result.receipt, nextRetryAt = null
                        ).withLog("${result.message}. Comprobante ${result.receipt} (API mock)")
                    }
                    // Tras la confirmación se liberan los archivos locales (se conserva la metadata)
                    withContext(Dispatchers.IO) { store.deleteReportFiles(id) }
                    repository.update(id) { r ->
                        r.copy(attachments = r.attachments.map { it.copy(path = null) })
                            .withLog("Archivos locales liberados; se conserva metadata e historial")
                    }
                    notify("Reporte sincronizado (simulación)")
                    return
                }

                is SyncResult.Rejected -> {
                    repository.update(id) {
                        it.withLog("Error ${result.code} en intento $attempt: ${result.message}")
                    }
                    if (attempt < MAX_ATTEMPTS) {
                        val wait = BACKOFF_MS[attempt - 1]
                        repository.update(id) {
                            it.copy(status = ReportStatus.ERROR, nextRetryAt = System.currentTimeMillis() + wait)
                                .withLog("Próximo intento automático en ${wait / 1000} segundos")
                        }
                        notify("Error de envío. Reintento automático en ${wait / 1000} s")
                        // Espera la pausa, o se adelanta si el usuario pulsa "Reintentar"
                        withTimeoutOrNull(wait) { signal.receive() }
                        if (!online) {
                            repository.update(id) {
                                it.copy(status = ReportStatus.EN_COLA, nextRetryAt = null)
                                    .withLog("Sin conexión durante el envío; queda pendiente hasta recuperar conectividad")
                            }
                            return
                        }
                    }
                }
            }
        }

        repository.update(id) {
            it.copy(status = ReportStatus.ERROR, nextRetryAt = null)
                .withLog("Reintentos automáticos agotados. Disponible reintento manual")
        }
        if (fail) updateFailNext(false) // el error forzado aplica solo al próximo envío
        notify("Error de envío: disponible reintento manual")
    }

    // ---------------- Conectividad y controles demo ----------------

    fun toggleOnline() = setOnline(!online)

    fun setOnline(value: Boolean) {
        if (value == online) return
        onlineState.value = value
        prefs.online = value
        if (value) {
            notify("Conexión simulada recuperada")
            val pending = reports.value.filter {
                it.status == ReportStatus.EN_COLA && syncJobs[it.id]?.isActive != true
            }
            if (pending.isNotEmpty()) {
                notify("Conexión disponible: sincronizando ${pending.size} reporte(s) pendiente(s)")
                pending.forEach { send(it.id) }
            }
        } else {
            notify("Modo sin conexión simulado: los reportes quedarán pendientes")
        }
    }

    fun updateFailNext(value: Boolean) {
        failNext = value
        prefs.failNext = value
    }

    fun requestDelete(id: String) {
        val report = repository.find(id) ?: return
        if (!report.status.isDeletable) {
            notify("Solo se pueden eliminar reportes en Borrador o En cola")
            return
        }
        confirmation = Confirmation.DeleteReport(id)
    }

    fun requestLoadDemo() {
        confirmation = Confirmation.LoadDemo
    }

    fun requestReset() {
        confirmation = Confirmation.ResetDemo
    }

    fun dismissConfirmation() {
        confirmation = null
    }

    /** Ejecuta la acción confirmada en el diálogo. */
    fun confirm(action: Confirmation) {
        confirmation = null
        when (action) {
            is Confirmation.DeleteReport -> deleteReport(action.id)
            Confirmation.LoadDemo -> loadDemoData()
            Confirmation.ResetDemo -> resetDemo()
            is Confirmation.LeaveDraft -> saveDraft(ReportStatus.BORRADOR, then = action.target)
        }
    }

    fun leaveDiscarding(target: Screen) {
        confirmation = null
        discardDraft(then = target)
    }

    private fun deleteReport(id: String) {
        viewModelScope.launch {
            syncJobs[id]?.cancel()
            repository.delete(id)
            withContext(Dispatchers.IO) { store.deleteReportFiles(id) }
            if (detailId == id) {
                detailId = null
                screen = Screen.HISTORY
            }
            notify("Reporte eliminado")
        }
    }

    private fun loadDemoData() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val statuses = listOf(ReportStatus.BORRADOR, ReportStatus.EN_COLA, ReportStatus.SINCRONIZADO)
            val baseId = Report.newId()
            val demo = statuses.mapIndexed { i, status ->
                val created = now - (i + 1) * 3_600_000L
                val synced = status == ReportStatus.SINCRONIZADO
                Report(
                    id = baseId + i,
                    createdAt = created,
                    user = user.orEmpty(),
                    installId = installId,
                    type = Catalogs.TIPOS[i],
                    process = Catalogs.PROCESOS[i],
                    area = "Área ficticia ${i + 1}",
                    shift = "Día",
                    notes = "Documento y datos sintéticos de demostración.",
                    attachments = listOf(
                        Attachment(
                            id = "demo-$i",
                            name = "ejemplo_ficticio.pdf",
                            mimeType = "application/pdf",
                            sizeBytes = 200,
                            sha256 = "demo-$i",
                            source = AttachmentSource.DEMO,
                            addedAt = created,
                            pages = 1,
                        )
                    ),
                    status = status,
                    attempts = if (synced) 1 else 0,
                    log = buildList {
                        add(LogEntry(created, "Reporte sintético de demostración creado"))
                        if (synced) add(LogEntry(created + 60_000, "Sincronización simulada completada"))
                    },
                    syncedAt = if (synced) created + 60_000 else null,
                    receipt = if (synced) "RCV-DEMO000$i" else null,
                    updatedAt = created,
                )
            }
            repository.addAll(demo)
            notify("Tres reportes ficticios cargados")
        }
    }

    private fun resetDemo() {
        viewModelScope.launch {
            syncJobs.values.forEach { it.cancel() }
            syncJobs.clear()
            repository.clear()
            repository.saveDraft(null)
            withContext(Dispatchers.IO) { store.deleteAll() }
            prefs.resetDemo()
            draft = null
            qualityConfirmed = false
            detailId = null
            user = null
            onlineState.value = prefs.online
            failNext = prefs.failNext
            screen = Screen.LOGIN
            notify("Demostración restablecida")
        }
    }

    companion object {
        const val MAX_ATTEMPTS = 4
        /** Pausas entre reintentos automáticos: 5, 15 y 30 segundos. */
        val BACKOFF_MS = longArrayOf(5_000, 15_000, 30_000)
    }
}
