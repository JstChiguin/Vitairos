package com.example.vitairosapp.data.remote

import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.SyncResult
import org.json.JSONObject

interface ApiSincronizacion {
    val descripcion: String

    fun armarCuerpo(reporte: Report): JSONObject

    suspend fun enviar(reporte: Report, estaEnLinea: () -> Boolean, forzarFallo: Boolean): SyncResult
}
