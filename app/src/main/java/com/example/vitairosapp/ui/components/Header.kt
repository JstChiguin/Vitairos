package com.example.vitairosapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.ui.navigation.Screen
import com.example.vitairosapp.ui.theme.Danger
import com.example.vitairosapp.ui.theme.Deep
import com.example.vitairosapp.ui.theme.HeaderMuted
import com.example.vitairosapp.ui.theme.Night
import com.example.vitairosapp.ui.theme.OnlinePill
import com.example.vitairosapp.ui.viewmodel.AppViewModel

@Composable
internal fun Header(vm: AppViewModel) {
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
