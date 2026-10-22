package com.example.vitairosapp.ui.screens.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.data.catalog.Catalogs
import com.example.vitairosapp.data.model.ReportStatus
import com.example.vitairosapp.ui.components.ButtonStyle
import com.example.vitairosapp.ui.components.FieldLabel
import com.example.vitairosapp.ui.components.Hint
import com.example.vitairosapp.ui.components.Notice
import com.example.vitairosapp.ui.components.ScreenColumn
import com.example.vitairosapp.ui.components.SelectField
import com.example.vitairosapp.ui.components.StepIndicator
import com.example.vitairosapp.ui.components.VButton
import com.example.vitairosapp.ui.components.VCard
import com.example.vitairosapp.ui.components.vitairosFieldColors
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.theme.Muted
import com.example.vitairosapp.ui.viewmodel.AppViewModel
import com.example.vitairosapp.util.formatDate

@Composable
fun ReportFormScreen(vm: AppViewModel) {
    val draft = vm.draft ?: return MissingDraft(vm)
    ScreenColumn {
        StepIndicator(0)
        if (vm.isEditing) {
            Notice("Editando un reporte guardado. El ID, la fecha de creación y la trazabilidad no se modifican.")
        } else {
            Notice("Catálogos provisionales para demostración. VITAIROS entregará o validará los definitivos.")
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldLabel("Tipo de reporte *")
            SelectField(
                value = draft.type,
                options = Catalogs.TIPOS,
                placeholder = "Seleccionar tipo",
                onSelect = { v -> vm.updateDraft { it.copy(type = v) } }
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldLabel("Proceso asociado *")
            SelectField(
                value = draft.process,
                options = Catalogs.PROCESOS,
                placeholder = "Seleccionar proceso",
                onSelect = { v -> vm.updateDraft { it.copy(process = v) } }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel("Área / faena")
                OutlinedTextField(
                    value = draft.area,
                    onValueChange = { v -> vm.updateDraft { it.copy(area = v.take(80)) } },
                    placeholder = { Text("Si corresponde") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = vitairosFieldColors(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel("Turno")
                SelectField(
                    value = draft.shift.ifBlank { NO_APLICA },
                    options = listOf(NO_APLICA) + Catalogs.TURNOS,
                    placeholder = NO_APLICA,
                    onSelect = { v -> vm.updateDraft { it.copy(shift = if (v == NO_APLICA) "" else v) } }
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldLabel("Observaciones")
            OutlinedTextField(
                value = draft.notes,
                onValueChange = { v -> vm.updateDraft { it.copy(notes = v.take(MAX_NOTES)) } },
                placeholder = { Text("Descripción breve del reporte") },
                minLines = 2,
                shape = RoundedCornerShape(14.dp),
                colors = vitairosFieldColors(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
            )
            Hint("${draft.notes.length}/$MAX_NOTES")
        }

        VCard {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("ID automático: ") }
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append(draft.id) }
                },
                color = Ink, fontSize = 13.sp
            )
            Text("Usuario: ${draft.user} · ${formatDate(draft.createdAt)}", color = Muted, fontSize = 12.5.sp)
        }

        VButton("Continuar a adjuntos →", vm::goToAttachments, Modifier.fillMaxWidth())
        VButton(
            "Guardar borrador",
            { vm.saveDraft(ReportStatus.BORRADOR) },
            Modifier.fillMaxWidth(),
            ButtonStyle.Secondary
        )
    }
}
