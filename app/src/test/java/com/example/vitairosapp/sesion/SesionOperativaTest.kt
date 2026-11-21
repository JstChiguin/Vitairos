package com.example.vitairosapp.sesion

import com.example.vitairosapp.data.local.AlmacenSesion
import com.example.vitairosapp.data.local.ResultadoInicioSesion
import com.example.vitairosapp.data.local.SesionOperativa
import com.example.vitairosapp.data.local.VerificadorCredencialesLocales
import com.example.vitairosapp.data.model.ModoApp
import com.example.vitairosapp.ui.navigation.ControlAcceso
import com.example.vitairosapp.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SesionOperativaTest {

    private class AlmacenEnMemoria(override var inicioSesionMillis: Long? = null) : AlmacenSesion

    private var ahora = 1_000_000L
    private val almacen = AlmacenEnMemoria()
    private fun sesion(almacenUsado: AlmacenSesion = almacen) = SesionOperativa(almacenUsado, reloj = { ahora })

    @Test
    fun `sin iniciar sesion no hay sesion activa`() {
        assertFalse(sesion().activa)
    }

    @Test
    fun `las credenciales correctas inician la sesion`() {
        val resultado = sesion().iniciar("operador", "12345")

        assertEquals(ResultadoInicioSesion.Correcto, resultado)
        assertTrue(sesion().activa)
    }

    @Test
    fun `el usuario no distingue mayusculas ni espacios laterales`() {
        assertEquals(ResultadoInicioSesion.Correcto, sesion().iniciar("  Operador ", "12345"))
    }

    @Test
    fun `una contrasena incorrecta se rechaza y no abre sesion`() {
        val resultado = sesion().iniciar("operador", "54321")

        assertEquals(ResultadoInicioSesion.CredencialesIncorrectas, resultado)
        assertFalse(sesion().activa)
        assertNull(almacen.inicioSesionMillis)
    }

    @Test
    fun `un usuario incorrecto se rechaza`() {
        assertEquals(ResultadoInicioSesion.CredencialesIncorrectas, sesion().iniciar("admin", "12345"))
        assertFalse(sesion().activa)
    }

    @Test
    fun `la contrasena es sensible a mayusculas y exacta`() {
        val verificador = VerificadorCredencialesLocales()
        assertFalse(verificador.verificar("operador", "12345 "))
        assertFalse(verificador.verificar("operador", "1234"))
        assertTrue(verificador.verificar("operador", "12345"))
    }

    @Test
    fun `los campos vacios se informan sin validar credenciales`() {
        assertEquals(ResultadoInicioSesion.CamposVacios, sesion().iniciar("", "12345"))
        assertEquals(ResultadoInicioSesion.CamposVacios, sesion().iniciar("operador", ""))
        assertEquals(ResultadoInicioSesion.CamposVacios, sesion().iniciar("   ", "12345"))
    }

    @Test
    fun `cerrar sesion la desactiva y limpia el almacen`() {
        val sesion = sesion()
        sesion.iniciar("operador", "12345")

        sesion.cerrar()

        assertFalse(sesion.activa)
        assertNull(almacen.inicioSesionMillis)
    }

    @Test
    fun `la sesion persiste en una nueva instancia mientras no expire`() {
        sesion().iniciar("operador", "12345")
        ahora += 60 * 60 * 1000L

        assertTrue(sesion().activa)
    }

    @Test
    fun `la sesion expira tras la duracion maxima`() {
        sesion().iniciar("operador", "12345")
        ahora += SesionOperativa.DURACION_MAXIMA_MILLIS + 1

        assertFalse(sesion().activa)
        assertNull(almacen.inicioSesionMillis)
    }

    @Test
    fun `el almacen solo conserva una marca de tiempo y no la contrasena`() {
        sesion().iniciar("operador", "12345")

        assertEquals(1_000_000L, almacen.inicioSesionMillis)
    }

    @Test
    fun `el modo operativo sin sesion solo permite la pantalla de inicio de sesion`() {
        Screen.entries.forEach { solicitada ->
            val permitida = ControlAcceso.pantallaPermitida(ModoApp.OPERATIVO, false, false, solicitada)
            assertEquals(Screen.LOGIN_OPERATIVO, permitida)
        }
    }

    @Test
    fun `el modo operativo con sesion permite las pantallas protegidas`() {
        val protegidas = listOf(
            Screen.HOME, Screen.FORM, Screen.FILES, Screen.REVIEW,
            Screen.QUEUE, Screen.HISTORY, Screen.DETAIL, Screen.SETTINGS
        )
        protegidas.forEach {
            assertEquals(it, ControlAcceso.pantallaPermitida(ModoApp.OPERATIVO, true, false, it))
        }
    }

    @Test
    fun `con sesion operativa los ingresos redirigen al panel`() {
        assertEquals(Screen.HOME, ControlAcceso.pantallaPermitida(ModoApp.OPERATIVO, true, false, Screen.LOGIN_OPERATIVO))
        assertEquals(Screen.HOME, ControlAcceso.pantallaPermitida(ModoApp.OPERATIVO, true, false, Screen.LOGIN))
    }

    @Test
    fun `tras cerrar sesion las pantallas operativas dejan de ser accesibles`() {
        val sesion = sesion()
        sesion.iniciar("operador", "12345")
        assertEquals(Screen.QUEUE, ControlAcceso.pantallaPermitida(ModoApp.OPERATIVO, sesion.activa, false, Screen.QUEUE))

        sesion.cerrar()

        assertEquals(Screen.LOGIN_OPERATIVO, ControlAcceso.pantallaPermitida(ModoApp.OPERATIVO, sesion.activa, false, Screen.QUEUE))
    }

    @Test
    fun `el modo demostracion es accesible sin iniciar sesion operativa`() {
        assertEquals(Screen.HOME, ControlAcceso.pantallaPermitida(ModoApp.DEMO, false, true, Screen.HOME))
        assertEquals(Screen.LOGIN, ControlAcceso.pantallaPermitida(ModoApp.DEMO, false, false, Screen.HOME))
    }

    @Test
    fun `el modo demostracion nunca muestra el inicio de sesion operativo`() {
        assertEquals(Screen.HOME, ControlAcceso.pantallaPermitida(ModoApp.DEMO, false, true, Screen.LOGIN_OPERATIVO))
    }

    @Test
    fun `cambiar de modo respeta la sesion de cada modo`() {
        assertEquals(Screen.HOME, ControlAcceso.pantallaPermitida(ModoApp.DEMO, false, true, Screen.HOME))
        assertEquals(Screen.LOGIN_OPERATIVO, ControlAcceso.pantallaPermitida(ModoApp.OPERATIVO, false, true, Screen.HOME))
    }
}
