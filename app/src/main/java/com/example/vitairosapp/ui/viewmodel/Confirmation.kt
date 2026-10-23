package com.example.vitairosapp.ui.viewmodel

import com.example.vitairosapp.ui.navigation.Screen

sealed class Confirmation {
    data class LeaveDraft(val target: Screen) : Confirmation()
    data class DeleteReport(val id: String) : Confirmation()
    data object LoadDemo : Confirmation()
    data object ResetDemo : Confirmation()
}
