package com.example.vitairosapp.data.model

enum class ModoApp(val etiqueta: String, val ranuraBorrador: Int) {
    OPERATIVO("Operativo", 0),
    DEMO("Demostración", 1);

    companion object {
        fun desdeNombre(nombre: String?): ModoApp =
            entries.firstOrNull { it.name == nombre } ?: OPERATIVO
    }
}
