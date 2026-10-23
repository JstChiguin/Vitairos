package com.example.vitairosapp.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.R
import com.example.vitairosapp.data.catalog.Catalogs
import com.example.vitairosapp.ui.components.FieldLabel
import com.example.vitairosapp.ui.components.Hint
import com.example.vitairosapp.ui.components.Notice
import com.example.vitairosapp.ui.components.ScreenColumn
import com.example.vitairosapp.ui.components.SelectField
import com.example.vitairosapp.ui.components.VButton
import com.example.vitairosapp.ui.components.VCard
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.theme.Muted
import com.example.vitairosapp.ui.viewmodel.AppViewModel

@Composable
fun LoginScreen(vm: AppViewModel) {
    var selected by rememberSaveable { mutableStateOf(Catalogs.USUARIOS_DEMO.first()) }
    ScreenColumn {
        Spacer(Modifier.height(16.dp))
        Image(
            painterResource(R.drawable.vitairos_logo),
            contentDescription = "Isotipo VITAIROS",
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.CenterHorizontally)
        )
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("VITAIROS Industrial AI", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                "Captura de reportes operacionales · MVP académico",
                color = Muted, fontSize = 12.5.sp, textAlign = TextAlign.Center
            )
        }
        VCard {
            Text("Ingresar a la demostración", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            FieldLabel("Usuario ficticio")
            SelectField(
                value = selected,
                options = Catalogs.USUARIOS_DEMO,
                placeholder = "Seleccionar usuario",
                onSelect = { selected = it }
            )
            Hint("Ingreso local simulado. No se utilizan credenciales corporativas.")
            VButton(
                if (vm.loggingIn) "Validando…" else "Ingresar →",
                onClick = { vm.login(selected) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !vm.loggingIn
            )
        }
        Spacer(Modifier.height(8.dp))
        Notice("Prototipo académico DSY1105. Datos, procesos y respuestas de envío son ficticios.")
    }
}
