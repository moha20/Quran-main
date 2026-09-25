package com.mosalah.quran.data.model

data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val nameTranslation: String,
    val versesCount: Int,
    val revelationType: RevelationType,
    val startPage: Int,
    val juzNumber: Int
)

enum class RevelationType {
    MAKKI,
    MADANI
}

data class Ayah(
    val surahNumber: Int,
    val ayahNumber: Int,
    val textArabic: String,
    val textEnglish: String,
    val textFrench: String = "",
    val textUrdu: String = "",
    val tafsirMuyassar: String = "",
    val tafsirIbnKathir: String = "",
    val tafsirSaadi: String = "",
    val words: List<WordSegment> = emptyList(),
    val tajweedAnnotations: List<TajweedSpan> = emptyList(),
    val page: Int = 0
)

data class WordSegment(
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val root: String = "",
    val grammarType: String = ""
)

data class TajweedSpan(
    val startChar: Int,
    val endChar: Int,
    val ruleType: TajweedRuleType
)

enum class TajweedRuleType {
    QALQALAH,
    GHUNNAH,
    IKHFA,
    IDGHAM,
    MADD
}

data class Qari(
    val id: String,
    val nameArabic: String,
    val nameEnglish: String,
    val style: String,
    val audioSubfolder: String, // e.g. "Alafasy_128kbps", "AbdulSamad_64kbps_QuranExplorer.Com"
    val country: String
)

data class DuaItem(
    val id: Int,
    val category: String,
    val categoryArabic: String,
    val titleArabic: String,
    val titleEnglish: String,
    val arabicText: String,
    val translation: String,
    val reference: String,
    val targetCount: Int = 1,
    val benefits: String = ""
)

data class KhatmaPlan(
    val totalDays: Int = 30,
    val currentPage: Int = 1,
    val targetPagesPerDay: Int = 20,
    val startDate: Long = System.currentTimeMillis()
)
