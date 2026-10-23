package com.example.vitairosapp

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.vitairosapp.ui.VitairosApp
import com.example.vitairosapp.ui.theme.VitairosAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Barras del sistema transparentes: la cabecera y la barra inferior de la app
        // pintan su propio fondo (y la pantalla de carga ocupa todo el alto)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            VitairosAppTheme {
                VitairosApp()
            }
        }
    }
}
