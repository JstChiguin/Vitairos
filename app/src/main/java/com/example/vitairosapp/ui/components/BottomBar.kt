package com.example.vitairosapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.ui.navigation.Screen
import com.example.vitairosapp.ui.navigation.Tabs
import com.example.vitairosapp.ui.theme.Copper
import com.example.vitairosapp.ui.theme.CopperDark
import com.example.vitairosapp.ui.theme.Ink
import com.example.vitairosapp.ui.theme.Mist
import com.example.vitairosapp.ui.theme.Muted

@Composable
internal fun BottomBar(current: Screen, pending: Int, onSelect: (Screen) -> Unit) {
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
