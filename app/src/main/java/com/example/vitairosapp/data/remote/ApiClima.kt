package com.example.vitairosapp.data.remote

import com.example.vitairosapp.data.model.CondicionesClima
import com.example.vitairosapp.data.model.SitioClima

sealed class ResultadoClima {
    data class Exitoso(val condiciones: CondicionesClima) : ResultadoClima()
    data class Fallo(val codigo: Int, val mensaje: String) : ResultadoClima()
    data object SinConexion : ResultadoClima()
}

interface ApiClima {
    suspend fun consultar(sitio: SitioClima): ResultadoClima
}
