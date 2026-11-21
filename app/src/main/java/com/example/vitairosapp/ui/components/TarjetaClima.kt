package com.example.vitairosapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vitairosapp.data.catalog.SitiosClima
import com.example.vitairosapp.data.model.CondicionesClima
import com.example.vitairosapp.ui.theme.CopperDark
import com.example.vitairosapp.ui.theme.Danger
import com.example.vitairosapp.ui.viewmodel.EstadoClima
import com.example.vitairosapp.ui.viewmodel.GestorClima
import com.example.vitairosapp.ui.viewmodel.OrigenSinConexion
import java.util.Locale

@Composable
fun TarjetaClima(gestor: GestorClima) {
    val estado by gestor.estado.collectAsState()
    val sitio by gestor.sitio.collectAsState()
    VCard {
        SelectField(
            value = sitio.nombre,
            options = SitiosClima.LISTA.map { it.nombre },
            placeholder = "Sitio",
            onSelect = { nombre -> SitiosClima.buscar(nombre)?.let(gestor::seleccionarSitio) },
            modifier = Modifier.fillMaxWidth()
        )
        when (val actual = estado) {
            EstadoClima.SinConsultar -> Hint("Sin datos de clima todavía.")
            EstadoClima.Cargando -> Hint("Consultando condiciones meteorológicas…")
            is EstadoClima.Disponible -> Hint("Datos actuales de Open-Meteo, sin ubicación del dispositivo.")
            is EstadoClima.SinConexion -> {
                Hint(EstadoClima.MENSAJE_SIN_CONEXION, Danger)
                Hint(
                    if (actual.origen == OrigenSinConexion.SIMULADA)
                        "Causa: la conexión simulada está desactivada en los controles de demostración."
                    else "Causa: el dispositivo no tiene conexión a Internet."
                )
            }
            is EstadoClima.Error -> Hint(actual.mensaje, Danger)
        }
        estado.lectura?.let { lectura ->
            if (!estado.esLecturaActual) {
                Hint("Última información disponible (no es una lectura actual):", CopperDark)
            }
            LecturaClima(lectura, estado.esLecturaActual)
        }
        VButton("Actualizar clima", gestor::consultar, Modifier.fillMaxWidth(), ButtonStyle.Secondary)
    }
}

@Composable
private fun LecturaClima(lectura: CondicionesClima, actual: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricTile(String.format(Locale.getDefault(), "%.1f °C", lectura.temperaturaC), "Temperatura", Modifier.weight(1f))
        MetricTile(String.format(Locale.getDefault(), "%.0f km/h", lectura.vientoKmh), "Viento", Modifier.weight(1f))
        MetricTile("${lectura.humedadPorcentaje} %", "Humedad", Modifier.weight(1f))
        MetricTile("${lectura.probabilidadLluviaPorcentaje} %", "Lluvia hoy", Modifier.weight(1f))
    }
    Hint(if (actual) "Medición: ${lectura.medidoEn.substringAfter('T')}" else "Medición anterior: ${lectura.medidoEn.replace('T', ' ')}")
}
