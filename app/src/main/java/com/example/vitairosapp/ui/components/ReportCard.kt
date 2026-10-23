package com.example.vitairosapp.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.ReportStatus
import com.example.vitairosapp.ui.theme.CopperDark
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.theme.Muted
import com.example.vitairosapp.util.formatDate
import com.example.vitairosapp.util.formatTime

@Composable
fun ReportCard(
    report: Report,
    online: Boolean,
    onDetail: () -> Unit,
    onEdit: () -> Unit,
    onSend: () -> Unit,
) {
    VCard {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                report.type.ifBlank { "Reporte sin tipo" },
                modifier = Modifier.weight(1f),
                color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            StatusPill(report.status)
        }
        Text(report.id, color = Muted, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
        Text(
            report.process.ifBlank { "Sin proceso" } + " · " + formatDate(report.createdAt),
            color = Muted, fontSize = 13.sp
        )
        Text(
            "Adjuntos: ${report.attachments.size} · Intentos: ${report.attempts} · v${report.version}",
            color = Muted, fontSize = 13.sp
        )
        val retryAt = report.nextRetryAt
        if (report.status == ReportStatus.ERROR && retryAt != null) {
            Text("Reintento automático a las ${formatTime(retryAt)}", color = CopperDark, fontSize = 12.5.sp)
        } else if (report.status == ReportStatus.EN_COLA && !online) {
            Text("Pendiente: se enviará al recuperar la conexión", color = CopperDark, fontSize = 12.5.sp)
        }
        Spacer(Modifier.height(2.dp))
        ActionRow {
            VButton("Ver detalle", onDetail, Modifier.weight(1f), ButtonStyle.Secondary)
            if (report.status.isEditable) {
                VButton("Editar", onEdit, Modifier.weight(1f), ButtonStyle.Ghost)
            }
            if (report.status == ReportStatus.EN_COLA) {
                VButton("Enviar", onSend, Modifier.weight(1f), ButtonStyle.Primary)
            } else if (report.status == ReportStatus.ERROR) {
                VButton("Reintentar", onSend, Modifier.weight(1f), ButtonStyle.Primary)
            }
        }
    }
}
