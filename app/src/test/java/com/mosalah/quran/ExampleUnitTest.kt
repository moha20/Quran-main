package com.mosalah.quran

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.mosalah.quran.data.quran.QuranDataProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun azkar_hasComprehensiveCollection() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    QuranDataProvider.init(context)
    val azkar = QuranDataProvider.hisnDuas
    assertTrue("Azkar list should have at least 20 items", azkar.size >= 20)

    val morningAzkar = azkar.filter { it.categoryArabic.contains("الصباح") }
    assertTrue("Should contain morning azkar", morningAzkar.isNotEmpty())

    val eveningAzkar = azkar.filter { it.categoryArabic.contains("المساء") }
    assertTrue("Should contain evening azkar", eveningAzkar.isNotEmpty())

    val prayerAzkar = azkar.filter { it.categoryArabic.contains("الصلاة") }
    assertTrue("Should contain after-prayer azkar", prayerAzkar.isNotEmpty())

    // Ensure all items have non-empty text, title, and valid targetCount
    azkar.forEach { dua ->
      assertTrue(dua.titleArabic.isNotBlank())
      assertTrue(dua.arabicText.isNotBlank())
      assertTrue(dua.targetCount >= 1)
    }
  }

  @Test
  fun themeMode_hasAllOptions() {
    val modes = com.mosalah.quran.ui.theme.AppThemeMode.values()
    assertTrue(modes.contains(com.mosalah.quran.ui.theme.AppThemeMode.LIGHT))
    assertTrue(modes.contains(com.mosalah.quran.ui.theme.AppThemeMode.DARK))
    assertTrue(modes.contains(com.mosalah.quran.ui.theme.AppThemeMode.SYSTEM))
    assertEquals("الوضع الفاتح ☀️", com.mosalah.quran.ui.theme.AppThemeMode.LIGHT.titleArabic)
  }

  @Test
  fun defaultTranslation_isArabicOnly() {
    val defaultState = com.mosalah.quran.ui.viewmodel.QuranUiState()
    assertEquals("ar_only", defaultState.selectedTranslation)
  }

  @Test
  fun reciters_hasComprehensiveCollection() {
    val qaris = QuranDataProvider.qaris
    assertTrue("Reciters list should have at least 40 items", qaris.size >= 40)

    val ids = qaris.map { it.id }
    assertEquals("All reciter IDs should be unique", ids.size, ids.distinct().size)

    // Ensure all 5 original reciter IDs are preserved
    assertTrue(ids.contains("alafasy"))
    assertTrue(ids.contains("abdulbasit"))
    assertTrue(ids.contains("sudais"))
    assertTrue(ids.contains("husary"))
    assertTrue(ids.contains("ghamadi"))

    // Ensure all items have valid names, styles, countries, and audioSubfolder
    qaris.forEach { qari ->
      assertTrue(qari.id.isNotBlank())
      assertTrue(qari.nameArabic.isNotBlank())
      assertTrue(qari.nameEnglish.isNotBlank())
      assertTrue(qari.style.isNotBlank())
      assertTrue(qari.country.isNotBlank())
      assertTrue(qari.audioSubfolder.isNotBlank())

      val audioUrl = QuranDataProvider.getAudioUrl(qari, 1, 1)
      assertTrue("URL should start with https://everyayah.com/data/", audioUrl.startsWith("https://everyayah.com/data/"))
      assertTrue("URL should end with 001001.mp3", audioUrl.endsWith("001001.mp3"))
    }
  }

  @Test
  fun audioPlayer_singletonAndService_configuredCorrectly() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val player1 = com.mosalah.quran.service.audio.QuranAudioPlayer.getInstance(context)
    val player2 = com.mosalah.quran.service.audio.QuranAudioPlayer.getInstance(context)
    assertSame("QuranAudioPlayer should be a singleton", player1, player2)
    assertEquals(com.mosalah.quran.service.audio.AudioPlaybackStatus.IDLE, player1.uiState.value.status)
    assertEquals(1, player1.uiState.value.surahNumber)
    assertEquals(1, player1.uiState.value.ayahNumber)
    assertEquals("quran_audio_playback_v2", com.mosalah.quran.service.audio.QuranAudioService.CHANNEL_ID)
    assertEquals(2001, com.mosalah.quran.service.audio.QuranAudioService.NOTIFICATION_ID)
  }
}
