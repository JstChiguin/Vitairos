package com.example.vitairosapp.datos

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vitairosapp.data.local.db.VitairosDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MigracionBaseDatosTest {

    private val nombreBase = "migracion-prueba.db"

    @get:Rule
    val ayudante = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        VitairosDatabase::class.java
    )

    private fun insertarReporte(db: SupportSQLiteDatabase, id: String, estado: String) {
        db.execSQL(
            "INSERT INTO reports (id, createdAt, user, installId, type, process, area, shift, notes, status, " +
                "attempts, version, syncedAt, receipt, nextRetryAt, updatedAt) VALUES " +
                "('$id', 1000, 'Usuario ficticio', 'INST-TEST', 'Tipo ficticio', 'Proceso ficticio', " +
                "'Area ficticia', 'Dia', 'Notas ficticias', '$estado', 0, 1, NULL, NULL, NULL, 1000)"
        )
    }

    private fun insertarAdjunto(db: SupportSQLiteDatabase, reporte: String, id: String, origen: String) {
        db.execSQL(
            "INSERT INTO attachments (reportId, id, position, name, mimeType, sizeBytes, originalSizeBytes, " +
                "path, sha256, source, addedAt, width, height, pages) VALUES " +
                "('$reporte', '$id', 0, 'ejemplo.pdf', 'application/pdf', 100, 100, NULL, 'hash-$id', " +
                "'$origen', 1000, 0, 0, 1)"
        )
    }

    private fun contar(db: SupportSQLiteDatabase, consulta: String): Int =
        db.query(consulta).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    @Test
    fun migracion1a2ConservaReportesYAdjuntosYClasificaPorModo() {
        ayudante.createDatabase(nombreBase, 1).apply {
            insertarReporte(this, "OPERATIVO-1", "EN_COLA")
            insertarAdjunto(this, "OPERATIVO-1", "adj-op", "PDF")
            insertarReporte(this, "DEMO-1", "SINCRONIZADO")
            insertarAdjunto(this, "DEMO-1", "adj-demo", "DEMO")
            close()
        }

        val db = ayudante.runMigrationsAndValidate(nombreBase, 2, true, VitairosDatabase.MIGRACION_1_2)

        assertEquals(2, contar(db, "SELECT COUNT(*) FROM reports"))
        assertEquals(2, contar(db, "SELECT COUNT(*) FROM attachments"))
        assertEquals(1, contar(db, "SELECT COUNT(*) FROM reports WHERE id = 'OPERATIVO-1' AND modo = 'OPERATIVO'"))
        assertEquals(1, contar(db, "SELECT COUNT(*) FROM reports WHERE id = 'DEMO-1' AND modo = 'DEMO'"))
        assertEquals(1, contar(db, "SELECT COUNT(*) FROM attachments WHERE reportId = 'OPERATIVO-1'"))
        assertEquals(1, contar(db, "SELECT COUNT(*) FROM attachments WHERE reportId = 'DEMO-1'"))
        db.close()
    }

    @Test
    fun migracion1a2ConservaElBorradorEnSuRanura() {
        ayudante.createDatabase(nombreBase, 1).apply {
            execSQL("INSERT INTO draft (slot, reportJson, updatedAt) VALUES (0, '{\"id\":\"B1\"}', 1000)")
            close()
        }

        val db = ayudante.runMigrationsAndValidate(nombreBase, 2, true, VitairosDatabase.MIGRACION_1_2)

        assertEquals(1, contar(db, "SELECT COUNT(*) FROM draft WHERE slot = 0"))
        db.close()
    }

    @Test
    fun migracion1a2DeUnaBaseVaciaEsValida() {
        ayudante.createDatabase(nombreBase, 1).close()

        val db = ayudante.runMigrationsAndValidate(nombreBase, 2, true, VitairosDatabase.MIGRACION_1_2)

        assertEquals(0, contar(db, "SELECT COUNT(*) FROM reports"))
        db.close()
    }
}
