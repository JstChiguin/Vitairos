package com.example.vitairosapp.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitairosapp.R
import com.example.vitairosapp.ui.theme.Copper
import com.example.vitairosapp.ui.theme.HeaderMuted
import com.example.vitairosapp.ui.theme.HeroGreen
import com.example.vitairosapp.ui.theme.Night

/** Pantalla de carga que se muestra al abrir la app, antes del ingreso o del panel. */
@Composable
fun SplashScreen() {
    // El logo aparece con un leve zoom y fundido
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        appear.animateTo(1f, tween(durationMillis = 700, easing = FastOutSlowInEasing))
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Night, HeroGreen)))
    ) {
        Column(
            Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    alpha = appear.value
                    scaleX = 0.85f + 0.15f * appear.value
                    scaleY = 0.85f + 0.15f * appear.value
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painterResource(R.drawable.vitairos_logo),
                contentDescription = "Logo VITAIROS",
                modifier = Modifier.size(132.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text("VITAIROS", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            Text("INDUSTRIAL AI", color = HeaderMuted, fontSize = 13.sp, letterSpacing = 4.sp)
            Spacer(Modifier.height(36.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .width(160.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50)),
                color = Copper,
                trackColor = Color.White.copy(alpha = 0.12f)
            )
            Spacer(Modifier.height(12.dp))
            Text("Cargando…", color = HeaderMuted, fontSize = 13.sp)
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text("Captura de reportes operacionales", color = HeaderMuted, fontSize = 12.sp)
            Text("Prototipo académico DSY1105", color = HeaderMuted.copy(alpha = 0.7f), fontSize = 11.sp)
        }
    }
}
