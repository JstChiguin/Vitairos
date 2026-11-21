package com.example.vitairosapp.data.local

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class ConectividadDispositivo(contexto: Context) {

    private val administrador =
        contexto.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun hayRed(): Boolean {
        val red = administrador.activeNetwork ?: return false
        val capacidades = administrador.getNetworkCapabilities(red) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
