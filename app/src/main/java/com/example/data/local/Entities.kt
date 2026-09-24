package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val ayahText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "reading_history")
data class ReadingHistoryEntity(
    @PrimaryKey
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val lastReadTimestamp: Long = System.currentTimeMillis(),
    val progressPercent: Float = 0f
)

@Entity(tableName = "user_notes")
data class UserNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val ayahSnippet: String,
    val noteContent: String,
    val colorHex: String = "#0F5A3E",
    val tag: String = "تدبر", // reflection, fiqh, memorization, etc.
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "memorization_progress")
data class MemorizationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val surahNumber: Int,
    val ayahNumber: Int,
    val status: String = "NOT_STARTED", // NOT_STARTED, IN_PROGRESS, MEMORIZED, NEEDS_REVIEW
    val repetitions: Int = 0,
    val lastReviewed: Long = System.currentTimeMillis(),
    val nextReview: Long = System.currentTimeMillis(),
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f
)

@Entity(tableName = "prayer_logs")
data class PrayerLogEntity(
    @PrimaryKey
    val dateString: String, // YYYY-MM-DD
    val fajr: Boolean = false,
    val dhuhr: Boolean = false,
    val asr: Boolean = false,
    val maghrib: Boolean = false,
    val isha: Boolean = false,
    val qiyam: Boolean = false
)

@Entity(tableName = "tasbih_logs")
data class TasbihEntity(
    @PrimaryKey
    val id: String, // e.g. "subhanallah", "alhamdulillah", "allahu_akbar", "astaghfirullah"
    val arabicPhrase: String,
    val translation: String,
    val currentCount: Int = 0,
    val targetCount: Int = 33,
    val totalHistoricalCount: Long = 0
)

@Entity(tableName = "custom_azkar")
data class CustomZikrEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titleArabic: String,
    val titleEnglish: String = "",
    val categoryArabic: String = "أذكار مخصصة",
    val arabicText: String,
    val translation: String = "",
    val targetCount: Int = 33,
    val currentCount: Int = 0,
    val benefits: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
