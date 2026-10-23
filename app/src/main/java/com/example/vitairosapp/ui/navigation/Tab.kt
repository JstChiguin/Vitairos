package com.example.vitairosapp.ui.navigation

data class Tab(val screen: Screen, val icon: String, val label: String)

internal val Tabs = listOf(
    Tab(Screen.HOME, "⌂", "Inicio"),
    Tab(Screen.FORM, "＋", "Nuevo"),
    Tab(Screen.QUEUE, "⇅", "Cola"),
    Tab(Screen.HISTORY, "◷", "Historial"),
    Tab(Screen.SETTINGS, "⚙", "Ajustes"),
)
