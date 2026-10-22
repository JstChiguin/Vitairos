package com.example.vitairosapp.ui.screens.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.data.model.Attachment
import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.ReportStatus
import com.example.vitairosapp.ui.components.ActionRow
import com.example.vitairosapp.ui.components.AttachmentPreviewDialog
import com.example.vitairosapp.ui.components.AttachmentRow
import com.example.vitairosapp.ui.components.ButtonStyle
import com.example.vitairosapp.ui.components.Divider
import com.example.vitairosapp.ui.components.EmptyState
import com.example.vitairosapp.ui.components.Hint
import com.example.vitairosapp.ui.components.InfoLine
import com.example.vitairosapp.ui.components.ScreenColumn
import com.example.vitairosapp.ui.components.SectionTitle
import com.example.vitairosapp.ui.components.StatusPill
import com.example.vitairosapp.ui.components.VButton
import com.example.vitairosapp.ui.components.VCard
import com.example.vitairosapp.ui.navigation.Screen
import com.example.vitairosapp.ui.theme.Danger
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.viewmodel.AppViewModel
import com.example.vitairosapp.util.formatDate

@Composable
fun DetailScreen(vm: AppViewModel, reports: List<Report>) {
    val report = reports.firstOrNull { it.id == vm.detailId }
    var preview by rememberSaveable { mutableStateOf<String?>(null) }
    ScreenColumn {
        if (report == null) {
            EmptyState("☷", "Selecciona un reporte en Historial.")
            VButton("Ir al historial", { vm.navigate(Screen.HISTORY) }, Modifier.fillMaxWidth(), ButtonStyle.Secondary)
            return@ScreenColumn
        }

        VCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Estado actual", Modifier.weight(1f), color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                StatusPill(report.status)
            }
            Text(report.id, color = Ink, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
            Divider()
            InfoLine("Tipo", report.type.ifBlank { "Sin tipo" })
            InfoLine("Proceso", report.process.ifBlank { "Sin proceso" })
            InfoLine("Área / faena", report.area.ifBlank { "No aplica" })
            InfoLine("Turno", report.shift.ifBlank { "No aplica" })
            InfoLine("Usuario", report.user)
            InfoLine("Fecha de creación", formatDate(report.createdAt))
            InfoLine("Versión", "${report.version} · Intentos: ${report.attempts}")
            InfoLine("Instalación", report.installId)
            if (report.syncedAt != null) InfoLine("Sincronizado", formatDate(report.syncedAt))
            report.receipt?.let { InfoLine("Comprobante", it) }
            InfoLine("Observaciones", report.notes.ifBlank { "Sin observaciones" })
        }

        SectionTitle("Evidencia documental", Modifier.padding(top = 8.dp))
        if (report.attachments.isEmpty()) {
            Hint("Sin adjuntos registrados.")
        } else {
            report.attachments.forEach { a ->
                AttachmentRow(a, onView = { preview = a.id })
            }
        }

        SectionTitle("Trazabilidad", Modifier.padding(top = 8.dp))
        VCard {
            val log = report.log.asReversed()
            if (log.isEmpty()) Hint("Sin eventos")
            log.forEachIndexed { i, entry ->
                if (i > 0) Divider()
                Column(Modifier.padding(vertical = 4.dp)) {
                    Text(formatDate(entry.at), color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(entry.message, color = Ink, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
        }

        val canEdit = report.status.isEditable
        val canSend = report.status == ReportStatus.EN_COLA || report.status == ReportStatus.ERROR
        val canDelete = report.status.isDeletable
        if (canEdit || canSend || canDelete) {
            ActionRow {
                if (canEdit) VButton("Editar", { vm.editReport(report.id) }, Modifier.weight(1f), ButtonStyle.Secondary)
                if (canSend) VButton(
                    if (report.status == ReportStatus.ERROR) "Reintentar" else "Enviar",
                    { vm.send(report.id) }, Modifier.weight(1f)
                )
                if (canDelete) VButton("Eliminar", { vm.requestDelete(report.id) }, Modifier.weight(1f), ButtonStyle.Danger)
            }
        }
        VButton("← Volver al historial", { vm.navigate(Screen.HISTORY) }, Modifier.fillMaxWidth(), ButtonStyle.Ghost)
    }

    val previewAttachment: Attachment? = report?.attachments?.firstOrNull { it.id == preview }
    previewAttachment?.let { AttachmentPreviewDialog(it) { preview = null } }
}
