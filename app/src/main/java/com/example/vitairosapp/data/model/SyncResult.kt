package com.example.vitairosapp.data.model

sealed class SyncResult {
    data class Accepted(val receipt: String, val message: String) : SyncResult()
    data class Rejected(val code: Int, val message: String) : SyncResult()
    data object NoConnection : SyncResult()
}
