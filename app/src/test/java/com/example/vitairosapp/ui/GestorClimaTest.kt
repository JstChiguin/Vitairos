package com.example.vitairosapp.ui

import com.example.vitairosapp.data.catalog.SitiosClima
import com.example.vitairosapp.data.model.CondicionesClima
import com.example.vitairosapp.data.model.SitioClima
import com.example.vitairosapp.data.remote.ApiClima
import com.example.vitairosapp.data.remote.ResultadoClima
import com.example.vitairosapp.ui.viewmodel.EstadoClima
import com.example.vitairosapp.ui.viewmodel.GestorClima
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GestorClimaTest {

    private class ApiClimaFalsa(var respuesta: ResultadoClima) : ApiClima {
        val sitiosConsultados = mutableListOf<SitioClima>()
        var espera: CompletableDeferred<Unit>? = null

        override suspend fun consultar(sitio: SitioClima): ResultadoClima {
            sitiosConsultados += sitio
            espera?.await()
            return respuesta
        }
    }

    private val lectura = CondicionesClima(18.5, 4.5, 56, 33, "2026-11-19T10:45")
    private val lecturaNueva = CondicionesClima(25.0, 10.0, 40, 5, "2026-11-19T11:45")
    private val sitioInicial = SitiosClima.LISTA[1]

    private class Entorno(val gestor: GestorClima, val api: ApiClimaFalsa, val sitiosGuardados: List<SitioClima>)

    private fun TestScope.crear(
        respuesta: ResultadoClima = ResultadoClima.Exitoso(lectura),
        enLinea: () -> Boolean = { true },
    ): Entorno {
        val api = ApiClimaFalsa(respuesta)
        val guardados = mutableListOf<SitioClima>()
        val gestor = GestorClima(api, this, enLinea, sitioInicial) { guardados += it }
        return Entorno(gestor, api, guardados)
    }

    @Test
    fun `al crearse no hay datos ni consulta`() = runTest {
        val entorno = crear()
        assertEquals(EstadoClima.SinConsultar, entorno.gestor.estado.value)
        assertEquals(sitioInicial, entorno.gestor.sitio.value)
        assertTrue(entorno.api.sitiosConsultados.isEmpty())
    }

    @Test
    fun `una consulta correcta deja las condiciones disponibles`() = runTest {
        val entorno = crear()

        entorno.gestor.consultar()
        advanceUntilIdle()

        assertEquals(EstadoClima.Disponible(lectura), entorno.gestor.estado.value)
        assertEquals(listOf(sitioInicial), entorno.api.sitiosConsultados)
    }

    @Test
    fun `mientras se consulta el estado es cargando`() = runTest {
        val entorno = crear()
        entorno.api.espera = CompletableDeferred()

        entorno.gestor.consultar()
        runCurrent()

        assertEquals(EstadoClima.Cargando, entorno.gestor.estado.value)
        entorno.api.espera!!.complete(Unit)
        advanceUntilIdle()
        assertEquals(EstadoClima.Disponible(lectura), entorno.gestor.estado.value)
    }

    @Test
    fun `sin conexion simulada no se consulta la api`() = runTest {
        val entorno = crear(enLinea = { false })

        entorno.gestor.consultar()
        advanceUntilIdle()

        assertEquals(EstadoClima.SinConexion(null), entorno.gestor.estado.value)
        assertTrue(entorno.api.sitiosConsultados.isEmpty())
    }

    @Test
    fun `sin conexion se conserva la ultima lectura obtenida`() = runTest {
        var enLinea = true
        val entorno = crear(enLinea = { enLinea })
        entorno.gestor.consultar()
        advanceUntilIdle()

        enLinea = false
        entorno.gestor.consultar()
        advanceUntilIdle()

        assertEquals(EstadoClima.SinConexion(lectura), entorno.gestor.estado.value)
        assertEquals(lectura, entorno.gestor.estado.value.lectura)
    }

    @Test
    fun `si la api informa falta de red se conserva la ultima lectura`() = runTest {
        val entorno = crear()
        entorno.gestor.consultar()
        advanceUntilIdle()

        entorno.api.respuesta = ResultadoClima.SinConexion
        entorno.gestor.consultar()
        advanceUntilIdle()

        assertEquals(EstadoClima.SinConexion(lectura), entorno.gestor.estado.value)
    }

    @Test
    fun `un fallo de la api informa el mensaje y conserva la ultima lectura`() = runTest {
        val entorno = crear()
        entorno.gestor.consultar()
        advanceUntilIdle()

        entorno.api.respuesta = ResultadoClima.Fallo(500, "Servicio caído")
        entorno.gestor.consultar()
        advanceUntilIdle()

        assertEquals(EstadoClima.Error("Servicio caído", lectura), entorno.gestor.estado.value)
    }

    @Test
    fun `un fallo sin lecturas previas no muestra datos`() = runTest {
        val entorno = crear(respuesta = ResultadoClima.Fallo(502, "Respuesta inesperada"))

        entorno.gestor.consultar()
        advanceUntilIdle()

        assertNull(entorno.gestor.estado.value.lectura)
    }

    @Test
    fun `una nueva consulta correcta reemplaza la lectura anterior`() = runTest {
        val entorno = crear()
        entorno.gestor.consultar()
        advanceUntilIdle()

        entorno.api.respuesta = ResultadoClima.Exitoso(lecturaNueva)
        entorno.gestor.consultar()
        advanceUntilIdle()

        assertEquals(EstadoClima.Disponible(lecturaNueva), entorno.gestor.estado.value)
    }

    @Test
    fun `cambiar de sitio lo guarda, descarta la lectura anterior y consulta el nuevo`() = runTest {
        val entorno = crear()
        entorno.gestor.consultar()
        advanceUntilIdle()
        val nuevoSitio = SitiosClima.LISTA[2]

        entorno.api.respuesta = ResultadoClima.Fallo(500, "Servicio caído")
        entorno.gestor.seleccionarSitio(nuevoSitio)
        advanceUntilIdle()

        assertEquals(nuevoSitio, entorno.gestor.sitio.value)
        assertEquals(listOf(nuevoSitio), entorno.sitiosGuardados)
        assertEquals(nuevoSitio, entorno.api.sitiosConsultados.last())
        assertEquals(EstadoClima.Error("Servicio caído", null), entorno.gestor.estado.value)
    }

    @Test
    fun `elegir el mismo sitio no repite la consulta ni lo guarda`() = runTest {
        val entorno = crear()

        entorno.gestor.seleccionarSitio(sitioInicial)
        advanceUntilIdle()

        assertTrue(entorno.api.sitiosConsultados.isEmpty())
        assertTrue(entorno.sitiosGuardados.isEmpty())
    }

    @Test
    fun `una consulta anterior no pisa el resultado de la consulta nueva`() = runTest {
        val entorno = crear()
        entorno.api.espera = CompletableDeferred()
        entorno.gestor.consultar()
        runCurrent()

        entorno.api.espera = null
        entorno.api.respuesta = ResultadoClima.Exitoso(lecturaNueva)
        entorno.gestor.seleccionarSitio(SitiosClima.LISTA[0])
        advanceUntilIdle()

        assertEquals(EstadoClima.Disponible(lecturaNueva), entorno.gestor.estado.value)
    }

    @Test
    fun `los estados sin datos no exponen lectura`() {
        assertNull(EstadoClima.SinConsultar.lectura)
        assertNull(EstadoClima.Cargando.lectura)
    }

    @Test
    fun `el catalogo de sitios tiene nombres unicos y coordenadas validas`() {
        val nombres = SitiosClima.LISTA.map { it.nombre }
        assertEquals(nombres.size, nombres.toSet().size)
        SitiosClima.LISTA.forEach {
            assertTrue(it.latitud in -90.0..90.0)
            assertTrue(it.longitud in -180.0..180.0)
        }
    }

    @Test
    fun `buscar sitio devuelve el sitio o null si no existe`() {
        assertEquals(SitiosClima.LISTA[0], SitiosClima.buscar(SitiosClima.LISTA[0].nombre))
        assertNull(SitiosClima.buscar("Sitio inexistente"))
        assertNull(SitiosClima.buscar(null))
    }
}
