package com.example.vitairosapp.data.catalog

/** Catálogos provisionales: VITAIROS entregará o validará los definitivos. */
object Catalogs {
    val TIPOS = listOf(
        "Reporte de turno (demostración)",
        "Registro de mantenimiento (demostración)",
        "Inspección operacional (demostración)",
        "Registro documental (demostración)",
    )

    val PROCESOS = listOf(
        "Operación (demostración)",
        "Mantenimiento (demostración)",
        "Seguridad operacional (demostración)",
        "Control documental (demostración)",
    )

    val TURNOS = listOf("Día", "Noche", "Otro")

    val USUARIOS_DEMO = listOf("Operador demo", "Operadora de terreno")
}
