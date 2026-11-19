package com.example.vitairosapp.datos

import com.example.vitairosapp.data.local.ReportJson
import com.example.vitairosapp.data.local.db.entity.DraftEntity
import com.example.vitairosapp.data.model.Attachment
import com.example.vitairosapp.data.model.AttachmentSource
import com.example.vitairosapp.data.model.Report
import com.example.vitairosapp.data.model.ReportStatus
import com.example.vitairosapp.data.repository.ReportRepository
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RepositorioReportesTest {

    @get:Rule
    val carpetaTemporal = TemporaryFolder()

    private val dao = DaoReportesEnMemoria()

    private fun crearRepositorio() = ReportRepository(dao, carpetaTemporal.root)

    private fun reporte(
        id: String,
        estado: ReportStatus = ReportStatus.BORRADOR,
        creadoEn: Long = 1_000L,
    ) = Report(
        id = id,
        createdAt = creadoEn,
        user = "Usuario ficticio",
        installId = "INST-TEST",
        type = "Tipo ficticio",
        process = "Proceso ficticio",
        status = estado,
        attachments = listOf(
            Attachment(
                id = "adj-$id",
                name = "ejemplo.pdf",
                mimeType = "application/pdf",
                sizeBytes = 100,
                sha256 = "hash-$id",
                source = AttachmentSource.DEMO,
            )
        ),
    ).withLog("Reporte creado en la prueba")

    @Test
    fun `un repositorio nuevo no tiene reportes ni borrador`() {
        val repositorio = crearRepositorio()
        assertTrue(repositorio.reports.value.isEmpty())
        assertNull(repositorio.loadDraft())
    }

    @Test
    fun `upsert agrega un reporte nuevo y lo persiste con adjuntos y trazabilidad`() = runBlocking {
        val repositorio = crearRepositorio()
        repositorio.upsert(reporte("R1", ReportStatus.EN_COLA))

        val guardado = repositorio.find("R1")
        assertNotNull(guardado)
        assertEquals(1, dao.count())

        val recargado = crearRepositorio().find("R1")
        assertEquals(ReportStatus.EN_COLA, recargado?.status)
        assertEquals(1, recargado?.attachments?.size)
        assertEquals(1, recargado?.log?.size)
    }

    @Test
    fun `upsert reemplaza un reporte existente sin duplicarlo`() = runBlocking {
        val repositorio = crearRepositorio()
        repositorio.upsert(reporte("R1", ReportStatus.BORRADOR))
        repositorio.upsert(reporte("R1", ReportStatus.EN_COLA))

        assertEquals(1, repositorio.reports.value.size)
        assertEquals(ReportStatus.EN_COLA, repositorio.find("R1")?.status)
        assertEquals(1, dao.count())
    }

    @Test
    fun `update aplica la transformacion y devuelve el reporte actualizado`() = runBlocking {
        val repositorio = crearRepositorio()
        repositorio.upsert(reporte("R1", ReportStatus.EN_COLA))

        val actualizado = repositorio.update("R1") {
            it.copy(status = ReportStatus.ENVIANDO, attempts = it.attempts + 1)
        }

        assertEquals(ReportStatus.ENVIANDO, actualizado?.status)
        assertEquals(1, repositorio.find("R1")?.attempts)
    }

    @Test
    fun `update devuelve null cuando el reporte no existe`() = runBlocking {
        val repositorio = crearRepositorio()
        assertNull(repositorio.update("INEXISTENTE") { it.copy(attempts = 9) })
    }

    @Test
    fun `delete elimina el reporte de memoria y de la base`() = runBlocking {
        val repositorio = crearRepositorio()
        repositorio.upsert(reporte("R1"))
        repositorio.upsert(reporte("R2"))

        repositorio.delete("R1")

        assertNull(repositorio.find("R1"))
        assertNotNull(repositorio.find("R2"))
        assertEquals(1, dao.count())
    }

    @Test
    fun `addAll y clear administran varios reportes`() = runBlocking {
        val repositorio = crearRepositorio()
        repositorio.addAll(listOf(reporte("R1"), reporte("R2"), reporte("R3")))
        assertEquals(3, repositorio.reports.value.size)
        assertEquals(3, dao.count())

        repositorio.clear()
        assertTrue(repositorio.reports.value.isEmpty())
        assertEquals(0, dao.count())
    }

    @Test
    fun `un envio interrumpido vuelve a la cola al reabrir la app`() = runBlocking {
        val primero = crearRepositorio()
        primero.upsert(reporte("R1", ReportStatus.ENVIANDO).copy(nextRetryAt = 5_000L))

        val reabierto = crearRepositorio().find("R1")

        assertEquals(ReportStatus.EN_COLA, reabierto?.status)
        assertNull(reabierto?.nextRetryAt)
        assertTrue(reabierto?.log?.last()?.message?.contains("interrumpido") == true)
    }

    @Test
    fun `los reportes en otros estados no cambian al reabrir la app`() = runBlocking {
        val primero = crearRepositorio()
        ReportStatus.entries.filter { it != ReportStatus.ENVIANDO }.forEach {
            primero.upsert(reporte("R-${it.name}", it))
        }

        val reabierto = crearRepositorio()

        ReportStatus.entries.filter { it != ReportStatus.ENVIANDO }.forEach {
            assertEquals(it, reabierto.find("R-${it.name}")?.status)
        }
    }

    @Test
    fun `el borrador se guarda, se recupera y se limpia`() = runBlocking {
        val repositorio = crearRepositorio()
        repositorio.saveDraft(reporte("BORRADOR-1"))

        assertEquals("BORRADOR-1", repositorio.loadDraft()?.id)
        assertEquals("BORRADOR-1", crearRepositorio().loadDraft()?.id)

        repositorio.saveDraft(null)
        assertNull(repositorio.loadDraft())
        assertNull(crearRepositorio().loadDraft())
    }

    @Test
    fun `un borrador ilegible se descarta sin romper la carga`() = runBlocking {
        dao.saveDraft(DraftEntity(reportJson = "{no es json"))
        assertNull(crearRepositorio().loadDraft())
    }

    @Test
    fun `se migran los reportes guardados en el archivo json antiguo`() {
        val carpetaDatos = File(carpetaTemporal.root, "data").apply { mkdirs() }
        val archivo = File(carpetaDatos, "reports.json")
        val reporteAntiguo = reporte("ANTIGUO", ReportStatus.EN_COLA)
        archivo.writeText(ReportJson.listToJson(listOf(reporteAntiguo)).toString())

        val repositorio = crearRepositorio()

        assertEquals(ReportStatus.EN_COLA, repositorio.find("ANTIGUO")?.status)
        assertFalse(archivo.exists())
        assertTrue(File(carpetaDatos, "reports.migrated.json").exists())
    }

    @Test
    fun `un archivo json antiguo corrupto se conserva como respaldo`() {
        val carpetaDatos = File(carpetaTemporal.root, "data").apply { mkdirs() }
        File(carpetaDatos, "reports.json").writeText("[ esto no es json")

        val repositorio = crearRepositorio()

        assertTrue(repositorio.reports.value.isEmpty())
        assertFalse(File(carpetaDatos, "reports.json").exists())
        assertTrue(carpetaDatos.listFiles().orEmpty().any { it.name.startsWith("reports.corrupt-") })
    }

    @Test
    fun `no se migra el archivo antiguo si la base ya tiene reportes`() = runBlocking {
        val repositorio = crearRepositorio()
        repositorio.upsert(reporte("ACTUAL"))
        val carpetaDatos = File(carpetaTemporal.root, "data").apply { mkdirs() }
        File(carpetaDatos, "reports.json").writeText(ReportJson.listToJson(listOf(reporte("ANTIGUO"))).toString())

        val reabierto = crearRepositorio()

        assertNotNull(reabierto.find("ACTUAL"))
        assertNull(reabierto.find("ANTIGUO"))
    }
}
