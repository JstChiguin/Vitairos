package com.example.vitairosapp.data.catalog

import com.example.vitairosapp.data.model.ModoApp

object Catalogs {
    private val TIPOS_OPERATIVOS = listOf(
        "Reporte de turno",
        "Registro de mantenimiento",
        "Inspección operacional",
        "Registro documental",
    )

    private val PROCESOS_OPERATIVOS = listOf(
        "Operación",
        "Mantenimiento",
        "Seguridad operacional",
        "Control documental",
    )

    private val TIPOS_DEMOSTRACION = TIPOS_OPERATIVOS.map { "$it (demostración)" }

    private val PROCESOS_DEMOSTRACION = PROCESOS_OPERATIVOS.map { "$it (demostración)" }

    fun tipos(modo: ModoApp): List<String> =
        if (modo == ModoApp.DEMO) TIPOS_DEMOSTRACION else TIPOS_OPERATIVOS

    fun procesos(modo: ModoApp): List<String> =
        if (modo == ModoApp.DEMO) PROCESOS_DEMOSTRACION else PROCESOS_OPERATIVOS

    val TURNOS = listOf("Día", "Noche", "Otro")

    val USUARIOS_DEMO = listOf("Operador demo", "Operadora de terreno")
}
