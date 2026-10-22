package com.example.vitairosapp.ui.screens.queue

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.ReportStatus
import com.example.vitairosapp.ui.components.ActionRow
import com.example.vitairosapp.ui.components.ButtonStyle
import com.example.vitairosapp.ui.components.EmptyState
import com.example.vitairosapp.ui.components.GREEN_DOT
import com.example.vitairosapp.ui.components.Hint
import com.example.vitairosapp.ui.components.Notice
import com.example.vitairosapp.ui.components.NoticeKind
import com.example.vitairosapp.ui.components.RED_DOT
import com.example.vitairosapp.ui.components.ReportCard
import com.example.vitairosapp.ui.components.ScreenColumn
import com.example.vitairosapp.ui.components.VButton
import com.example.vitairosapp.ui.theme.Danger
import com.example.vitairosapp.ui.viewmodel.AppViewModel

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
