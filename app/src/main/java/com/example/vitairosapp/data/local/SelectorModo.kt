package com.example.vitairosapp.data.local

import com.example.vitairosapp.data.model.ModoApp

interface AlmacenModo {
    var modo: ModoApp
}

class SelectorModo(private val almacen: AlmacenModo) {

    val actual: ModoApp get() = almacen.modo

    fun establecer(nuevo: ModoApp): Boolean {
        if (nuevo == almacen.modo) return false
        almacen.modo = nuevo
        return true
    }
}
