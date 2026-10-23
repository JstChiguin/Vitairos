package com.example.vitairosapp.ui.screens.report

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.data.model.ReportStatus
import com.example.vitairosapp.ui.components.ActionRow
import com.example.vitairosapp.ui.components.AttachmentPreviewDialog
import com.example.vitairosapp.ui.components.AttachmentRow
import com.example.vitairosapp.ui.components.ButtonStyle
import com.example.vitairosapp.ui.components.Divider
import com.example.vitairosapp.ui.components.InfoLine
import com.example.vitairosapp.ui.components.Notice
import com.example.vitairosapp.ui.components.NoticeKind
import com.example.vitairosapp.ui.components.ScreenColumn
import com.example.vitairosapp.ui.components.StatusPill
import com.example.vitairosapp.ui.components.StepIndicator
import com.example.vitairosapp.ui.components.VButton
import com.example.vitairosapp.ui.components.VCard
import com.example.vitairosapp.ui.theme.Copper
import com.example.vitairosapp.ui.theme.Danger
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.theme.Muted
import com.example.vitairosapp.ui.viewmodel.AppViewModel
import com.example.vitairosapp.util.formatDate

@Composable
fun ReviewScreen(vm: AppViewModel) {
    val draft = vm.draft ?: return MissingDraft(vm)
    var preview by rememberSaveable { mutableStateOf<String?>(null) }
    ScreenColumn {
        StepIndicator(2)
        VCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Resumen del reporte", Modifier.weight(1f), color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                StatusPill(draft.status)
            }
            Divider()
            InfoLine("Tipo", draft.type)
            InfoLine("Proceso", draft.process)
            InfoLine("Área", draft.area.ifBlank { NO_APLICA })
            InfoLine("Turno", draft.shift.ifBlank { NO_APLICA })
            InfoLine("Usuario", draft.user)
            InfoLine("Creación", formatDate(draft.createdAt))
            InfoLine("Observaciones", draft.notes.ifBlank { "Sin observaciones" })
            InfoLine("Adjuntos", "${draft.attachments.size}")
        }

        draft.attachments.forEach { a -> AttachmentRow(a, onView = { preview = a.id }) }

        Row(
            Modifier.clickable { vm.qualityConfirmed = !vm.qualityConfirmed },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = vm.qualityConfirmed,
                onCheckedChange = { vm.qualityConfirmed = it },
                colors = CheckboxDefaults.colors(checkedColor = Copper, uncheckedColor = Muted)
            )
            Text(
                "Confirmo que los documentos corresponden al reporte y son legibles.",
                color = Ink, fontSize = 15.sp, lineHeight = 22.sp
            )
        }

        if (!vm.online) {
            Notice(
                "Sin conexión: al enviar, el reporte quedará guardado y pendiente en la cola. " +
                    "Se enviará automáticamente al recuperar la conexión.",
                NoticeKind.Danger
            )
        }

        ActionRow {
            VButton("← Corregir", vm::backToAttachments, Modifier.weight(1f), ButtonStyle.Secondary)
            VButton(
                if (vm.online) "Enviar ahora" else "Guardar pendiente",
                {
                    if (vm.requireQualityConfirmation()) vm.saveDraft(ReportStatus.EN_COLA, sendNow = true)
                },
                Modifier.weight(1f),
                ButtonStyle.Copper
            )
        }
        VButton(
            "Guardar en cola (enviar después)",
            { if (vm.requireQualityConfirmation()) vm.saveDraft(ReportStatus.EN_COLA) },
            Modifier.fillMaxWidth(),
            ButtonStyle.Secondary
        )
        VButton(
            "Guardar como borrador",
            { vm.saveDraft(ReportStatus.BORRADOR) },
            Modifier.fillMaxWidth(),
            ButtonStyle.Ghost
        )
        Spacer(Modifier.height(4.dp))
    }

    draft.attachments.firstOrNull { it.id == preview }?.let {
        AttachmentPreviewDialog(it) { preview = null }
    }
}
