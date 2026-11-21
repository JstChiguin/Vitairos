package com.example.vitairosapp.data.catalog

import com.example.vitairosapp.data.model.SitioClima

object SitiosClima {
    val LISTA = listOf(
        SitioClima("Planta ficticia Norte (ref. Antofagasta)", -23.65, -70.40),
        SitioClima("Planta ficticia Centro (ref. Santiago)", -33.45, -70.66),
        SitioClima("Planta ficticia Sur (ref. Concepción)", -36.83, -73.05),
    )

    fun buscar(nombre: String?): SitioClima? = LISTA.firstOrNull { it.nombre == nombre }
}
