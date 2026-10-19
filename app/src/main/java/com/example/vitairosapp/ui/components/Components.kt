package com.example.vitairosapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.data.Report
import com.example.vitairosapp.data.ReportStatus
import com.example.vitairosapp.ui.formatDate
import com.example.vitairosapp.ui.formatTime
import com.example.vitairosapp.ui.theme.Bone
import com.example.vitairosapp.ui.theme.Copper
import com.example.vitairosapp.ui.theme.CopperDark
import com.example.vitairosapp.ui.theme.Danger
import com.example.vitairosapp.ui.theme.DangerBg
import com.example.vitairosapp.ui.theme.Deep
import com.example.vitairosapp.ui.theme.DisabledBg
import com.example.vitairosapp.ui.theme.FieldBorder
import com.example.vitairosapp.ui.theme.InfoNoticeBg
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.theme.Line
import com.example.vitairosapp.ui.theme.Mist
import com.example.vitairosapp.ui.theme.Muted
import com.example.vitairosapp.ui.theme.OnCopper
import com.example.vitairosapp.ui.theme.SuccessBg
import com.example.vitairosapp.ui.theme.SuccessNoticeBg
import com.example.vitairosapp.ui.theme.SuccessText
import com.example.vitairosapp.ui.theme.WarnBg
import com.example.vitairosapp.ui.theme.WarnText

enum class ButtonStyle { Primary, Secondary, Copper, Ghost, Danger }

enum class NoticeKind { Info, Success, Danger }

@Composable
fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@Composable
fun VCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, Line, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = content
    )
}

@Composable
fun VButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    enabled: Boolean = true,
) {
    val (bg, fg) = when (style) {
        ButtonStyle.Primary -> Deep to Color.White
        ButtonStyle.Secondary -> Mist to Deep
        ButtonStyle.Copper -> Copper to OnCopper
        ButtonStyle.Ghost -> Color.White to Deep
        ButtonStyle.Danger -> DangerBg to Danger
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            contentColor = fg,
            disabledContainerColor = if (style == ButtonStyle.Primary) DisabledBg else bg.copy(alpha = 0.5f),
            disabledContentColor = if (style == ButtonStyle.Primary) Bone else fg.copy(alpha = 0.5f),
        ),
        border = if (style == ButtonStyle.Ghost) BorderStroke(1.dp, Line) else null,
        elevation = null,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

/** Fila de botones que se reparten el ancho. */
@Composable
fun ActionRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun FieldLabel(text: String) {
    Text(text, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun Hint(text: String, color: Color = Muted) {
    Text(text, color = color, fontSize = 11.5.sp, lineHeight = 17.sp, letterSpacing = 0.1.sp)
}

@Composable
fun InfoLine(label: String, value: String) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("$label:") }
            append(" ")
            append(value)
        },
        color = Ink, fontSize = 13.sp, lineHeight = 18.sp
    )
}

@Composable
fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Line)
    )
}

@Composable
fun Notice(text: String, kind: NoticeKind = NoticeKind.Info) {
    val (bg, bar) = when (kind) {
        NoticeKind.Info -> InfoNoticeBg to Copper
        NoticeKind.Success -> SuccessNoticeBg to SuccessText
        NoticeKind.Danger -> DangerBg to Danger
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
    ) {
        Box(
            Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(bar)
        )
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            color = Ink, fontSize = 13.sp, lineHeight = 19.sp
        )
    }
}

@Composable
fun MetricTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Muted, fontSize = 11.sp)
    }
}

@Composable
fun StatusPill(status: ReportStatus) {
    val symbol = when (status) {
        ReportStatus.BORRADOR -> "◌"
        ReportStatus.ENVIANDO -> "↻"
        ReportStatus.EN_COLA -> "◷"
        ReportStatus.SINCRONIZADO -> "✓"
        ReportStatus.ERROR -> "!"
    }
    val (bg, fg) = when (status) {
        ReportStatus.BORRADOR -> Mist to Deep
        ReportStatus.EN_COLA -> WarnBg to WarnText
        ReportStatus.ENVIANDO -> InfoNoticeBg to CopperDark
        ReportStatus.SINCRONIZADO -> SuccessBg to SuccessText
        ReportStatus.ERROR -> DangerBg to Danger
    }
    Text(
        "$symbol ${status.label}",
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = fg, fontSize = 12.5.sp, fontWeight = FontWeight.Bold
    )
}

@Composable
fun EmptyState(symbol: String, text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(symbol, color = Muted, fontSize = 36.sp)
        Text(text, color = Muted, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

/** Barra de progreso del flujo de creación (3 pasos). */
@Composable
fun StepIndicator(current: Int, total: Int = 3) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (i <= current) Copper else Line)
            )
        }
    }
}

fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

@Composable
fun vitairosFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Copper,
    unfocusedBorderColor = FieldBorder,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = Copper,
    focusedTextColor = Ink,
    unfocusedTextColor = Ink,
    focusedPlaceholderColor = Muted,
    unfocusedPlaceholderColor = Muted,
)

@Composable
fun SelectField(
    value: String,
    options: List<String>,
    placeholder: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            placeholder = { Text(placeholder) },
            trailingIcon = { Text("▾", color = Muted, fontSize = 12.sp) },
            shape = RoundedCornerShape(14.dp),
            colors = vitairosFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        // Capa transparente para abrir el menú con un toque en todo el campo
        Box(
            Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(14.dp))
                .clickableNoRipple { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color.White
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = Ink, fontSize = 15.sp) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}

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
