package com.example.vitairosapp.data.model

sealed class ImportResult {
    data class Ok(val attachment: Attachment, val warning: String? = null) : ImportResult()
    data class Rejected(val reason: String) : ImportResult()
}
