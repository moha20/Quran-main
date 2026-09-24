package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BookmarkEntity
import com.example.data.local.CustomZikrEntity
import com.example.data.local.MemorizationEntity
import com.example.data.local.PrayerLogEntity
import com.example.data.local.ReadingHistoryEntity
import com.example.data.local.TasbihEntity
import com.example.data.local.UserNoteEntity
import com.example.data.model.Ayah
import com.example.data.model.DuaItem
import com.example.data.model.Qari
import com.example.data.model.RevelationType
import com.example.data.model.Surah
import com.example.data.quran.QuranDataProvider
import com.example.data.repository.QuranRepository
import com.example.data.repository.SearchResult
import com.example.data.repository.TafsirRepository
import com.example.data.repository.TafsirType
import com.example.service.audio.AudioPlayerUiState
import com.example.service.audio.QuranAudioPlayer
import com.example.service.prayer.CityPreset
import com.example.service.prayer.OnlinePrayerService
import com.example.service.prayer.PrayerTimeCalculator
import com.example.service.prayer.PrayerTimes
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SurahFilterType {
    ALL,
    MAKKI,
    MADANI,
    BOOKMARKED
}

enum class ReaderViewMode {
    PAGES, // عرض صفحات المصحف
    AYAHS  // عرض الآيات قائمة
}

data class QuranUiState(
    val selectedSurahNumber: Int = 1,
    val selectedAyahIndex: Int = 1,
    val searchQuery: String = "",
    val searchResults: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val surahFilter: SurahFilterType = SurahFilterType.ALL,
    val selectedCity: CityPreset = PrayerTimeCalculator.cities.first(),
    val prayerTimes: PrayerTimes = PrayerTimeCalculator.calculatePrayerTimes(PrayerTimeCalculator.cities.first()),
    val hijriDate: String = PrayerTimeCalculator.getHijriDateString(),
    val qiblaAngle: Double = PrayerTimeCalculator.calculateQiblaAngle(PrayerTimeCalculator.cities.first().latitude, PrayerTimeCalculator.cities.first().longitude),
    val arabicFontSize: Float = 26f,
    val translationFontSize: Float = 15f,
    val selectedTranslation: String = "ar_only", // "ar_only" (Arabic text only), "ar_en", "ar_fr", "ar_ur"
    val readerViewMode: ReaderViewMode = ReaderViewMode.PAGES,
    val activeTafsirAyah: Ayah? = null,
    val activeWordAnalysisAyah: Ayah? = null,
    val activeShareAyah: Ayah? = null,
    val activeNoteAyah: Ayah? = null,
    val khatmaPlanDays: Int = 30,
    val khatmaCurrentPage: Int = 15,
    val memorizationDailyTarget: Int = 5,
    val memorizationFlashcardIndex: Int = 0,
    val flashcardRevealed: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT
)

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    val repository = QuranRepository(application)
    val tafsirRepository = TafsirRepository(application)
    val audioPlayer = QuranAudioPlayer(application)
    val onlinePrayerService = OnlinePrayerService(application)
    private val prefs = application.getSharedPreferences("quran_app_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    private val _isUpdatingPrayerTimes = MutableStateFlow(false)
    val isUpdatingPrayerTimes: StateFlow<Boolean> = _isUpdatingPrayerTimes.asStateFlow()

    val audioState: StateFlow<AudioPlayerUiState> = audioPlayer.uiState

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastRead: StateFlow<ReadingHistoryEntity?> = repository.lastReadFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val notes: StateFlow<List<UserNoteEntity>> = repository.notesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memorizationList: StateFlow<List<MemorizationEntity>> = repository.memorizationFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasbihList: StateFlow<List<TasbihEntity>> = repository.tasbihFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customAzkar: StateFlow<List<CustomZikrEntity>> = repository.customAzkarFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _duaCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val duaCounts: StateFlow<Map<Int, Int>> = _duaCounts.asStateFlow()

    private val todayDateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val todayPrayerLog: StateFlow<PrayerLogEntity?> = repository.getPrayerLog(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        val savedTheme = prefs.getString("theme_mode", AppThemeMode.LIGHT.name)
        val initialTheme = try {
            AppThemeMode.valueOf(savedTheme ?: AppThemeMode.LIGHT.name)
        } catch (_: Exception) {
            AppThemeMode.LIGHT
        }
        val savedTranslation = prefs.getString("selected_translation", "ar_only") ?: "ar_only"
        _uiState.value = _uiState.value.copy(
            themeMode = initialTheme,
            selectedTranslation = savedTranslation
        )

        viewModelScope.launch {
            repository.initDefaultTasbih()
        }

        // Automatic internet prayer times update on app start
        viewModelScope.launch {
            refreshPrayerTimesAuto(isInitial = true)
        }

        // Real-time prayer countdown ticker (updates every 60 seconds)
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                updatePrayerCountdown()
            }
        }
    }

    fun getAllSurahs(): List<Surah> = repository.getAllSurahs()

    fun getFilteredSurahs(
        filter: SurahFilterType = _uiState.value.surahFilter,
        currentBookmarks: List<BookmarkEntity> = bookmarks.value
    ): List<Surah> {
        val all = repository.getAllSurahs()
        return when (filter) {
            SurahFilterType.ALL -> all
            SurahFilterType.MAKKI -> all.filter { it.revelationType == RevelationType.MAKKI }
            SurahFilterType.MADANI -> all.filter { it.revelationType == RevelationType.MADANI }
            SurahFilterType.BOOKMARKED -> {
                val bookmarkedSurahNums = currentBookmarks.map { it.surahNumber }.toSet()
                all.filter { bookmarkedSurahNums.contains(it.number) }
            }
        }
    }

    suspend fun getTafsir(
        tafsirType: TafsirType,
        surahNumber: Int,
        ayahNumber: Int,
        fallbackMuyassar: String = ""
    ): Result<String> = tafsirRepository.getTafsir(tafsirType, surahNumber, ayahNumber, fallbackMuyassar)

    fun getAyahsForSurah(surahNumber: Int): List<Ayah> =
        repository.getAyahsForSurah(surahNumber)

    fun getPagesForSurah(surahNumber: Int): Map<Int, List<Ayah>> =
        repository.getPagesForSurah(surahNumber)

    fun setReaderViewMode(mode: ReaderViewMode) {
        _uiState.value = _uiState.value.copy(readerViewMode = mode)
    }

    fun selectSurah(surahNumber: Int, ayahNumber: Int = 1) {
        _uiState.value = _uiState.value.copy(
            selectedSurahNumber = surahNumber,
            selectedAyahIndex = ayahNumber
        )
        val surah = repository.getSurahByNumber(surahNumber)
        surah?.let {
            viewModelScope.launch {
                val progress = ayahNumber.toFloat() / it.versesCount.toFloat()
                repository.updateReadingProgress(it.number, ayahNumber, it.nameArabic, progress)
            }
        }
    }

    fun setFilter(filter: SurahFilterType) {
        _uiState.value = _uiState.value.copy(surahFilter = filter)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            isSearching = query.isNotBlank(),
            searchResults = if (query.isNotBlank()) repository.searchQuran(query) else emptyList()
        )
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "", isSearching = false, searchResults = emptyList())
    }

    // Audio Controls
    fun playAyahAudio(surahNumber: Int, ayahNumber: Int) {
        audioPlayer.playAyah(surahNumber, ayahNumber)
    }

    fun toggleAudioPlayback() {
        audioPlayer.togglePlayPause()
    }

    fun setQari(qari: Qari) {
        audioPlayer.setQari(qari)
    }

    fun setAudioSpeed(speed: Float) {
        audioPlayer.setSpeed(speed)
    }

    fun setRepeatCount(count: Int) {
        audioPlayer.setRepeatCount(count)
    }

    // Bookmarks
    fun toggleBookmark(surahNumber: Int, ayahNumber: Int, surahName: String, ayahText: String) {
        viewModelScope.launch {
            repository.toggleBookmark(surahNumber, ayahNumber, surahName, ayahText)
        }
    }

    fun removeBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.removeBookmark(bookmark)
        }
    }

    // Notes
    fun addNote(
        surahNumber: Int,
        ayahNumber: Int,
        surahName: String,
        ayahSnippet: String,
        noteContent: String,
        colorHex: String,
        tag: String
    ) {
        viewModelScope.launch {
            repository.addNote(surahNumber, ayahNumber, surahName, ayahSnippet, noteContent, colorHex, tag)
            _uiState.value = _uiState.value.copy(activeNoteAyah = null)
        }
    }

    fun deleteNote(note: UserNoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // Dialogs & Modals
    fun showTafsirDialog(ayah: Ayah?) {
        _uiState.value = _uiState.value.copy(activeTafsirAyah = ayah)
    }

    fun showWordAnalysisDialog(ayah: Ayah?) {
        _uiState.value = _uiState.value.copy(activeWordAnalysisAyah = ayah)
    }

    fun showShareDialog(ayah: Ayah?) {
        _uiState.value = _uiState.value.copy(activeShareAyah = ayah)
    }

    fun showNoteDialog(ayah: Ayah?) {
        _uiState.value = _uiState.value.copy(activeNoteAyah = ayah)
    }

    // Display Font Size & Translation Settings
    fun updateFontSize(arabic: Float, translation: Float) {
        _uiState.value = _uiState.value.copy(
            arabicFontSize = arabic,
            translationFontSize = translation
        )
    }

    fun setTranslationLanguage(lang: String) {
        _uiState.value = _uiState.value.copy(selectedTranslation = lang)
        prefs.edit().putString("selected_translation", lang).apply()
    }

    // Memorization / Hifz
    fun recordFlashcardReview(surahNumber: Int, ayahNumber: Int, remembered: Boolean) {
        viewModelScope.launch {
            val status = if (remembered) "MEMORIZED" else "NEEDS_REVIEW"
            repository.updateMemorization(surahNumber, ayahNumber, status, isSuccess = remembered)
            val currentIdx = _uiState.value.memorizationFlashcardIndex
            _uiState.value = _uiState.value.copy(
                memorizationFlashcardIndex = currentIdx + 1,
                flashcardRevealed = false
            )
        }
    }

    fun toggleFlashcardReveal() {
        _uiState.value = _uiState.value.copy(flashcardRevealed = !_uiState.value.flashcardRevealed)
    }

    // Prayer & Qibla
    fun selectCity(city: CityPreset) {
        val pt = PrayerTimeCalculator.calculatePrayerTimes(city)
        val qibla = PrayerTimeCalculator.calculateQiblaAngle(city.latitude, city.longitude)
        _uiState.value = _uiState.value.copy(
            selectedCity = city,
            prayerTimes = pt,
            qiblaAngle = qibla
        )

        // Automatically fetch accurate online times for the selected city
        viewModelScope.launch {
            _isUpdatingPrayerTimes.value = true
            val onlineResult = onlinePrayerService.fetchPrayerTimesForCity(city)
            if (onlineResult != null) {
                _uiState.value = _uiState.value.copy(
                    prayerTimes = onlineResult.prayerTimes,
                    hijriDate = onlineResult.hijriDate,
                    qiblaAngle = onlineResult.qiblaAngle
                )
            }
            _isUpdatingPrayerTimes.value = false
        }
    }

    fun selectAutoLocation() {
        viewModelScope.launch {
            refreshPrayerTimesAuto(isInitial = false)
        }
    }

    fun refreshPrayerTimesManual() {
        viewModelScope.launch {
            val currentCity = _uiState.value.selectedCity
            if (currentCity.latitude == 0.0 && currentCity.longitude == 0.0) {
                refreshPrayerTimesAuto(isInitial = false)
            } else {
                _isUpdatingPrayerTimes.value = true
                val onlineResult = onlinePrayerService.fetchPrayerTimesForCity(currentCity)
                if (onlineResult != null) {
                    _uiState.value = _uiState.value.copy(
                        prayerTimes = onlineResult.prayerTimes,
                        hijriDate = onlineResult.hijriDate,
                        qiblaAngle = onlineResult.qiblaAngle
                    )
                }
                _isUpdatingPrayerTimes.value = false
            }
        }
    }

    private suspend fun refreshPrayerTimesAuto(isInitial: Boolean) {
        _isUpdatingPrayerTimes.value = true
        val onlineResult = onlinePrayerService.autoDetectAndFetch()
        if (onlineResult != null) {
            _uiState.value = _uiState.value.copy(
                prayerTimes = onlineResult.prayerTimes,
                hijriDate = onlineResult.hijriDate,
                qiblaAngle = onlineResult.qiblaAngle,
                selectedCity = CityPreset(
                    nameArabic = onlineResult.cityName,
                    nameEnglish = onlineResult.cityName,
                    latitude = onlineResult.latitude,
                    longitude = onlineResult.longitude,
                    timezone = 0.0
                )
            )
        } else if (!isInitial && !_uiState.value.prayerTimes.isFromInternet) {
            // Recalculate with offline formula if online was not reachable
            val fallbackCity = _uiState.value.selectedCity
            val pt = PrayerTimeCalculator.calculatePrayerTimes(fallbackCity)
            _uiState.value = _uiState.value.copy(prayerTimes = pt)
        }
        _isUpdatingPrayerTimes.value = false
    }

    fun updatePrayerCountdown() {
        val currentPt = _uiState.value.prayerTimes
        val (nextName, remainingMins) = PrayerTimeCalculator.calculateNextPrayer(
            fajr = currentPt.fajr,
            sunrise = currentPt.sunrise,
            dhuhr = currentPt.dhuhr,
            asr = currentPt.asr,
            maghrib = currentPt.maghrib,
            isha = currentPt.isha
        )
        if (currentPt.nextPrayerName != nextName || currentPt.nextPrayerRemainingMinutes != remainingMins) {
            _uiState.value = _uiState.value.copy(
                prayerTimes = currentPt.copy(
                    nextPrayerName = nextName,
                    nextPrayerRemainingMinutes = remainingMins
                )
            )
        }
    }

    fun togglePrayerCompleted(prayerName: String) {
        viewModelScope.launch {
            val current = todayPrayerLog.value ?: PrayerLogEntity(dateString = todayDateString)
            val updated = when (prayerName) {
                "fajr" -> current.copy(fajr = !current.fajr)
                "dhuhr" -> current.copy(dhuhr = !current.dhuhr)
                "asr" -> current.copy(asr = !current.asr)
                "maghrib" -> current.copy(maghrib = !current.maghrib)
                "isha" -> current.copy(isha = !current.isha)
                "qiyam" -> current.copy(qiyam = !current.qiyam)
                else -> current
            }
            repository.savePrayerLog(updated)
        }
    }

    // Tasbih
    fun incrementTasbih(id: String) {
        viewModelScope.launch {
            repository.incrementTasbih(id)
        }
    }

    fun resetTasbih(id: String) {
        viewModelScope.launch {
            repository.resetTasbih(id)
        }
    }

    fun addTasbih(phrase: String, translation: String, targetCount: Int) {
        viewModelScope.launch {
            repository.addTasbih(phrase, translation, targetCount)
        }
    }

    fun deleteTasbih(tasbih: TasbihEntity) {
        viewModelScope.launch {
            repository.deleteTasbih(tasbih)
        }
    }

    // Interactive Hisn al-Muslim Adhkar Counting
    fun incrementDuaCount(duaId: Int, targetCount: Int) {
        val current = _duaCounts.value[duaId] ?: 0
        val next = if (current >= targetCount) targetCount else current + 1
        _duaCounts.value = _duaCounts.value + (duaId to next)
    }

    fun resetDuaCount(duaId: Int) {
        _duaCounts.value = _duaCounts.value + (duaId to 0)
    }

    fun resetCategoryDuaCounts(categoryArabic: String) {
        val duas = repository.getDuas().filter { it.categoryArabic == categoryArabic }
        val updated = _duaCounts.value.toMutableMap()
        duas.forEach { updated[it.id] = 0 }
        _duaCounts.value = updated
    }

    // Custom Azkar
    fun addCustomZikr(
        titleArabic: String,
        titleEnglish: String,
        categoryArabic: String,
        arabicText: String,
        translation: String,
        targetCount: Int,
        benefits: String
    ) {
        viewModelScope.launch {
            repository.addCustomZikr(
                titleArabic = titleArabic,
                titleEnglish = titleEnglish,
                categoryArabic = categoryArabic,
                arabicText = arabicText,
                translation = translation,
                targetCount = targetCount,
                benefits = benefits
            )
        }
    }

    fun incrementCustomZikr(id: Long) {
        viewModelScope.launch {
            repository.incrementCustomZikr(id)
        }
    }

    fun resetCustomZikr(id: Long) {
        viewModelScope.launch {
            repository.resetCustomZikr(id)
        }
    }

    fun deleteCustomZikr(zikr: CustomZikrEntity) {
        viewModelScope.launch {
            repository.deleteCustomZikr(zikr)
        }
    }

    // Khatma planner
    fun updateKhatmaPage(page: Int) {
        _uiState.value = _uiState.value.copy(khatmaCurrentPage = page.coerceIn(1, 604))
    }

    fun setKhatmaPlanDays(days: Int) {
        _uiState.value = _uiState.value.copy(khatmaPlanDays = days)
    }

    // Theme Mode Management
    fun setThemeMode(mode: AppThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun toggleTheme() {
        val nextMode = when (_uiState.value.themeMode) {
            AppThemeMode.LIGHT -> AppThemeMode.DARK
            AppThemeMode.DARK -> AppThemeMode.LIGHT
            AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
        }
        setThemeMode(nextMode)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
