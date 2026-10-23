package com.example.vitairosapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.example.vitairosapp.ui.theme.Bone
import com.example.vitairosapp.ui.theme.Danger
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.theme.Muted
import com.example.vitairosapp.ui.viewmodel.AppViewModel
import com.example.vitairosapp.ui.viewmodel.Confirmation

@Composable
internal fun ConfirmationDialog(vm: AppViewModel, confirmation: Confirmation) {
    when (confirmation) {
        is Confirmation.LeaveDraft -> AlertDialog(
            onDismissRequest = vm::dismissConfirmation,
            containerColor = Bone,
            title = { Text("¿Salir del reporte en curso?", color = Ink) },
            text = {
                Text(
                    "Puedes guardarlo como borrador en el dispositivo para continuar después, o descartar los cambios.",
                    color = Muted
                )
            },
            confirmButton = {
                Column(horizontalAlignment = Alignment.End) {
                    TextButton(onClick = { vm.confirm(confirmation) }) {
                        Text("Guardar borrador", color = Ink, fontWeight = FontWeight.Medium)
                    }
                    Row {
                        TextButton(onClick = { vm.leaveDiscarding(confirmation.target) }) {
                            Text("Descartar", color = Danger, fontWeight = FontWeight.Medium)
                        }
                        TextButton(onClick = vm::dismissConfirmation) {
                            Text("Seguir editando", color = Ink, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        )

        is Confirmation.DeleteReport -> SimpleConfirm(
            vm, confirmation,
            title = "¿Eliminar el reporte pendiente?",
            text = "Esta acción no se puede deshacer. Se eliminarán también sus adjuntos locales.",
            confirmLabel = "Eliminar",
            danger = true
        )

        Confirmation.LoadDemo -> SimpleConfirm(
            vm, confirmation,
            title = "Cargar reportes de ejemplo",
            text = "Se agregarán tres reportes sintéticos (Borrador, En cola y Sincronizado) para la demostración.",
            confirmLabel = "Agregar",
            danger = false
        )

        Confirmation.ResetDemo -> SimpleConfirm(
            vm, confirmation,
            title = "¿Restablecer la demostración?",
            text = "Se eliminarán todos los reportes, adjuntos y la sesión simulada de este dispositivo.",
            confirmLabel = "Restablecer",
            danger = true
        )
    }
}

@Composable
private fun SimpleConfirm(
    vm: AppViewModel,
    confirmation: Confirmation,
    title: String,
    text: String,
    confirmLabel: String,
    danger: Boolean,
) {
    AlertDialog(
        onDismissRequest = vm::dismissConfirmation,
        containerColor = Bone,
        title = { Text(title, color = Ink) },
        text = { Text(text, color = Muted) },
        confirmButton = {
            TextButton(onClick = { vm.confirm(confirmation) }) {
                Text(confirmLabel, color = if (danger) Danger else Ink, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = vm::dismissConfirmation) {
                Text("Cancelar", color = Ink)
            }
        }
    )
}
