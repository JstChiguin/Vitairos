package com.example.vitairosapp.data.model

data class CondicionesClima(
    val temperaturaC: Double,
    val vientoKmh: Double,
    val humedadPorcentaje: Int,
    val probabilidadLluviaPorcentaje: Int,
    val medidoEn: String,
)
