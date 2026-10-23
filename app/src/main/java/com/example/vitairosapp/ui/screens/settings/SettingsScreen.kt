package com.example.vitairosapp.ui.screens.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.ui.components.ButtonStyle
import com.example.vitairosapp.ui.components.Divider
import com.example.vitairosapp.ui.components.InfoLine
import com.example.vitairosapp.ui.components.Notice
import com.example.vitairosapp.ui.components.ScreenColumn
import com.example.vitairosapp.ui.components.VButton
import com.example.vitairosapp.ui.components.VCard
import com.example.vitairosapp.ui.theme.Copper
import com.example.vitairosapp.ui.theme.Danger
import com.example.vitairosapp.ui.theme.FieldBorder
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.theme.Line
import com.example.vitairosapp.ui.theme.Mist
import com.example.vitairosapp.ui.theme.Muted
import com.example.vitairosapp.ui.viewmodel.AppViewModel

@Composable
fun SettingsScreen(vm: AppViewModel) {
    ScreenColumn {
        VCard {
            Text("Controles para la presentación", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Estos controles no corresponden a un sistema real de VITAIROS.", color = Muted, fontSize = 13.5.sp, lineHeight = 21.sp)
            SettingSwitch("Conexión simulada disponible", vm.online, vm::setOnline)
            Divider()
            SettingSwitch("Simular error en el próximo envío (agota los reintentos)", vm.failNext, vm::updateFailNext)
            Divider()
            Spacer(Modifier.height(2.dp))
            VButton("Cargar reportes de ejemplo", vm::requestLoadDemo, Modifier.fillMaxWidth(), ButtonStyle.Secondary)
            VButton("Restablecer demostración", vm::requestReset, Modifier.fillMaxWidth(), ButtonStyle.Danger)
        }
        VCard {
            Text("Sesión y dispositivo", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            InfoLine("Usuario", vm.user ?: "—")
            InfoLine("ID de instalación", vm.installId)
            InfoLine("Almacenamiento", "Privado de la aplicación")
        }
        Notice(
            "La información se conserva en el almacenamiento privado de esta app. El envío es simulado: " +
                "no se transmite a servidores. No usar información real o confidencial."
        )
        VButton("Cerrar sesión simulada", vm::logout, Modifier.fillMaxWidth(), ButtonStyle.Ghost)
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f), color = Ink, fontSize = 14.sp, lineHeight = 22.sp)
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Copper,
                checkedBorderColor = Copper,
                uncheckedThumbColor = Line,
                uncheckedTrackColor = Mist,
                uncheckedBorderColor = FieldBorder,
            )
        )
    }
}
