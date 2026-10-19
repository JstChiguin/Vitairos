package com.example.vitairosapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.R
import com.example.vitairosapp.data.Catalogs
import com.example.vitairosapp.data.Report
import com.example.vitairosapp.data.ReportStatus
import com.example.vitairosapp.ui.AppViewModel
import com.example.vitairosapp.ui.Screen
import com.example.vitairosapp.ui.components.ActionRow
import com.example.vitairosapp.ui.components.AttachmentPreviewDialog
import com.example.vitairosapp.ui.components.AttachmentRow
import com.example.vitairosapp.ui.components.ButtonStyle
import com.example.vitairosapp.ui.components.Divider
import com.example.vitairosapp.ui.components.EmptyState
import com.example.vitairosapp.ui.components.FieldLabel
import com.example.vitairosapp.ui.components.Hint
import com.example.vitairosapp.ui.components.InfoLine
import com.example.vitairosapp.ui.components.MetricTile
import com.example.vitairosapp.ui.components.Notice
import com.example.vitairosapp.ui.components.NoticeKind
import com.example.vitairosapp.ui.components.ReportCard
import com.example.vitairosapp.ui.components.ScreenColumn
import com.example.vitairosapp.ui.components.SectionTitle
import com.example.vitairosapp.ui.components.SelectField
import com.example.vitairosapp.ui.components.StatusPill
import com.example.vitairosapp.ui.components.VButton
import com.example.vitairosapp.ui.components.VCard
import com.example.vitairosapp.ui.formatDate
import com.example.vitairosapp.data.Attachment
import com.example.vitairosapp.ui.theme.Copper
import com.example.vitairosapp.ui.theme.FieldBorder
import com.example.vitairosapp.ui.theme.HeaderMuted
import com.example.vitairosapp.ui.theme.HeroGreen
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.theme.Line
import com.example.vitairosapp.ui.theme.Mist
import com.example.vitairosapp.ui.theme.Muted
import com.example.vitairosapp.ui.theme.Night

private const val GREEN_DOT = "🟢"
private const val RED_DOT = "🔴"

// ---------------- Ingreso ----------------

@Composable
fun LoginScreen(vm: AppViewModel) {
    var selected by rememberSaveable { mutableStateOf(Catalogs.USUARIOS_DEMO.first()) }
    ScreenColumn {
        Spacer(Modifier.height(16.dp))
        Image(
            painterResource(R.drawable.vitairos_logo),
            contentDescription = "Isotipo VITAIROS",
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.CenterHorizontally)
        )
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("VITAIROS Industrial AI", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                "Captura de reportes operacionales · MVP académico",
                color = Muted, fontSize = 12.5.sp, textAlign = TextAlign.Center
            )
        }
        VCard {
            Text("Ingresar a la demostración", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            FieldLabel("Usuario ficticio")
            SelectField(
                value = selected,
                options = Catalogs.USUARIOS_DEMO,
                placeholder = "Seleccionar usuario",
                onSelect = { selected = it }
            )
            Hint("Ingreso local simulado. No se utilizan credenciales corporativas.")
            VButton(
                if (vm.loggingIn) "Validando…" else "Ingresar →",
                onClick = { vm.login(selected) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !vm.loggingIn
            )
        }
        Spacer(Modifier.height(8.dp))
        Notice("Prototipo académico DSY1105. Datos, procesos y respuestas de envío son ficticios.")
    }
}

// ---------------- Panel principal ----------------

@Composable
fun HomeScreen(vm: AppViewModel, reports: List<Report>) {
    ScreenColumn {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(Night, HeroGreen)))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("OPERACIÓN DE TERRENO", color = HeaderMuted, fontSize = 12.sp, letterSpacing = 1.sp)
            Text("Hola, ${vm.user ?: "Operador"}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Captura documental con trazabilidad, incluso sin conexión.", color = HeaderMuted, fontSize = 12.5.sp)
            Spacer(Modifier.height(6.dp))
            VButton("＋ Crear nuevo reporte", vm::startNewReport, Modifier.fillMaxWidth(), ButtonStyle.Copper)
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile("${reports.size}", "Total", Modifier.weight(1f))
            MetricTile("${reports.count { it.status.isPending }}", "Pendientes", Modifier.weight(1f))
            MetricTile("${reports.count { it.status == ReportStatus.SINCRONIZADO }}", "Sincronizados", Modifier.weight(1f))
        }

        SectionTitle("Conectividad de demostración", Modifier.padding(top = 8.dp))
        VCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (vm.online) "$GREEN_DOT Conectado" else "$RED_DOT Sin conexión",
                        color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold
                    )
                    Text("Control de red simulado", color = Muted, fontSize = 11.5.sp)
                }
                VButton(
                    if (vm.online) "Desconectar" else "Reconectar",
                    vm::toggleOnline,
                    style = ButtonStyle.Secondary
                )
            }
        }

        SectionTitle("Últimos reportes", Modifier.padding(top = 8.dp))
        val latest = reports.sortedByDescending { it.createdAt }.take(3)
        if (latest.isEmpty()) {
            EmptyState("▧", "Todavía no hay reportes. Crea el primero.")
        } else {
            latest.forEach { report ->
                ReportCard(
                    report, vm.online,
                    onDetail = { vm.openDetail(report.id) },
                    onEdit = { vm.editReport(report.id) },
                    onSend = { vm.send(report.id) }
                )
            }
        }
    }
}

// ---------------- Cola de envíos ----------------

@Composable
fun QueueScreen(vm: AppViewModel, reports: List<Report>) {
    val queue = reports.filter { it.status != ReportStatus.SINCRONIZADO }.sortedByDescending { it.createdAt }
    val sendable = reports.count { it.status == ReportStatus.EN_COLA || it.status == ReportStatus.ERROR }
    ScreenColumn {
        if (vm.online) {
            Notice("$GREEN_DOT Conectividad simulada disponible. Los reportes en cola pueden enviarse.", NoticeKind.Success)
        } else {
            Notice(
                "$RED_DOT Sin conexión simulada. Los reportes permanecen almacenados localmente y se enviarán automáticamente al reconectar.",
                NoticeKind.Danger
            )
        }
        ActionRow {
            VButton(
                if (vm.online) "Simular desconexión" else "Recuperar conexión",
                vm::toggleOnline, Modifier.weight(1f), ButtonStyle.Secondary
            )
            VButton(
                "Enviar pendientes ($sendable)", vm::sendAll, Modifier.weight(1f),
                enabled = vm.online
            )
        }
        Hint(
            "Demostración: envío inicial + 3 reintentos automáticos con pausas de 5, 15 y 30 segundos; " +
                "después queda en Error con reintento manual. Puedes forzar un error en Ajustes."
        )
        if (queue.isEmpty()) {
            EmptyState("✓", "No hay reportes pendientes.")
        } else {
            queue.forEach { report ->
                ReportCard(
                    report, vm.online,
                    onDetail = { vm.openDetail(report.id) },
                    onEdit = { vm.editReport(report.id) },
                    onSend = { vm.send(report.id) }
                )
            }
        }
    }
}

// ---------------- Historial ----------------

@Composable
fun HistoryScreen(vm: AppViewModel, reports: List<Report>) {
    var filter by rememberSaveable { mutableStateOf<ReportStatus?>(null) }
    val visible = reports
        .filter { filter == null || it.status == filter }
        .sortedByDescending { it.createdAt }
    ScreenColumn {
        FieldLabel("Filtrar por estado")
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip("Todos (${reports.size})", filter == null) { filter = null }
            ReportStatus.entries.forEach { status ->
                FilterChip("${status.label} (${reports.count { it.status == status }})", filter == status) {
                    filter = status
                }
            }
        }
        if (visible.isEmpty()) {
            EmptyState("◷", "No hay reportes para este filtro.")
        } else {
            visible.forEach { report ->
                ReportCard(
                    report, vm.online,
                    onDetail = { vm.openDetail(report.id) },
                    onEdit = { vm.editReport(report.id) },
                    onSend = { vm.send(report.id) }
                )
            }
        }
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Copper else Color.White)
            .border(1.dp, if (selected) Copper else FieldBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        color = if (selected) Color.White else Muted,
        fontSize = 15.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
    )
}

// ---------------- Detalle y trazabilidad ----------------

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

// ---------------- Configuración demo ----------------

@Composable
fun SettingsScreen(vm: AppViewModel) {
    ScreenColumn {
        VCard {
            Text("Controles para la presentación", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Estos controles no corresponden a un sistema real de VITAIROS.", color = Muted, fontSize = 13.5.sp, lineHeight = 21.sp)
            SettingSwitch("Conexión simulada disponible", vm.online, vm::setOnline)
            Divider()
            SettingSwitch("Simular error en el próximo envío (agota los reintentos)", vm.failNext, vm::updateFailNext)
            Divider()
            Spacer(Modifier.height(2.dp))
            VButton("Cargar reportes de ejemplo", vm::requestLoadDemo, Modifier.fillMaxWidth(), ButtonStyle.Secondary)
            VButton("Restablecer demostración", vm::requestReset, Modifier.fillMaxWidth(), ButtonStyle.Danger)
        }
        VCard {
            Text("Sesión y dispositivo", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            InfoLine("Usuario", vm.user ?: "—")
            InfoLine("ID de instalación", vm.installId)
            InfoLine("Almacenamiento", "Privado de la aplicación")
        }
        Notice(
            "La información se conserva en el almacenamiento privado de esta app. El envío es simulado: " +
                "no se transmite a servidores. No usar información real o confidencial."
        )
        VButton("Cerrar sesión simulada", vm::logout, Modifier.fillMaxWidth(), ButtonStyle.Ghost)
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f), color = Ink, fontSize = 14.sp, lineHeight = 22.sp)
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Copper,
                checkedBorderColor = Copper,
                uncheckedThumbColor = Line,
                uncheckedTrackColor = Mist,
                uncheckedBorderColor = FieldBorder,
            )
        )
    }
}

