package com.mosalah.quran.service.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import android.util.Log
import com.mosalah.quran.data.model.Qari
import com.mosalah.quran.data.quran.QuranDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

    companion object {
        private const val TAG = "QuranAudioPlayer"

        @Volatile
        private var INSTANCE: QuranAudioPlayer? = null

        fun getInstance(context: Context): QuranAudioPlayer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: QuranAudioPlayer(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var playJob: Job? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                hasAudioFocus = false
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                try {
                    mediaPlayer?.setVolume(0.2f, 0.2f)
                } catch (_: Exception) {}
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                hasAudioFocus = true
                try {
                    mediaPlayer?.setVolume(1.0f, 1.0f)
                } catch (_: Exception) {}
                if (_uiState.value.status == AudioPlaybackStatus.PAUSED) {
                    resume()
                }
            }
        }
    }

    private val _uiState = MutableStateFlow(AudioPlayerUiState())
    val uiState: StateFlow<AudioPlayerUiState> = _uiState.asStateFlow()

    private fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) return true
        val am = audioManager ?: return true
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()

        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest == null) {
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build()
            }
            am.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
        hasAudioFocus = granted
        return granted
    }

    private fun abandonAudioFocus() {
        if (!hasAudioFocus) return
        val am = audioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(audioFocusChangeListener)
            }
        } catch (_: Exception) {}
        hasAudioFocus = false
    }

    private fun getOrCreatePlayer(): MediaPlayer {
        mediaPlayer?.let { return it }
        val player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            try {
                setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
            } catch (_: Exception) {}

            setOnPreparedListener { mp ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        val params = mp.playbackParams
                        params.speed = _uiState.value.playbackSpeed
                        mp.playbackParams = params
                    } catch (_: Exception) {}
                }
                requestAudioFocus()
                try {
                    mp.setVolume(1.0f, 1.0f)
                } catch (_: Exception) {}
                mp.start()
                _uiState.value = _uiState.value.copy(status = AudioPlaybackStatus.PLAYING)
                Log.d(TAG, "Audio started playing successfully")
            }
            setOnCompletionListener {
                handleAyahCompletion()
            }
            setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                _uiState.value = _uiState.value.copy(
                    status = AudioPlaybackStatus.ERROR,
                    errorMessage = "تعذر تشغيل التسجيل الصوتي ($what, $extra)"
                )
                abandonAudioFocus()
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
        QuranAudioService.start(context)

        val url = QuranDataProvider.getAudioUrl(qari, surahNumber, ayahNumber)
        Log.d(TAG, "Preparing audio URL: $url")
        playJob?.cancel()
        playJob = scope.launch(Dispatchers.IO) {
            try {
                synchronized(player) {
                    player.reset()
                    player.setDataSource(url)
                    player.prepareAsync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load audio from $url: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    status = AudioPlaybackStatus.ERROR,
                    errorMessage = "خطأ في الاتصال بالخادم الصوتي"
                )
            }
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
        } catch (_: Exception) {}
        _uiState.value = _uiState.value.copy(status = AudioPlaybackStatus.PAUSED)
    }

    fun resume() {
        requestAudioFocus()
        try {
            mediaPlayer?.start()
        } catch (_: Exception) {}
        _uiState.value = _uiState.value.copy(status = AudioPlaybackStatus.PLAYING)
    }

    fun togglePlayPause() {
        val currentState = _uiState.value.status
        if (currentState == AudioPlaybackStatus.PLAYING) {
            pause()
        } else if (currentState == AudioPlaybackStatus.PAUSED && mediaPlayer != null) {
            QuranAudioService.start(context)
            resume()
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
            stop()
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
        playJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        } catch (_: Exception) {}
        abandonAudioFocus()
        _uiState.value = _uiState.value.copy(status = AudioPlaybackStatus.IDLE)
        QuranAudioService.stop(context)
    }

    fun release() {
        playJob?.cancel()
        abandonAudioFocus()
        try {
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}
        QuranAudioService.stop(context)
    }
}
