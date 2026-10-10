package com.mosalah.quran.service.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.session.MediaSession
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mosalah.quran.MainActivity
import com.mosalah.quran.data.quran.QuranDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class QuranAudioService : Service() {

    companion object {
        const val CHANNEL_ID = "quran_audio_playback_v2"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START = "com.mosalah.quran.action.START"
        const val ACTION_PLAY_PAUSE = "com.mosalah.quran.action.PLAY_PAUSE"
        const val ACTION_NEXT = "com.mosalah.quran.action.NEXT"
        const val ACTION_PREV = "com.mosalah.quran.action.PREV"
        const val ACTION_STOP = "com.mosalah.quran.action.STOP"

        fun start(context: Context) {
            val intent = Intent(context, QuranAudioService::class.java).apply {
                action = ACTION_START
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {}
        }

        fun stop(context: Context) {
            val intent = Intent(context, QuranAudioService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var observeJob: Job? = null
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        try {
            mediaSession = MediaSession(this, "QuranAudioPlaybackSession").apply {
                setCallback(object : MediaSession.Callback() {
                    override fun onPlay() { QuranAudioPlayer.getInstance(this@QuranAudioService).togglePlayPause() }
                    override fun onPause() { QuranAudioPlayer.getInstance(this@QuranAudioService).togglePlayPause() }
                    override fun onSkipToNext() { QuranAudioPlayer.getInstance(this@QuranAudioService).playNextAyah() }
                    override fun onSkipToPrevious() { QuranAudioPlayer.getInstance(this@QuranAudioService).playPreviousAyah() }
                    override fun onStop() { QuranAudioPlayer.getInstance(this@QuranAudioService).stop() }
                })
                isActive = true
            }
        } catch (_: Exception) {}
        observeAudioState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val player = QuranAudioPlayer.getInstance(this)
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> player.togglePlayPause()
            ACTION_NEXT -> player.playNextAyah()
            ACTION_PREV -> player.playPreviousAyah()
            ACTION_STOP -> {
                player.stop()
                stopForegroundSafely()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                updateNotification(player.uiState.value)
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun observeAudioState() {
        val player = QuranAudioPlayer.getInstance(this)
        observeJob?.cancel()
        observeJob = serviceScope.launch {
            player.uiState.collect { state ->
                when (state.status) {
                    AudioPlaybackStatus.PLAYING,
                    AudioPlaybackStatus.BUFFERING,
                    AudioPlaybackStatus.PAUSED -> {
                        updateNotification(state)
                    }
                    AudioPlaybackStatus.IDLE,
                    AudioPlaybackStatus.ERROR -> {
                        stopForegroundSafely()
                        stopSelf()
                    }
                }
            }
        }
    }

    private fun updateNotification(state: AudioPlayerUiState) {
        val notification = buildNotification(state)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (_: Exception) {}
    }

    private fun stopForegroundSafely() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (_: Exception) {}
    }

    private fun buildNotification(state: AudioPlayerUiState): Notification {
        val isPlaying = state.status == AudioPlaybackStatus.PLAYING || state.status == AudioPlaybackStatus.BUFFERING
        val surah = QuranDataProvider.surahs.find { it.number == state.surahNumber }
        val surahName = surah?.nameArabic ?: "سورة ${state.surahNumber}"

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevPendingIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, QuranAudioService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPausePendingIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, QuranAudioService::class.java).apply { action = ACTION_PLAY_PAUSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextPendingIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, QuranAudioService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopPendingIntent = PendingIntent.getService(
            this,
            4,
            Intent(this, QuranAudioService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val playPauseTitle = if (isPlaying) "إيقاف مؤقت" else "تشغيل"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(state.qari.nameArabic)
            .setContentText("سورة $surahName • الآية ${state.ayahNumber}")
            .setSubText(state.qari.style)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(android.R.drawable.ic_media_previous, "السابق", prevPendingIntent)
            .addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "التالي", nextPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إغلاق", stopPendingIntent)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تلاوة القرآن الكريم",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إشعار تشغيل وتلاوة آيات القرآن الكريم في الخلفية"
                setShowBadge(false)
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        observeJob?.cancel()
        try {
            mediaSession?.isActive = false
            mediaSession?.release()
            mediaSession = null
        } catch (_: Exception) {}
    }
}
