package dev.antonlammers.trainist.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.RingtoneManager
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dev.antonlammers.trainist.MainActivity
import dev.antonlammers.trainist.R

/**
 * Builds and posts the rest-timer notifications, all under one notification id so each state
 * (running / paused / over) replaces the previous one:
 * - [showOngoing] / [showPaused]: a low-importance, silent, ongoing (`setOngoing`) notification that
 *   is visible for as long as a rest is running or paused. Both use a **custom content view**
 *   (`notification_rest_timer.xml`) that shows the remaining time LARGE — [showOngoing] via an
 *   embedded [android.widget.Chronometer] counting down to the end instant (which Android keeps live
 *   itself, even after the app process is gone, so no per-second re-posting is needed), [showPaused]
 *   via the frozen mm:ss text.
 * - [showExpired]: the alerting "rest over" notification — unlike the ongoing ones, its channel is
 *   high-importance with sound + vibration so it reaches the user with the phone put down.
 *
 * Tapping any of them opens the app straight into the live workout session (spec addendum).
 */
object RestTimerNotifier {

    /** Set on the [MainActivity] launch intent so it navigates straight to the live session. */
    const val EXTRA_OPEN_WORKOUT_SESSION = "open_workout_session"

    private const val CHANNEL_ONGOING_ID = "rest_timer_ongoing"
    // Bumped from the legacy "rest_timer" id: a notification channel's importance/sound/vibration are
    // immutable once created, so users who ran an earlier build where this channel ended up silent
    // would never get sound again. A fresh id guarantees the HIGH + sound + vibration settings apply.
    private const val CHANNEL_ALERT_ID = "rest_timer_alert"
    // The ongoing/paused countdown and the "rest over" alert use SEPARATE notification ids on purpose:
    // Android will not move an already-posted notification to a different channel on update, so posting
    // the HIGH-channel alert under the ongoing notification's id would leave it stuck on the silent LOW
    // channel — no sound, no vibration. A distinct id makes the alert a fresh notification that actually
    // alerts. showExpired cancels the ongoing one so only the alert remains.
    private const val NOTIFICATION_ID_ONGOING = 2002
    private const val NOTIFICATION_ID_ALERT = 2003

    // Notification-shade text colours (the custom RemoteViews text can't rely on ?android:textColorPrimary
    // resolving to the shade's theme across OEMs): near-black in light mode, near-white in dark mode.
    private const val TEXT_COLOR_LIGHT = 0xFF1A1A1A.toInt()
    private const val TEXT_COLOR_DARK = 0xFFECECEC.toInt()

    /** Running countdown: ongoing, silent, big live chronometer counting down to [endAtMs]. */
    fun showOngoing(context: Context, exerciseName: String, endAtMs: Long) {
        // Chronometer.setBase expects the SystemClock.elapsedRealtime() timebase; endAtMs is wall-clock.
        val chronometerBase = SystemClock.elapsedRealtime() + (endAtMs - System.currentTimeMillis())
        val textColor = notificationTextColor(context)
        val content = RemoteViews(context.packageName, R.layout.notification_rest_timer).apply {
            setTextViewText(R.id.rest_title, "Satzpause · $exerciseName")
            setViewVisibility(R.id.rest_chronometer, View.VISIBLE)
            setViewVisibility(R.id.rest_time_static, View.GONE)
            setChronometer(R.id.rest_chronometer, chronometerBase, null, true)
            setChronometerCountDown(R.id.rest_chronometer, true)
            setTextColor(R.id.rest_title, textColor)
            setTextColor(R.id.rest_chronometer, textColor)
        }
        post(context, NOTIFICATION_ID_ONGOING, ongoingBuilder(context, content).build())
    }

    /** Paused countdown: ongoing, silent, big static remaining time (no chronometer while frozen). */
    fun showPaused(context: Context, exerciseName: String, remainingSeconds: Int) {
        val textColor = notificationTextColor(context)
        val content = RemoteViews(context.packageName, R.layout.notification_rest_timer).apply {
            setTextViewText(R.id.rest_title, "Pausiert · $exerciseName")
            setViewVisibility(R.id.rest_chronometer, View.GONE)
            setViewVisibility(R.id.rest_time_static, View.VISIBLE)
            setTextViewText(R.id.rest_time_static, formatMmSs(remainingSeconds))
            setTextColor(R.id.rest_title, textColor)
            setTextColor(R.id.rest_time_static, textColor)
        }
        post(context, NOTIFICATION_ID_ONGOING, ongoingBuilder(context, content).build())
    }

    /** Near-black on the light shade, near-white on the dark shade (readable in both). */
    private fun notificationTextColor(context: Context): Int {
        val night = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return if (night == Configuration.UI_MODE_NIGHT_YES) TEXT_COLOR_DARK else TEXT_COLOR_LIGHT
    }

    private fun ongoingBuilder(context: Context, content: RemoteViews) =
        NotificationCompat.Builder(context, CHANNEL_ONGOING_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(content)
            .setCustomBigContentView(content)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openWorkoutPendingIntent(context))

    /** Rest is over: dismisses the ongoing countdown and posts a fresh, alerting notification. */
    fun showExpired(context: Context) {
        // Cancel the silent ongoing notification first; the alert is a NEW notification (own id) so it
        // lands on the HIGH channel and actually rings/vibrates instead of inheriting the LOW channel.
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_ONGOING)
        post(
            context,
            NOTIFICATION_ID_ALERT,
            NotificationCompat.Builder(context, CHANNEL_ALERT_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Pause vorbei")
                .setContentText("Zeit für den nächsten Satz.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                // Explicit sound + vibration too: covers pre-O (channels ignore these) and any device
                // where the channel's own sound/vibration was toggled off — the alert still fires.
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(longArrayOf(0, 400, 200, 400))
                .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
                .setContentIntent(openWorkoutPendingIntent(context))
                .setAutoCancel(true)
                .build(),
        )
    }

    /** Dismisses whichever rest-timer notification is currently showing (skip/finish/discard). */
    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).apply {
            cancel(NOTIFICATION_ID_ONGOING)
            cancel(NOTIFICATION_ID_ALERT)
        }
    }

    private fun post(context: Context, id: Int, notification: android.app.Notification) {
        ensureChannels(context)
        // On Android 13+ posting silently no-ops without the runtime permission — guard to avoid noise.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private fun openWorkoutPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_OPEN_WORKOUT_SESSION, true)
        }
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun formatMmSs(totalSeconds: Int): String {
        val safe = totalSeconds.coerceAtLeast(0)
        return "%d:%02d".format(safe / 60, safe % 60)
    }

    private fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ONGOING_ID,
                    "Ruhe-Timer (Status)",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Zeigt die laufende Satzpause an, solange sie läuft."
                    enableVibration(false)
                    setSound(null, null)
                },
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ALERT_ID,
                    "Ruhe-Timer",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Meldet, wenn die Satzpause abgelaufen ist."
                    enableVibration(true)
                    setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                        null,
                    )
                },
            )
        }
    }
}
