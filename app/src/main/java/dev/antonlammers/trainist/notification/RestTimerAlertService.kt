package dev.antonlammers.trainist.notification

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat

/**
 * Plays the rest-over alert as **media-stream** audio + direct vibration.
 * [AudioAttributes.USAGE_MEDIA] plays on the media stream — the deliberate choice here: the alert
 * behaves like any other app audio (media volume, audible over headphones), rather than hijacking
 * the alarm stream, which fits an app that holds no alarm permissions. The vibration is triggered
 * directly via [Vibrator.vibrate] rather than through a notification channel, and the accompanying
 * heads-up notification is posted silent ([RestTimerNotifier.buildExpiredNotification]) so channel
 * sound and player never double up.
 *
 * The alert is a short **notification-style chime** — [RingtoneManager.TYPE_NOTIFICATION], not
 * [RingtoneManager.TYPE_ALARM] — since the latter is the user's own configured alarm-clock tone,
 * which is typically long/melody-length and reads as "my alarm went off", not a quick app cue. Total
 * runtime (sound + vibration) is hard-capped at [ALERT_DURATION_MS] regardless of the resolved tone's
 * own length, so the alert always ends quickly even without user interaction. It can also be silenced
 * immediately: tapping the notification's "Stoppen" action (or swiping it away, or opening the app,
 * see [RestTimerNotifier.buildExpiredNotification] / `MainActivity`) sends [ACTION_STOP].
 *
 * Runs as a short-lived foreground service so playback reliably finishes even with the screen
 * off/locked. Started via [tryStart] from the session screen when the in-app countdown crosses zero
 * (app in the foreground — always allowed), or from [RestTimerAlarmReceiver]'s background-fallback
 * alarm, where the start can be rejected on Android 12+ (no exact alarm = no foreground-service
 * start exemption) — [tryStart] reports that so the receiver can fall back to a sounding
 * notification.
 */
class RestTimerAlertService : Service() {

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var stopped = false
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAlert()
            return START_NOT_STICKY
        }

        ServiceCompat.startForeground(
            this,
            RestTimerNotifier.NOTIFICATION_ID_ALERT,
            RestTimerNotifier.buildExpiredNotification(this),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
        )

        wakeLock = getSystemService(PowerManager::class.java)
            ?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Trainist:RestTimerAlert")
            ?.apply { acquire(ALERT_DURATION_MS + 1_000L) }

        // Hard cap: the alert must always be short, regardless of how long the resolved tone actually is.
        mainHandler.postDelayed({ stopAlert() }, ALERT_DURATION_MS)

        vibrate()
        playSound()
        return START_NOT_STICKY
    }

    private fun vibrate() {
        val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as? Vibrator
        }
        vibrator = v
        v?.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, 0))
    }

    private fun playSound() {
        val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
        if (uri == null) return
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            setOnCompletionListener { stopAlert() }
            setOnErrorListener { _, _, _ -> stopAlert(); true }
            try {
                setDataSource(this@RestTimerAlertService, uri)
                prepare()
                start()
            } catch (e: Exception) {
                stopAlert()
            }
        }
    }

    private fun stopAlert() {
        if (stopped) return
        stopped = true
        mainHandler.removeCallbacksAndMessages(null)
        vibrator?.cancel()
        vibrator = null
        player?.let { runCatching { it.stop() }; runCatching { it.release() } }
        player = null
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    override fun onDestroy() {
        stopAlert()
        super.onDestroy()
    }

    companion object {
        /** Sent as the [Intent.getAction] to silence an in-progress alert immediately. */
        const val ACTION_STOP = "dev.antonlammers.trainist.action.STOP_REST_ALERT"

        /**
         * Starts the alert as a foreground service; returns false when Android rejects the start
         * because the app is in the background (Android 12+ restriction — possible on the inexact
         * background-alarm path, never on the in-app expiry path). Callers use the result to decide
         * whether a fallback notification is needed instead.
         */
        fun tryStart(context: Context): Boolean = try {
            ContextCompat.startForegroundService(context, Intent(context, RestTimerAlertService::class.java))
            true
        } catch (e: IllegalStateException) {
            // ForegroundServiceStartNotAllowedException (API 31+) extends IllegalStateException;
            // catching the base type keeps this safe to verify on every API level.
            false
        }

        // The alert is meant to be a short cue, not a ringing alarm — always stop after this long,
        // regardless of the resolved tone's actual length.
        private const val ALERT_DURATION_MS = 6_000L
        private val VIBRATION_PATTERN = longArrayOf(0, 400, 200, 400)
    }
}
