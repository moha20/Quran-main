package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BookmarkEntity::class,
        ReadingHistoryEntity::class,
        UserNoteEntity::class,
        MemorizationEntity::class,
        PrayerLogEntity::class,
        TasbihEntity::class,
        CustomZikrEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class QuranDatabase : RoomDatabase() {

    abstract fun quranDao(): QuranDao

    companion object {
        @Volatile
        private var INSTANCE: QuranDatabase? = null

        fun getDatabase(context: Context): QuranDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuranDatabase::class.java,
                    "quran_kareem.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
