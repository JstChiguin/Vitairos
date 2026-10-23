package com.example.vitairosapp.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ChileLocale: Locale = Locale.forLanguageTag("es-CL")

fun formatDate(millis: Long?): String =
    if (millis == null) "—" else SimpleDateFormat("dd-MM-yy HH:mm", ChileLocale).format(Date(millis))

fun formatTime(millis: Long): String = SimpleDateFormat("HH:mm:ss", ChileLocale).format(Date(millis))

fun formatSize(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
    bytes >= 1024 -> String.format(Locale.US, "%.0f KB", bytes / 1024.0)
    else -> "$bytes B"
}
