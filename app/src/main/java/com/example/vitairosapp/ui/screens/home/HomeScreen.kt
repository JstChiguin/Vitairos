package com.example.vitairosapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.ReportStatus
import com.example.vitairosapp.ui.components.ButtonStyle
import com.example.vitairosapp.ui.components.EmptyState
import com.example.vitairosapp.ui.components.GREEN_DOT
import com.example.vitairosapp.ui.components.MetricTile
import com.example.vitairosapp.ui.components.RED_DOT
import com.example.vitairosapp.ui.components.ReportCard
import com.example.vitairosapp.ui.components.ScreenColumn
import com.example.vitairosapp.ui.components.SectionTitle
import com.example.vitairosapp.ui.components.VButton
import com.example.vitairosapp.ui.components.VCard
import com.example.vitairosapp.ui.theme.Copper
import com.example.vitairosapp.ui.theme.HeaderMuted
import com.example.vitairosapp.ui.theme.HeroGreen
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.theme.Muted
import com.example.vitairosapp.ui.theme.Night
import com.example.vitairosapp.ui.viewmodel.AppViewModel

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
