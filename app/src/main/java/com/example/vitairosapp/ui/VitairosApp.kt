package com.example.vitairosapp.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vitairosapp.data.model.ReportStatus
import com.example.vitairosapp.ui.components.BottomBar
import com.example.vitairosapp.ui.components.ConfirmationDialog
import com.example.vitairosapp.ui.components.Header
import com.example.vitairosapp.ui.navigation.Screen
import com.example.vitairosapp.ui.screens.detail.DetailScreen
import com.example.vitairosapp.ui.screens.history.HistoryScreen
import com.example.vitairosapp.ui.screens.home.HomeScreen
import com.example.vitairosapp.ui.screens.login.LoginScreen
import com.example.vitairosapp.ui.screens.queue.QueueScreen
import com.example.vitairosapp.ui.screens.report.AttachmentsScreen
import com.example.vitairosapp.ui.screens.report.ReportFormScreen
import com.example.vitairosapp.ui.screens.report.ReviewScreen
import com.example.vitairosapp.ui.screens.settings.SettingsScreen
import com.example.vitairosapp.ui.screens.splash.SplashScreen
import com.example.vitairosapp.ui.theme.Bone
import com.example.vitairosapp.ui.theme.SnackBg
import com.example.vitairosapp.ui.viewmodel.AppViewModel
import kotlinx.coroutines.delay

/** Duración de la pantalla de carga al abrir la app. */
private const val SPLASH_MILLIS = 1800L

@Composable
fun VitairosApp(vm: AppViewModel = viewModel()) {
    var loading by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(SPLASH_MILLIS)
        loading = false
    }
    Crossfade(targetState = loading, animationSpec = tween(400), label = "splash") { isLoading ->
        if (isLoading) SplashScreen() else AppContent(vm)
    }
}

@Composable
private fun AppContent(vm: AppViewModel) {
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
