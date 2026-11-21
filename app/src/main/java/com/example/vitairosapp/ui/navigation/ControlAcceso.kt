package com.example.vitairosapp.ui.navigation

import com.example.vitairosapp.data.model.ModoApp

object ControlAcceso {

    fun pantallaPermitida(
        modo: ModoApp,
        sesionOperativaActiva: Boolean,
        ingresoDemostracionRealizado: Boolean,
        solicitada: Screen,
    ): Screen = when {
        modo == ModoApp.OPERATIVO && !sesionOperativaActiva -> Screen.LOGIN_OPERATIVO
        modo == ModoApp.OPERATIVO && solicitada == Screen.LOGIN -> Screen.HOME
        modo == ModoApp.OPERATIVO && solicitada == Screen.LOGIN_OPERATIVO -> Screen.HOME
        modo == ModoApp.DEMO && solicitada == Screen.LOGIN_OPERATIVO -> Screen.HOME
        modo == ModoApp.DEMO && !ingresoDemostracionRealizado -> Screen.LOGIN
        else -> solicitada
    }
}
