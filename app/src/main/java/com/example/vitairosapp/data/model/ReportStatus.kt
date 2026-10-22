package com.example.vitairosapp.data.model

enum class ReportStatus(val label: String) {
    BORRADOR("Borrador"),
    EN_COLA("En cola"),
    ENVIANDO("Enviando"),
    SINCRONIZADO("Sincronizado"),
    ERROR("Error");

    val isEditable: Boolean get() = this == BORRADOR || this == EN_COLA || this == ERROR
    val isDeletable: Boolean get() = this == BORRADOR || this == EN_COLA
    val isPending: Boolean get() = this == EN_COLA || this == ENVIANDO || this == ERROR
}
