package com.example.vitairosapp.data.model

data class LogEntry(
    val at: Long = System.currentTimeMillis(),
    val message: String,
)
