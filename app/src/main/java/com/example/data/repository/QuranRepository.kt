package com.example.data.repository

import android.content.Context
import com.example.data.local.BookmarkEntity
import com.example.data.local.CustomZikrEntity
import com.example.data.local.MemorizationEntity
import com.example.data.local.PrayerLogEntity
import com.example.data.local.QuranDatabase
import com.example.data.local.ReadingHistoryEntity
import com.example.data.local.TasbihEntity
import com.example.data.local.UserNoteEntity
import com.example.data.model.Ayah
import com.example.data.model.DuaItem
import com.example.data.model.Qari
import com.example.data.model.Surah
import com.example.data.quran.QuranDataProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class QuranRepository(private val context: Context) {

    init {
        QuranDataProvider.init(context)
    }

    private val db = QuranDatabase.getDatabase(context)
    private val dao = db.quranDao()

    fun getAllSurahs(): List<Surah> = QuranDataProvider.surahs

    fun getSurahByNumber(number: Int): Surah? =
        QuranDataProvider.surahs.find { it.number == number }

    fun getAyahsForSurah(surahNumber: Int): List<Ayah> =
        QuranDataProvider.getAyahsForSurah(surahNumber, context)

    fun getPagesForSurah(surahNumber: Int): Map<Int, List<Ayah>> =
        QuranDataProvider.getPagesForSurah(surahNumber, context)

    fun getQaris(): List<Qari> = QuranDataProvider.qaris

    fun getDuas(): List<DuaItem> = QuranDataProvider.hisnDuas

    fun searchQuran(query: String): List<SearchResult> {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return emptyList()

        val results = mutableListOf<SearchResult>()
        // 1. Search Surah names
        QuranDataProvider.surahs.forEach { surah ->
            if (surah.nameArabic.contains(cleanQuery, ignoreCase = true) ||
                surah.nameEnglish.contains(cleanQuery, ignoreCase = true) ||
                surah.nameTranslation.contains(cleanQuery, ignoreCase = true)
            ) {
                results.add(
                    SearchResult(
                        type = SearchResultType.SURAH,
                        surahNumber = surah.number,
                        ayahNumber = 1,
                        title = "سُورَةُ ${surah.nameArabic}",
                        snippet = "سورة ${if (surah.revelationType.name == "MAKKI") "مكية" else "مدنية"} • عدد الآيات: ${surah.versesCount} • ص ${surah.startPage}"
                    )
                )
            }
        }

        // 2. Search Ayah texts across the Holy Quran
        for (sNum in 1..114) {
            if (results.size >= 30) break
            val ayahs = getAyahsForSurah(sNum)
            val surah = getSurahByNumber(sNum)
            for (ayah in ayahs) {
                if (ayah.textArabic.contains(cleanQuery) ||
                    ayah.textEnglish.contains(cleanQuery, ignoreCase = true) ||
                    ayah.tafsirMuyassar.contains(cleanQuery)
                ) {
                    results.add(
                        SearchResult(
                            type = SearchResultType.AYAH,
                            surahNumber = sNum,
                            ayahNumber = ayah.ayahNumber,
                            title = "سورة ${surah?.nameArabic ?: ""} - آية ${ayah.ayahNumber}",
                            snippet = ayah.textArabic
                        )
                    )
                    if (results.size >= 30) break
                }
            }
        }
        return results
    }

    // Bookmarks
    val bookmarksFlow: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()

    fun isBookmarked(surahNumber: Int, ayahNumber: Int): Flow<Boolean> =
        dao.isBookmarked(surahNumber, ayahNumber)

    suspend fun toggleBookmark(surahNumber: Int, ayahNumber: Int, surahName: String, ayahText: String) = withContext(Dispatchers.IO) {
        val currentBookmarked = dao.isBookmarked(surahNumber, ayahNumber).firstOrNull() ?: false
        if (currentBookmarked) {
            dao.deleteBookmarkByAyah(surahNumber, ayahNumber)
        } else {
            dao.insertBookmark(
                BookmarkEntity(
                    surahNumber = surahNumber,
                    ayahNumber = ayahNumber,
                    surahName = surahName,
                    ayahText = ayahText
                )
            )
        }
    }

    suspend fun removeBookmark(bookmark: BookmarkEntity) = withContext(Dispatchers.IO) {
        dao.deleteBookmark(bookmark)
    }

    // Reading History
    val lastReadFlow: Flow<ReadingHistoryEntity?> = dao.getLastRead()
    val allHistoryFlow: Flow<List<ReadingHistoryEntity>> = dao.getAllReadingHistory()

    suspend fun updateReadingProgress(surahNumber: Int, ayahNumber: Int, surahName: String, progressPercent: Float) =
        withContext(Dispatchers.IO) {
            dao.saveReadingHistory(
                ReadingHistoryEntity(
                    surahNumber = surahNumber,
                    ayahNumber = ayahNumber,
                    surahName = surahName,
                    lastReadTimestamp = System.currentTimeMillis(),
                    progressPercent = progressPercent
                )
            )
        }

    // Notes
    val notesFlow: Flow<List<UserNoteEntity>> = dao.getAllNotes()

    suspend fun addNote(
        surahNumber: Int,
        ayahNumber: Int,
        surahName: String,
        ayahSnippet: String,
        noteContent: String,
        colorHex: String,
        tag: String
    ) = withContext(Dispatchers.IO) {
        dao.insertNote(
            UserNoteEntity(
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                surahName = surahName,
                ayahSnippet = ayahSnippet,
                noteContent = noteContent,
                colorHex = colorHex,
                tag = tag
            )
        )
    }

    suspend fun deleteNote(note: UserNoteEntity) = withContext(Dispatchers.IO) {
        dao.deleteNote(note)
    }

    // Memorization
    val memorizationFlow: Flow<List<MemorizationEntity>> = dao.getAllMemorization()

    suspend fun updateMemorization(
        surahNumber: Int,
        ayahNumber: Int,
        status: String,
        isSuccess: Boolean = true
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getMemorizationForAyah(surahNumber, ayahNumber)
        val rep = (existing?.repetitions ?: 0) + 1
        val interval = if (isSuccess) {
            when (rep) {
                1 -> 1
                2 -> 3
                3 -> 7
                else -> 14
            }
        } else {
            1
        }
        val nextReviewMillis = System.currentTimeMillis() + (interval * 24L * 60L * 60L * 1000L)
        dao.insertOrUpdateMemorization(
            MemorizationEntity(
                id = existing?.id ?: 0,
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                status = status,
                repetitions = rep,
                lastReviewed = System.currentTimeMillis(),
                nextReview = nextReviewMillis,
                intervalDays = interval
            )
        )
    }

    // Prayer tracker
    fun getPrayerLog(dateString: String): Flow<PrayerLogEntity?> = dao.getPrayerLogForDate(dateString)

    suspend fun savePrayerLog(prayerLog: PrayerLogEntity) = withContext(Dispatchers.IO) {
        dao.insertPrayerLog(prayerLog)
    }

    // Tasbih
    val tasbihFlow: Flow<List<TasbihEntity>> = dao.getAllTasbih()

    suspend fun initDefaultTasbih() = withContext(Dispatchers.IO) {
        val defaults = listOf(
            TasbihEntity("subhanallah", "سُبْحَانَ اللَّهِ", "تنزيه الله وتقديسه عن كل نقص", 0, 33, 0),
            TasbihEntity("alhamdulillah", "الْحَمْدُ لِلَّهِ", "الثناء والحمد والشكر لله رب العالمين", 0, 33, 0),
            TasbihEntity("allahu_akbar", "اللَّهُ أَكْبَرُ", "تعظيم الله وإجلاله فوق كل شيء", 0, 33, 0),
            TasbihEntity("la_ilaha_illallah", "لَا إِلَٰهَ إِلَّا اللَّهُ", "شهادة التوحيد والإخلاص لله تعالى", 0, 100, 0),
            TasbihEntity("astaghfirullah", "أَسْتَغْفِرُ اللَّهَ", "طلب المغفرة والصفح من الذنوب", 0, 100, 0),
            TasbihEntity("salawat", "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ", "الصلاة والسلام على النبي المختار ﷺ", 0, 100, 0)
        )
        dao.insertInitialTasbih(defaults)
    }

    suspend fun incrementTasbih(id: String) = withContext(Dispatchers.IO) {
        val list = dao.getAllTasbih().firstOrNull() ?: return@withContext
        val target = list.find { it.id == id } ?: return@withContext
        val newCurrent = if (target.currentCount + 1 >= target.targetCount) 0 else target.currentCount + 1
        dao.insertOrUpdateTasbih(
            target.copy(
                currentCount = newCurrent,
                totalHistoricalCount = target.totalHistoricalCount + 1
            )
        )
    }

    suspend fun resetTasbih(id: String) = withContext(Dispatchers.IO) {
        val list = dao.getAllTasbih().firstOrNull() ?: return@withContext
        val target = list.find { it.id == id } ?: return@withContext
        dao.insertOrUpdateTasbih(target.copy(currentCount = 0))
    }

    suspend fun addTasbih(phrase: String, translation: String, targetCount: Int) = withContext(Dispatchers.IO) {
        val id = "tasbih_${System.currentTimeMillis()}"
        dao.insertOrUpdateTasbih(
            TasbihEntity(
                id = id,
                arabicPhrase = phrase,
                translation = translation,
                currentCount = 0,
                targetCount = targetCount,
                totalHistoricalCount = 0
            )
        )
    }

    suspend fun deleteTasbih(tasbih: TasbihEntity) = withContext(Dispatchers.IO) {
        dao.deleteTasbih(tasbih)
    }

    // Custom Azkar
    val customAzkarFlow: Flow<List<CustomZikrEntity>> = dao.getAllCustomAzkar()

    suspend fun addCustomZikr(
        titleArabic: String,
        titleEnglish: String,
        categoryArabic: String,
        arabicText: String,
        translation: String,
        targetCount: Int,
        benefits: String
    ) = withContext(Dispatchers.IO) {
        dao.insertCustomZikr(
            CustomZikrEntity(
                titleArabic = titleArabic,
                titleEnglish = titleEnglish,
                categoryArabic = categoryArabic,
                arabicText = arabicText,
                translation = translation,
                targetCount = targetCount,
                currentCount = 0,
                benefits = benefits
            )
        )
    }

    suspend fun incrementCustomZikr(id: Long) = withContext(Dispatchers.IO) {
        val list = dao.getAllCustomAzkar().firstOrNull() ?: return@withContext
        val target = list.find { it.id == id } ?: return@withContext
        val nextCount = if (target.currentCount + 1 >= target.targetCount) target.targetCount else target.currentCount + 1
        dao.updateCustomZikr(target.copy(currentCount = nextCount))
    }

    suspend fun resetCustomZikr(id: Long) = withContext(Dispatchers.IO) {
        val list = dao.getAllCustomAzkar().firstOrNull() ?: return@withContext
        val target = list.find { it.id == id } ?: return@withContext
        dao.updateCustomZikr(target.copy(currentCount = 0))
    }

    suspend fun deleteCustomZikr(zikr: CustomZikrEntity) = withContext(Dispatchers.IO) {
        dao.deleteCustomZikr(zikr)
    }

    suspend fun deleteCustomZikrById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteCustomZikrById(id)
    }
}

data class SearchResult(
    val type: SearchResultType,
    val surahNumber: Int,
    val ayahNumber: Int,
    val title: String,
    val snippet: String
)

enum class SearchResultType {
    SURAH,
    AYAH
}
