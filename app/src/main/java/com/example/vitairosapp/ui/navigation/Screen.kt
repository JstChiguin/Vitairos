package com.example.vitairosapp.ui.navigation

enum class Screen(val title: String) {
    LOGIN("Ingreso a la demostración"),
    LOGIN_OPERATIVO("Inicio de sesión"),
    HOME("Panel principal"),
    FORM("Nuevo reporte"),
    FILES("Adjuntos y vista previa"),
    REVIEW("Revisión y confirmación"),
    QUEUE("Cola de envíos"),
    HISTORY("Historial"),
    DETAIL("Detalle y trazabilidad"),
    SETTINGS("Ajustes");

    val esIngreso: Boolean get() = this == LOGIN || this == LOGIN_OPERATIVO

    val isReportFlow: Boolean get() = this == FORM || this == FILES || this == REVIEW
}
