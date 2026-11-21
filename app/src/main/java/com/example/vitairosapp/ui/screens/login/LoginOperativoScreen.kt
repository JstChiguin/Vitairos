package com.example.vitairosapp.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.R
import com.example.vitairosapp.data.model.ModoApp
import com.example.vitairosapp.ui.components.ButtonStyle
import com.example.vitairosapp.ui.components.FieldLabel
import com.example.vitairosapp.ui.components.Notice
import com.example.vitairosapp.ui.components.NoticeKind
import com.example.vitairosapp.ui.components.ScreenColumn
import com.example.vitairosapp.ui.components.VButton
import com.example.vitairosapp.ui.components.VCard
import com.example.vitairosapp.ui.components.vitairosFieldColors
import com.example.vitairosapp.ui.theme.CopperDark
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.theme.Muted
import com.example.vitairosapp.ui.viewmodel.AppViewModel

@Composable
fun LoginOperativoScreen(vm: AppViewModel) {
    var usuario by rememberSaveable { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var contrasenaVisible by remember { mutableStateOf(false) }
    val intentarIngreso = { vm.iniciarSesionOperativa(usuario, contrasena) }
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
                "Captura de reportes operacionales",
                color = Muted, fontSize = 12.5.sp, textAlign = TextAlign.Center
            )
        }
        VCard {
            Text("Iniciar sesión", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            FieldLabel("Usuario")
            OutlinedTextField(
                value = usuario,
                onValueChange = { usuario = it.take(40) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = vitairosFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            FieldLabel("Contraseña")
            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it.take(64) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = vitairosFieldColors(),
                visualTransformation = if (contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { intentarIngreso() }),
                trailingIcon = {
                    TextButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                        Text(if (contrasenaVisible) "Ocultar" else "Mostrar", color = CopperDark, fontSize = 13.sp)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            vm.errorInicioSesion?.let { Notice(it, NoticeKind.Danger) }
            VButton(
                if (vm.iniciandoSesion) "Validando…" else "Ingresar →",
                onClick = intentarIngreso,
                modifier = Modifier.fillMaxWidth(),
                enabled = !vm.iniciandoSesion
            )
        }
        VButton(
            "Explorar el modo demostración",
            { vm.cambiarModo(ModoApp.DEMO) },
            Modifier.fillMaxWidth(),
            ButtonStyle.Ghost
        )
        Spacer(Modifier.height(8.dp))
        Notice("El modo demostración no requiere iniciar sesión y usa solo datos ficticios.")
    }
}
