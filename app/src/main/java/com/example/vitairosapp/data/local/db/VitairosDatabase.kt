package com.example.vitairosapp.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.vitairosapp.data.local.db.dao.ReportDao
import com.example.vitairosapp.data.local.db.entity.AttachmentEntity
import com.example.vitairosapp.data.local.db.entity.DraftEntity
import com.example.vitairosapp.data.local.db.entity.LogEntryEntity
import com.example.vitairosapp.data.local.db.entity.ReportEntity

/** Base de datos SQLite local de la app (archivo `databases/vitairos.db`). */
@Database(
    entities = [ReportEntity::class, AttachmentEntity::class, LogEntryEntity::class, DraftEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VitairosDatabase : RoomDatabase() {

    abstract fun reportDao(): ReportDao

    companion object {
        private const val NAME = "vitairos.db"

        @Volatile
        private var instance: VitairosDatabase? = null

        fun get(context: Context): VitairosDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, VitairosDatabase::class.java, NAME)
                    .build()
                    .also { instance = it }
            }
    }
}
