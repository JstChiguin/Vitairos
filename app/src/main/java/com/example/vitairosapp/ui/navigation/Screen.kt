package com.example.vitairosapp.ui.navigation

enum class Screen(val title: String) {
    LOGIN("Ingreso simulado"),
    HOME("Panel principal"),
    FORM("Nuevo reporte"),
    FILES("Adjuntos y vista previa"),
    REVIEW("Revisión y confirmación"),
    QUEUE("Cola de envíos"),
    HISTORY("Historial"),
    DETAIL("Detalle y trazabilidad"),
    SETTINGS("Configuración demo");

    val isReportFlow: Boolean get() = this == FORM || this == FILES || this == REVIEW
}
