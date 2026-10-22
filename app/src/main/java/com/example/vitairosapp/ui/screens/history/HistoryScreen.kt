package com.example.vitairosapp.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.ReportStatus
import com.example.vitairosapp.ui.components.EmptyState
import com.example.vitairosapp.ui.components.FieldLabel
import com.example.vitairosapp.ui.components.ReportCard
import com.example.vitairosapp.ui.components.ScreenColumn
import com.example.vitairosapp.ui.theme.Copper
import com.example.vitairosapp.ui.theme.FieldBorder
import com.example.vitairosapp.ui.theme.Muted
import com.example.vitairosapp.ui.viewmodel.AppViewModel

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
