package com.example.vitairosapp.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vitairosapp.ui.screens.AttachmentsScreen
import com.example.vitairosapp.ui.screens.DetailScreen
import com.example.vitairosapp.ui.screens.HistoryScreen
import com.example.vitairosapp.ui.screens.HomeScreen
import com.example.vitairosapp.ui.screens.LoginScreen
import com.example.vitairosapp.ui.screens.QueueScreen
import com.example.vitairosapp.ui.screens.ReportFormScreen
import com.example.vitairosapp.ui.screens.ReviewScreen
import com.example.vitairosapp.ui.screens.SettingsScreen
import com.example.vitairosapp.ui.theme.Bone
import com.example.vitairosapp.ui.theme.Copper
import com.example.vitairosapp.ui.theme.CopperDark
import com.example.vitairosapp.ui.theme.Danger
import com.example.vitairosapp.ui.theme.Deep
import com.example.vitairosapp.ui.theme.HeaderMuted
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.theme.Mist
import com.example.vitairosapp.ui.theme.Muted
import com.example.vitairosapp.ui.theme.Night
import com.example.vitairosapp.ui.theme.OnlinePill
import com.example.vitairosapp.ui.theme.SnackBg
import com.example.vitairosapp.data.ReportStatus

data class Tab(val screen: Screen, val icon: String, val label: String)

private val Tabs = listOf(
    Tab(Screen.HOME, "⌂", "Inicio"),
    Tab(Screen.FORM, "＋", "Nuevo"),
    Tab(Screen.QUEUE, "⇅", "Cola"),
    Tab(Screen.HISTORY, "◷", "Historial"),
    Tab(Screen.SETTINGS, "⚙", "Ajustes"),
)

@Composable
fun VitairosApp(vm: AppViewModel = viewModel()) {
    val reports by vm.reports.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.messages.collect { message ->
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message)
        }
    }

    // Botón atrás del sistema: retrocede dentro del flujo antes de salir
    BackHandler(enabled = vm.screen != Screen.LOGIN && vm.screen != Screen.HOME) {
        when (vm.screen) {
            Screen.FILES -> vm.backToForm()
            Screen.REVIEW -> vm.backToAttachments()
            Screen.DETAIL -> vm.navigate(Screen.HISTORY)
            else -> vm.navigate(Screen.HOME)
        }
    }

    Scaffold(
        containerColor = Bone,
        topBar = { Header(vm) },
        bottomBar = {
            if (vm.screen != Screen.LOGIN) {
                val pending = reports.count {
                    it.status == ReportStatus.EN_COLA || it.status == ReportStatus.ERROR
                }
                BottomBar(vm.screen, pending, vm::navigate)
            }
        },
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SnackBg,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (vm.screen) {
                Screen.LOGIN -> LoginScreen(vm)
                Screen.HOME -> HomeScreen(vm, reports)
                Screen.FORM -> ReportFormScreen(vm)
                Screen.FILES -> AttachmentsScreen(vm)
                Screen.REVIEW -> ReviewScreen(vm)
                Screen.QUEUE -> QueueScreen(vm, reports)
                Screen.HISTORY -> HistoryScreen(vm, reports)
                Screen.DETAIL -> DetailScreen(vm, reports)
                Screen.SETTINGS -> SettingsScreen(vm)
            }
        }
    }

    vm.confirmation?.let { ConfirmationDialog(vm, it) }
}

@Composable
private fun Header(vm: AppViewModel) {
    val title = if (vm.screen == Screen.FORM && vm.isEditing) "Editar reporte" else vm.screen.title
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(Night, Night, Deep)))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("VITAIROS INDUSTRIAL AI", color = HeaderMuted, fontSize = 11.sp, letterSpacing = 2.sp)
                Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            if (vm.screen != Screen.LOGIN) {
                Text(
                    if (vm.online) "◉ Online" else "◌ Sin red",
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (vm.online) OnlinePill else Danger)
                        .clickable { vm.toggleOnline() }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BottomBar(current: Screen, pending: Int, onSelect: (Screen) -> Unit) {
    val selected = when (current) {
        Screen.FILES, Screen.REVIEW -> Screen.FORM
        Screen.DETAIL -> Screen.HISTORY
        else -> current
    }
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(vertical = 10.dp)
    ) {
        Tabs.forEach { tab ->
            val isSelected = tab.screen == selected
            Column(
                Modifier
                    .weight(1f)
                    .clickable { onSelect(tab.screen) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) Mist else Color.Transparent)
                            .padding(horizontal = 18.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(tab.icon, color = if (isSelected) CopperDark else Muted, fontSize = 18.sp)
                    }
                    if (tab.screen == Screen.QUEUE && pending > 0) {
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = (-4).dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Copper),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$pending", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(
                    tab.label,
                    color = if (isSelected) CopperDark else Muted,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun ConfirmationDialog(vm: AppViewModel, confirmation: Confirmation) {
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
