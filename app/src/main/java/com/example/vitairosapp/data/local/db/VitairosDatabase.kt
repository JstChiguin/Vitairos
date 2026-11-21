package com.example.vitairosapp.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.vitairosapp.data.local.db.dao.ReportDao
import com.example.vitairosapp.data.local.db.entity.AttachmentEntity
import com.example.vitairosapp.data.local.db.entity.DraftEntity
import com.example.vitairosapp.data.local.db.entity.LogEntryEntity
import com.example.vitairosapp.data.local.db.entity.ReportEntity

/** Base de datos SQLite local de la app (archivo `databases/vitairos.db`). */
@Database(
    entities = [ReportEntity::class, AttachmentEntity::class, LogEntryEntity::class, DraftEntity::class],
    version = 2,
    exportSchema = true
)
abstract class VitairosDatabase : RoomDatabase() {

    abstract fun reportDao(): ReportDao

    companion object {
        private const val NAME = "vitairos.db"

        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reports ADD COLUMN modo TEXT NOT NULL DEFAULT 'OPERATIVO'")
                db.execSQL(
                    "UPDATE reports SET modo = 'DEMO' WHERE id IN " +
                        "(SELECT DISTINCT reportId FROM attachments WHERE source = 'DEMO')"
                )
                db.execSQL("UPDATE draft SET slot = 1 WHERE reportJson LIKE '%\"source\":\"DEMO\"%'")
            }
        }

        @Volatile
        private var instance: VitairosDatabase? = null

        fun get(context: Context): VitairosDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, VitairosDatabase::class.java, NAME)
                    .addMigrations(MIGRACION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
