package com.example

import com.example.data.quran.QuranDataProvider
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun azkar_hasComprehensiveCollection() {
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
    val modes = com.example.ui.theme.AppThemeMode.values()
    assertTrue(modes.contains(com.example.ui.theme.AppThemeMode.LIGHT))
    assertTrue(modes.contains(com.example.ui.theme.AppThemeMode.DARK))
    assertTrue(modes.contains(com.example.ui.theme.AppThemeMode.SYSTEM))
    assertEquals("الوضع الفاتح ☀️", com.example.ui.theme.AppThemeMode.LIGHT.titleArabic)
  }

  @Test
  fun defaultTranslation_isArabicOnly() {
    val defaultState = com.example.ui.viewmodel.QuranUiState()
    assertEquals("ar_only", defaultState.selectedTranslation)
  }
}
