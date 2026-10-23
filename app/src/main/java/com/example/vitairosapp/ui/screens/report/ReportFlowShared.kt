package com.example.vitairosapp.ui.screens.report

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.vitairosapp.ui.components.EmptyState
import com.example.vitairosapp.ui.components.ScreenColumn
import com.example.vitairosapp.ui.components.VButton
import com.example.vitairosapp.ui.viewmodel.AppViewModel

internal const val NO_APLICA = "No aplica"

internal const val MAX_NOTES = 600

@Composable
internal fun MissingDraft(vm: AppViewModel) {
    ScreenColumn {
        EmptyState("＋", "No hay un reporte en curso.")
        VButton("Crear nuevo reporte", vm::startNewReport, Modifier.fillMaxWidth())
    }
}
