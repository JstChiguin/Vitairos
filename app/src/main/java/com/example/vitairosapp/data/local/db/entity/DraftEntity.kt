package com.example.vitairosapp.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla `draft`: el reporte que se está creando o editando (una sola fila).
 * Se guarda como JSON porque puede tener el mismo ID que un reporte ya guardado
 * (cuando se edita) y no debe mezclarse con él hasta que el usuario lo confirme.
 */
@Entity(tableName = "draft")
data class DraftEntity(
    @PrimaryKey val slot: Int = 0,
    val reportJson: String,
    val updatedAt: Long = System.currentTimeMillis(),
)
