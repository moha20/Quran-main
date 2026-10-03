package com.mosalah.quran

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.mosalah.quran.data.quran.QuranDataProvider
import com.mosalah.quran.ui.screens.DhikrDuasScreen
import com.mosalah.quran.ui.screens.MemorizationScreen
import com.mosalah.quran.ui.screens.MushafScreen
import com.mosalah.quran.ui.screens.PrayerQiblaScreen
import com.mosalah.quran.ui.screens.ReciterAudioScreen
import com.mosalah.quran.ui.screens.SurahReaderScreen
import com.mosalah.quran.ui.theme.MyApplicationTheme
import com.mosalah.quran.ui.viewmodel.QuranViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class StoreScreenshotsTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        QuranDataProvider.init(context)
    }

    @Test
    fun capture_screenshot_1_mushaf() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = QuranViewModel(app)
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                MushafScreen(viewModel = viewModel, onOpenSurah = { _, _ -> })
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/screenshot_1_mushaf.png")
    }

    @Test
    fun capture_screenshot_2_reader() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = QuranViewModel(app)
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                SurahReaderScreen(surahNumber = 18, initialAyahNumber = 1, viewModel = viewModel, onBack = {})
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/screenshot_2_recitation.png")
    }

    @Test
    fun capture_screenshot_3_azkar() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = QuranViewModel(app)
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                DhikrDuasScreen(viewModel = viewModel)
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/screenshot_3_azkar.png")
    }

    @Test
    fun capture_screenshot_4_prayer() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = QuranViewModel(app)
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                PrayerQiblaScreen(viewModel = viewModel)
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/screenshot_4_prayer.png")
    }

    @Test
    fun capture_screenshot_5_memorization() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = QuranViewModel(app)
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                MemorizationScreen(viewModel = viewModel)
            }
        }
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/screenshot_5_memorization.png")
    }
}
