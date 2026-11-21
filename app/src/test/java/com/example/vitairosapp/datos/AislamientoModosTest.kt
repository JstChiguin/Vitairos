package com.example.vitairosapp.datos

import com.example.vitairosapp.data.local.ReportJson
import com.example.vitairosapp.data.model.Attachment
import com.example.vitairosapp.data.model.AttachmentSource
import com.example.vitairosapp.data.model.ModoApp
import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.ReportStatus
import com.example.vitairosapp.data.repository.ReportRepository
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AislamientoModosTest {

    @get:Rule
    val carpetaTemporal = TemporaryFolder()

    private val dao = DaoReportesEnMemoria()

    private fun abrir(modo: ModoApp = ModoApp.OPERATIVO) =
        ReportRepository(dao, carpetaTemporal.root, modo)

    private fun reporte(id: String, modo: ModoApp, estado: ReportStatus = ReportStatus.EN_COLA) = Report(
        id = id,
        createdAt = 1_000L,
        user = "Usuario ficticio",
        installId = "INST-TEST",
        type = "Tipo ficticio",
        process = "Proceso ficticio",
        status = estado,
        modo = modo,
        attachments = listOf(
            Attachment(
                id = "adj-$id",
                name = "ejemplo-$id.pdf",
                mimeType = "application/pdf",
                sizeBytes = 100,
                sha256 = "hash-$id",
                source = AttachmentSource.PDF,
            )
        ),
    ).withLog("Creado en la prueba")

    private suspend fun repositorioConAmbosModos(): ReportRepository {
        val repositorio = abrir()
        repositorio.upsert(reporte("OP-1", ModoApp.OPERATIVO))
        repositorio.upsert(reporte("OP-2", ModoApp.OPERATIVO, ReportStatus.BORRADOR))
        repositorio.upsert(reporte("DEMO-1", ModoApp.DEMO))
        return repositorio
    }

    @Test
    fun `el modo operativo solo muestra reportes operativos`() = runBlocking {
        val repositorio = repositorioConAmbosModos()
        assertEquals(setOf("OP-1", "OP-2"), repositorio.reports.value.map { it.id }.toSet())
    }

    @Test
    fun `entrar al modo demo solo muestra reportes de demostracion`() = runBlocking {
        val repositorio = repositorioConAmbosModos()
        repositorio.cambiarModo(ModoApp.DEMO)
        assertEquals(listOf("DEMO-1"), repositorio.reports.value.map { it.id })
        assertNull(repositorio.find("OP-1"))
    }

    @Test
    fun `salir del modo demo restaura los reportes operativos`() = runBlocking {
        val repositorio = repositorioConAmbosModos()
        repositorio.cambiarModo(ModoApp.DEMO)
        repositorio.cambiarModo(ModoApp.OPERATIVO)
        assertEquals(setOf("OP-1", "OP-2"), repositorio.reports.value.map { it.id }.toSet())
        assertNull(repositorio.find("DEMO-1"))
    }

    @Test
    fun `entrar y salir del modo demo no modifica la base operativa`() = runBlocking {
        val repositorio = repositorioConAmbosModos()
        val antes = dao.getAll().filter { it.report.modo == ModoApp.OPERATIVO }

        repositorio.cambiarModo(ModoApp.DEMO)
        repositorio.cambiarModo(ModoApp.OPERATIVO)

        val despues = dao.getAll().filter { it.report.modo == ModoApp.OPERATIVO }
        assertEquals(antes, despues)
    }

    @Test
    fun `eliminar en modo demo no puede borrar un reporte operativo`() = runBlocking {
        val repositorio = repositorioConAmbosModos()
        repositorio.cambiarModo(ModoApp.DEMO)

        repositorio.delete("OP-1")

        assertEquals(3, dao.count())
        repositorio.cambiarModo(ModoApp.OPERATIVO)
        assertNotNull(repositorio.find("OP-1"))
    }

    @Test
    fun `eliminar en modo operativo no puede borrar un reporte de demostracion`() = runBlocking {
        val repositorio = repositorioConAmbosModos()

        repositorio.delete("DEMO-1")

        assertEquals(3, dao.count())
    }

    @Test
    fun `restablecer la demo solo elimina reportes de demostracion y conserva adjuntos operativos`() = runBlocking {
        val repositorio = repositorioConAmbosModos()
        repositorio.cambiarModo(ModoApp.DEMO)

        repositorio.clear()

        assertTrue(repositorio.reports.value.isEmpty())
        repositorio.cambiarModo(ModoApp.OPERATIVO)
        assertEquals(setOf("OP-1", "OP-2"), repositorio.reports.value.map { it.id }.toSet())
        assertEquals("adj-OP-1", repositorio.find("OP-1")?.attachments?.single()?.id)
        assertEquals(2, dao.count())
        assertTrue(dao.getAll().none { it.report.modo == ModoApp.DEMO })
    }

    @Test
    fun `vaciar el modo operativo conserva los reportes de demostracion`() = runBlocking {
        val repositorio = repositorioConAmbosModos()

        repositorio.clear()

        assertTrue(repositorio.reports.value.isEmpty())
        repositorio.cambiarModo(ModoApp.DEMO)
        assertEquals("adj-DEMO-1", repositorio.find("DEMO-1")?.attachments?.single()?.id)
    }

    @Test
    fun `la separacion persiste al reabrir la aplicacion en cada modo`() = runBlocking {
        repositorioConAmbosModos()

        val operativo = abrir(ModoApp.OPERATIVO)
        val demo = abrir(ModoApp.DEMO)

        assertEquals(setOf("OP-1", "OP-2"), operativo.reports.value.map { it.id }.toSet())
        assertEquals(listOf("DEMO-1"), demo.reports.value.map { it.id })
    }

    @Test
    fun `los adjuntos conservan su asociacion con el reporte tras cambiar de modo y reabrir`() = runBlocking {
        val repositorio = repositorioConAmbosModos()
        repositorio.cambiarModo(ModoApp.DEMO)
        repositorio.cambiarModo(ModoApp.OPERATIVO)

        val reabierto = abrir()
        reabierto.reports.value.forEach { assertEquals("adj-${it.id}", it.attachments.single().id) }
        assertEquals("adj-DEMO-1", abrir(ModoApp.DEMO).find("DEMO-1")?.attachments?.single()?.id)
    }

    @Test
    fun `actualizar un reporte operativo desde una tarea pendiente no lo mezcla con la demo`() = runBlocking {
        val repositorio = repositorioConAmbosModos()
        repositorio.cambiarModo(ModoApp.DEMO)

        repositorio.update("OP-1") { it.copy(attempts = 7) }

        assertEquals(listOf("DEMO-1"), repositorio.reports.value.map { it.id })
        repositorio.cambiarModo(ModoApp.OPERATIVO)
        assertEquals(7, repositorio.find("OP-1")?.attempts)
    }

    @Test
    fun `cada modo conserva su propio borrador`() = runBlocking {
        val repositorio = abrir()
        repositorio.saveDraft(reporte("BORRADOR-OP", ModoApp.OPERATIVO, ReportStatus.BORRADOR))
        repositorio.cambiarModo(ModoApp.DEMO)
        assertNull(repositorio.loadDraft())

        repositorio.saveDraft(reporte("BORRADOR-DEMO", ModoApp.DEMO, ReportStatus.BORRADOR))
        repositorio.cambiarModo(ModoApp.OPERATIVO)
        assertEquals("BORRADOR-OP", repositorio.loadDraft()?.id)

        assertEquals("BORRADOR-OP", abrir(ModoApp.OPERATIVO).loadDraft()?.id)
        assertEquals("BORRADOR-DEMO", abrir(ModoApp.DEMO).loadDraft()?.id)
    }

    @Test
    fun `descartar el borrador de un modo no afecta el del otro`() = runBlocking {
        val repositorio = abrir()
        repositorio.saveDraft(reporte("BORRADOR-OP", ModoApp.OPERATIVO, ReportStatus.BORRADOR))
        repositorio.cambiarModo(ModoApp.DEMO)
        repositorio.saveDraft(reporte("BORRADOR-DEMO", ModoApp.DEMO, ReportStatus.BORRADOR))

        repositorio.saveDraft(null)

        assertNull(abrir(ModoApp.DEMO).loadDraft())
        assertEquals("BORRADOR-OP", abrir(ModoApp.OPERATIVO).loadDraft()?.id)
    }

    @Test
    fun `los identificadores referenciados incluyen reportes y borradores de ambos modos`() = runBlocking {
        val repositorio = repositorioConAmbosModos()
        repositorio.saveDraft(reporte("BORRADOR-OP", ModoApp.OPERATIVO, ReportStatus.BORRADOR))
        repositorio.cambiarModo(ModoApp.DEMO)
        repositorio.saveDraft(reporte("BORRADOR-DEMO", ModoApp.DEMO, ReportStatus.BORRADOR))

        assertEquals(
            setOf("OP-1", "OP-2", "DEMO-1", "BORRADOR-OP", "BORRADOR-DEMO"),
            repositorio.idsReferenciados()
        )
    }

    @Test
    fun `el modo se serializa y un json sin modo se interpreta como operativo`() {
        val demo = reporte("DEMO-1", ModoApp.DEMO)
        assertEquals(ModoApp.DEMO, ReportJson.fromJson(ReportJson.toJson(demo)).modo)

        val sinModo = ReportJson.toJson(demo).apply { remove("modo") }
        assertEquals(ModoApp.OPERATIVO, ReportJson.fromJson(JSONObject(sinModo.toString())).modo)
    }

    @Test
    fun `un reporte operativo sigue disponible sin conexion tras reabrir la app`() = runBlocking {
        val repositorio = abrir()
        repositorio.upsert(reporte("OP-OFFLINE", ModoApp.OPERATIVO))

        val reabierto = abrir()

        assertEquals(ReportStatus.EN_COLA, reabierto.find("OP-OFFLINE")?.status)
    }

    @Test
    fun `un borrador guardado en modo operativo conserva el modo operativo al reabrir`() = runBlocking {
        val repositorio = abrir()
        repositorio.saveDraft(reporte("BORRADOR-OP", ModoApp.OPERATIVO, ReportStatus.BORRADOR))

        assertEquals(ModoApp.OPERATIVO, abrir().loadDraft()?.modo)
    }

    @Test
    fun `la ranura del borrador define su modo aunque el json diga otro`() = runBlocking {
        val repositorio = abrir(ModoApp.DEMO)
        repositorio.saveDraft(reporte("BORRADOR-DEMO", ModoApp.OPERATIVO, ReportStatus.BORRADOR))

        val reabierto = abrir(ModoApp.DEMO)

        assertEquals(ModoApp.DEMO, reabierto.loadDraft()?.modo)
    }

    @Test
    fun `un borrador demo con json sin modo no pasa a operativo`() = runBlocking {
        val repositorio = abrir(ModoApp.DEMO)
        val json = ReportJson.toJson(reporte("BORRADOR-DEMO", ModoApp.DEMO, ReportStatus.BORRADOR)).apply { remove("modo") }
        dao.saveDraft(
            com.example.vitairosapp.data.local.db.entity.DraftEntity(
                slot = ModoApp.DEMO.ranuraBorrador, reportJson = json.toString()
            )
        )

        assertEquals(ModoApp.DEMO, abrir(ModoApp.DEMO).loadDraft()?.modo)
        assertNull(repositorio.find("BORRADOR-DEMO"))
    }

    @Test
    fun `un reporte antiguo sin evidencia de origen permanece operativo y no se reclasifica`() = runBlocking {
        val antiguo = reporte("ANTIGUO", ModoApp.OPERATIVO)
        abrir().upsert(antiguo)

        val reabierto = abrir()
        reabierto.cambiarModo(ModoApp.DEMO)
        reabierto.cambiarModo(ModoApp.OPERATIVO)

        assertEquals(ModoApp.OPERATIVO, reabierto.find("ANTIGUO")?.modo)
        assertTrue(dao.getAll().none { it.report.modo == ModoApp.DEMO })
    }

    @Test
    fun `un reporte guardado en un modo nunca aparece en la lista del otro tras reabrir`() = runBlocking {
        val repositorio = repositorioConAmbosModos()

        val demo = abrir(ModoApp.DEMO)
        val operativo = abrir(ModoApp.OPERATIVO)

        assertEquals(listOf("DEMO-1"), demo.reports.value.map { it.id })
        assertTrue(operativo.reports.value.none { it.modo == ModoApp.DEMO })
        assertTrue(demo.reports.value.none { it.modo == ModoApp.OPERATIVO })
        assertEquals(3, repositorio.idsReferenciados().size)
    }

    @Test
    fun `un reporte enviado se conserva en el historial del modo`() = runBlocking {
        val repositorio = abrir(ModoApp.DEMO)
        repositorio.upsert(reporte("DEMO-ENVIADO", ModoApp.DEMO, ReportStatus.SINCRONIZADO))

        assertEquals(ReportStatus.SINCRONIZADO, abrir(ModoApp.DEMO).find("DEMO-ENVIADO")?.status)
    }

    @Test
    fun `un envio simulado exitoso conserva el reporte y su trazabilidad`() = runBlocking {
        val repositorio = abrir(ModoApp.DEMO)
        repositorio.upsert(reporte("DEMO-ENVIO", ModoApp.DEMO))

        repositorio.update("DEMO-ENVIO") {
            it.copy(status = ReportStatus.SINCRONIZADO, receipt = "RCV-1", syncedAt = 5L)
                .withLog("Envío simulado completado")
        }

        val guardado = abrir(ModoApp.DEMO).find("DEMO-ENVIO")
        assertEquals(ReportStatus.SINCRONIZADO, guardado?.status)
        assertEquals("Envío simulado completado", guardado?.log?.last()?.message)
        assertEquals(2, guardado?.log?.size)
    }
}
