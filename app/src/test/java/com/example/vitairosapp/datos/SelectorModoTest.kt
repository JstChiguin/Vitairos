package com.example.vitairosapp.datos

import com.example.vitairosapp.data.local.AlmacenModo
import com.example.vitairosapp.data.local.SelectorModo
import com.example.vitairosapp.data.model.ModoApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectorModoTest {

    private class AlmacenEnMemoria(override var modo: ModoApp = ModoApp.OPERATIVO) : AlmacenModo

    @Test
    fun `la aplicacion inicia en modo operativo por defecto`() {
        assertEquals(ModoApp.OPERATIVO, SelectorModo(AlmacenEnMemoria()).actual)
    }

    @Test
    fun `entrar a la demostracion cambia y persiste el modo`() {
        val almacen = AlmacenEnMemoria()
        val selector = SelectorModo(almacen)

        assertTrue(selector.establecer(ModoApp.DEMO))

        assertEquals(ModoApp.DEMO, selector.actual)
        assertEquals(ModoApp.DEMO, almacen.modo)
    }

    @Test
    fun `salir de la demostracion regresa al modo operativo`() {
        val selector = SelectorModo(AlmacenEnMemoria(ModoApp.DEMO))

        assertTrue(selector.establecer(ModoApp.OPERATIVO))

        assertEquals(ModoApp.OPERATIVO, selector.actual)
    }

    @Test
    fun `establecer el modo ya activo no produce cambio`() {
        val selector = SelectorModo(AlmacenEnMemoria())
        assertFalse(selector.establecer(ModoApp.OPERATIVO))
    }

    @Test
    fun `el modo elegido persiste en una nueva instancia del selector`() {
        val almacen = AlmacenEnMemoria()
        SelectorModo(almacen).establecer(ModoApp.DEMO)

        assertEquals(ModoApp.DEMO, SelectorModo(almacen).actual)
    }

    @Test
    fun `un nombre de modo desconocido se interpreta como operativo`() {
        assertEquals(ModoApp.OPERATIVO, ModoApp.desdeNombre("INEXISTENTE"))
        assertEquals(ModoApp.OPERATIVO, ModoApp.desdeNombre(null))
        assertEquals(ModoApp.DEMO, ModoApp.desdeNombre("DEMO"))
    }

    @Test
    fun `cada modo usa una ranura de borrador distinta`() {
        assertEquals(ModoApp.entries.size, ModoApp.entries.map { it.ranuraBorrador }.toSet().size)
    }
}
