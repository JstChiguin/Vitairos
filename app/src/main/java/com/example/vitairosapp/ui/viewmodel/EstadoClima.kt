package com.example.vitairosapp.ui.viewmodel

import com.example.vitairosapp.data.model.CondicionesClima

enum class OrigenSinConexion { DISPOSITIVO, SIMULADA }

sealed class EstadoClima {
    data object SinConsultar : EstadoClima()
    data object Cargando : EstadoClima()
    data class Disponible(val condiciones: CondicionesClima) : EstadoClima()
    data class SinConexion(
        val ultimaLectura: CondicionesClima?,
        val origen: OrigenSinConexion = OrigenSinConexion.DISPOSITIVO,
    ) : EstadoClima()
    data class Error(val mensaje: String, val ultimaLectura: CondicionesClima?) : EstadoClima()

    val lectura: CondicionesClima?
        get() = when (this) {
            is Disponible -> condiciones
            is SinConexion -> ultimaLectura
            is Error -> ultimaLectura
            SinConsultar, Cargando -> null
        }

    val esLecturaActual: Boolean get() = this is Disponible

    companion object {
        const val MENSAJE_SIN_CONEXION = "Sin conexión. No se puede actualizar el clima"
    }
}
