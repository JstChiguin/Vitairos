package com.example.vitairosapp.ui.viewmodel

import com.example.vitairosapp.data.model.CondicionesClima
import com.example.vitairosapp.data.model.SitioClima
import com.example.vitairosapp.data.remote.ApiClima
import com.example.vitairosapp.data.remote.ResultadoClima
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GestorClima(
    private val api: ApiClima,
    private val alcance: CoroutineScope,
    private val estaEnLinea: () -> Boolean,
    sitioInicial: SitioClima,
    private val alCambiarSitio: (SitioClima) -> Unit,
) {

    var origenSinConexion: () -> OrigenSinConexion = { OrigenSinConexion.DISPOSITIVO }

    private val estadoInterno = MutableStateFlow<EstadoClima>(EstadoClima.SinConsultar)
    val estado: StateFlow<EstadoClima> = estadoInterno.asStateFlow()

    private val sitioInterno = MutableStateFlow(sitioInicial)
    val sitio: StateFlow<SitioClima> = sitioInterno.asStateFlow()

    private var consultaEnCurso: Job? = null
    private var ultimaLectura: CondicionesClima? = null

    fun consultar() {
        consultaEnCurso?.cancel()
        if (!estaEnLinea()) {
            estadoInterno.value = EstadoClima.SinConexion(ultimaLectura, origenSinConexion())
            return
        }
        estadoInterno.value = EstadoClima.Cargando
        val sitioConsultado = sitioInterno.value
        consultaEnCurso = alcance.launch {
            estadoInterno.value = when (val resultado = api.consultar(sitioConsultado)) {
                is ResultadoClima.Exitoso -> {
                    ultimaLectura = resultado.condiciones
                    EstadoClima.Disponible(resultado.condiciones)
                }
                ResultadoClima.SinConexion -> EstadoClima.SinConexion(ultimaLectura, OrigenSinConexion.DISPOSITIVO)
                is ResultadoClima.Fallo -> EstadoClima.Error(resultado.mensaje, ultimaLectura)
            }
        }
    }

    fun seleccionarSitio(nuevoSitio: SitioClima) {
        if (nuevoSitio == sitioInterno.value) return
        ultimaLectura = null
        sitioInterno.value = nuevoSitio
        alCambiarSitio(nuevoSitio)
        consultar()
    }
}
