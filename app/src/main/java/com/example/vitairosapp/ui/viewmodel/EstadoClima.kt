package com.example.vitairosapp.ui.viewmodel

import com.example.vitairosapp.data.model.CondicionesClima

sealed class EstadoClima {
    data object SinConsultar : EstadoClima()
    data object Cargando : EstadoClima()
    data class Disponible(val condiciones: CondicionesClima) : EstadoClima()
    data class SinConexion(val ultimaLectura: CondicionesClima?) : EstadoClima()
    data class Error(val mensaje: String, val ultimaLectura: CondicionesClima?) : EstadoClima()

    val lectura: CondicionesClima?
        get() = when (this) {
            is Disponible -> condiciones
            is SinConexion -> ultimaLectura
            is Error -> ultimaLectura
            SinConsultar, Cargando -> null
        }
}
