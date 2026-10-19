package com.example.vitairosapp.data

import android.content.Context
import androidx.core.content.edit
import java.util.Locale
import java.util.UUID

class AppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("vitairos_prefs", Context.MODE_PRIVATE)

    var user: String?
        get() = prefs.getString("user", null)
        set(value) = prefs.edit { putString("user", value) }

    var online: Boolean
        get() = prefs.getBoolean("online", true)
        set(value) = prefs.edit { putBoolean("online", value) }

    var failNext: Boolean
        get() = prefs.getBoolean("fail_next", false)
        set(value) = prefs.edit { putBoolean("fail_next", value) }

    /** Paso del flujo de creación en que quedó el borrador (FORM, FILES o REVIEW). */
    var draftStep: String?
        get() = prefs.getString("draft_step", null)
        set(value) = prefs.edit { putString("draft_step", value) }

    /** Identificador estable de esta instalación, generado la primera vez. */
    val installId: String
        get() {
            prefs.getString("install_id", null)?.let { return it }
            val id = "INST-" + UUID.randomUUID().toString().take(8).uppercase(Locale.ROOT)
            prefs.edit { putString("install_id", id) }
            return id
        }

    /** Borra la sesión simulada y los controles demo; conserva el ID de instalación. */
    fun resetDemo() {
        prefs.edit {
            remove("user")
            remove("online")
            remove("fail_next")
            remove("draft_step")
        }
    }

    companion object {
        const val MAX_ATTACHMENTS = 5
    }
}
