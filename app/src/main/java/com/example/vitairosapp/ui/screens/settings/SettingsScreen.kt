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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.data.model.ModoApp
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
    val reportes by vm.reports.collectAsState()
    ScreenColumn {
        VCard {
            Text("Modo de la aplicación", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            InfoLine("Modo actual", vm.modo.etiqueta)
            if (vm.esDemo) {
                Text(
                    "Estás en el modo demostración: solo se muestran y modifican datos ficticios. " +
                        "Tus reportes operativos se conservan intactos y reaparecen al salir.",
                    color = Muted, fontSize = 13.5.sp, lineHeight = 21.sp
                )
                VButton(
                    "Salir de la demostración",
                    { vm.cambiarModo(ModoApp.OPERATIVO) },
                    Modifier.fillMaxWidth(),
                    ButtonStyle.Secondary
                )
            } else {
                Text(
                    "Los reportes se guardan en este dispositivo y funcionan sin conexión. " +
                        "El modo demostración usa datos ficticios separados de los operativos.",
                    color = Muted, fontSize = 13.5.sp, lineHeight = 21.sp
                )
                VButton(
                    "Entrar al modo demostración",
                    { vm.cambiarModo(ModoApp.DEMO) },
                    Modifier.fillMaxWidth(),
                    ButtonStyle.Secondary
                )
            }
        }
        if (vm.esDemo) {
            VCard {
                Text("Controles de la demostración", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Estos controles no corresponden a un sistema real de VITAIROS.", color = Muted, fontSize = 13.5.sp, lineHeight = 21.sp)
                SettingSwitch("Red simulada disponible (no es la conectividad real del dispositivo)", vm.online, vm::setOnline)
                Divider()
                SettingSwitch("Simular error en el próximo envío (agota los reintentos)", vm.failNext, vm::updateFailNext)
                Divider()
                SettingSwitch("Enviar a la API REST real (JSONPlaceholder, solo datos ficticios)", vm.usarApiReal, vm::cambiarUsoApiReal)
                Divider()
                Spacer(Modifier.height(2.dp))
                VButton("Cargar reportes de ejemplo", vm::requestLoadDemo, Modifier.fillMaxWidth(), ButtonStyle.Secondary)
                VButton("Restablecer demostración", vm::requestReset, Modifier.fillMaxWidth(), ButtonStyle.Danger)
            }
        }
        VCard {
            Text("Dispositivo", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            InfoLine("Usuario", vm.user ?: "—")
            if (!vm.esDemo) InfoLine("Sesión", if (vm.haySesionOperativa) "Activa en este dispositivo" else "Cerrada")
            InfoLine("Reportes del modo actual", "${reportes.size}")
            InfoLine("ID de instalación", vm.installId)
            InfoLine("Almacenamiento", "Privado de la aplicación")
        }
        Notice(
            when {
                !vm.esDemo ->
                    "La información se conserva en el almacenamiento privado de esta app y no se transmite a servidores: " +
                        "los reportes quedan pendientes de sincronización hasta que exista un destino remoto. " +
                        "Solo la consulta meteorológica usa Internet (Open-Meteo). " +
                        "El inicio de sesión es local y de demostración, no seguridad empresarial."
                vm.usarApiReal ->
                    "Con la API real activada solo se envían datos ficticios de clasificación, sin adjuntos, notas, " +
                        "usuario ni identificador de instalación. No usar información real o confidencial."
                else ->
                    "El envío es simulado: no se transmite a servidores. No usar información real o confidencial."
            }
        )
        if (vm.esDemo) {
            VButton("Cerrar sesión de la demostración", vm::logout, Modifier.fillMaxWidth(), ButtonStyle.Ghost)
        } else {
            VButton("Cerrar sesión", vm::cerrarSesionOperativa, Modifier.fillMaxWidth(), ButtonStyle.Ghost)
        }
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
