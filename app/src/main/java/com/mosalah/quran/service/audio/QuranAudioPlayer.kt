package com.mosalah.quran.service.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import com.mosalah.quran.data.model.Qari
import com.mosalah.quran.data.quran.QuranDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AudioPlaybackStatus {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR
}

data class AudioPlayerUiState(
    val status: AudioPlaybackStatus = AudioPlaybackStatus.IDLE,
    val surahNumber: Int = 1,
    val ayahNumber: Int = 1,
    val qari: Qari = QuranDataProvider.qaris.first(),
    val playbackSpeed: Float = 1.0f,
    val repeatCount: Int = 1,
    val currentRepeatIteration: Int = 1,
    val errorMessage: String? = null
)

class QuranAudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(AudioPlayerUiState())
    val uiState: StateFlow<AudioPlayerUiState> = _uiState.asStateFlow()

    private fun getOrCreatePlayer(): MediaPlayer {
        mediaPlayer?.let { return it }
        val player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnPreparedListener { mp ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        val params = mp.playbackParams
                        params.speed = _uiState.value.playbackSpeed
                        mp.playbackParams = params
                    } catch (_: Exception) {}
                }
                mp.start()
                _uiState.value = _uiState.value.copy(status = AudioPlaybackStatus.PLAYING)
            }
            setOnCompletionListener {
                handleAyahCompletion()
            }
            setOnErrorListener { _, what, extra ->
                _uiState.value = _uiState.value.copy(
                    status = AudioPlaybackStatus.ERROR,
                    errorMessage = "تعذر تشغيل التسجيل الصوتي ($what, $extra)"
                )
                true
            }
        }
        mediaPlayer = player
        return player
    }

    fun playAyah(surahNumber: Int, ayahNumber: Int, qari: Qari = _uiState.value.qari) {
        val player = getOrCreatePlayer()
        _uiState.value = _uiState.value.copy(
            status = AudioPlaybackStatus.BUFFERING,
            surahNumber = surahNumber,
            ayahNumber = ayahNumber,
            qari = qari,
            errorMessage = null,
            currentRepeatIteration = 1
        )

        val url = QuranDataProvider.getAudioUrl(qari, surahNumber, ayahNumber)
        scope.launch(Dispatchers.IO) {
            try {
                player.reset()
                player.setDataSource(url)
                player.prepareAsync()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    status = AudioPlaybackStatus.ERROR,
                    errorMessage = "خطأ في الاتصال بالخادم الصوتي"
                )
            }
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        val currentState = _uiState.value.status
        if (currentState == AudioPlaybackStatus.PLAYING) {
            player.pause()
            _uiState.value = _uiState.value.copy(status = AudioPlaybackStatus.PAUSED)
        } else if (currentState == AudioPlaybackStatus.PAUSED) {
            player.start()
            _uiState.value = _uiState.value.copy(status = AudioPlaybackStatus.PLAYING)
        } else {
            playAyah(_uiState.value.surahNumber, _uiState.value.ayahNumber, _uiState.value.qari)
        }
    }

    fun setSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
        mediaPlayer?.let { mp ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mp.isPlaying) {
                try {
                    val params = mp.playbackParams
                    params.speed = speed
                    mp.playbackParams = params
                } catch (_: Exception) {}
            }
        }
    }

    fun setRepeatCount(count: Int) {
        _uiState.value = _uiState.value.copy(repeatCount = count, currentRepeatIteration = 1)
    }

    fun setQari(qari: Qari) {
        val wasPlaying = _uiState.value.status == AudioPlaybackStatus.PLAYING
        _uiState.value = _uiState.value.copy(qari = qari)
        if (wasPlaying) {
            playAyah(_uiState.value.surahNumber, _uiState.value.ayahNumber, qari)
        }
    }

    private fun handleAyahCompletion() {
        val state = _uiState.value
        if (state.currentRepeatIteration < state.repeatCount) {
            // Repeat current Ayah
            _uiState.value = state.copy(currentRepeatIteration = state.currentRepeatIteration + 1)
            playAyah(state.surahNumber, state.ayahNumber, state.qari)
        } else {
            // Play next Ayah
            playNextAyah()
        }
    }

    fun playNextAyah() {
        val state = _uiState.value
        val surah = QuranDataProvider.surahs.find { it.number == state.surahNumber }
        val maxAyahs = surah?.versesCount ?: 7

        if (state.ayahNumber < maxAyahs) {
            playAyah(state.surahNumber, state.ayahNumber + 1, state.qari)
        } else if (state.surahNumber < 114) {
            playAyah(state.surahNumber + 1, 1, state.qari)
        } else {
            _uiState.value = state.copy(status = AudioPlaybackStatus.IDLE)
        }
    }

    fun playPreviousAyah() {
        val state = _uiState.value
        if (state.ayahNumber > 1) {
            playAyah(state.surahNumber, state.ayahNumber - 1, state.qari)
        } else if (state.surahNumber > 1) {
            val prevSurah = QuranDataProvider.surahs.find { it.number == state.surahNumber - 1 }
            val lastAyah = prevSurah?.versesCount ?: 1
            playAyah(state.surahNumber - 1, lastAyah, state.qari)
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        } catch (_: Exception) {}
        _uiState.value = _uiState.value.copy(status = AudioPlaybackStatus.IDLE)
    }

    fun release() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}
    }
}
