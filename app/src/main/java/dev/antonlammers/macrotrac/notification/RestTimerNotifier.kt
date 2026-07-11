package dev.antonlammers.macrotrac.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dev.antonlammers.macrotrac.MainActivity
import dev.antonlammers.macrotrac.R

/**
 * Builds and posts the rest-timer notifications, all under one notification id so each state
 * (running / paused / over) replaces the previous one:
 * - [showOngoing] / [showPaused]: a low-importance, silent, ongoing (`setOngoing`) notification that
 *   is visible for as long as a rest is running or paused — [showOngoing] uses a native chronometer
 *   (`setUsesChronometer` + `setChronometerCountDown`) so Android itself keeps the displayed
 *   remaining time live, without the app having to re-post every second.
 * - [showExpired]: the alerting "rest over" notification — unlike the ongoing ones, its channel is
 *   high-importance with sound + vibration so it reaches the user with the phone put down.
 *
 * Tapping any of them opens the app straight into the live workout session (spec addendum).
 */
object RestTimerNotifier {

    /** Set on the [MainActivity] launch intent so it navigates straight to the live session. */
    const val EXTRA_OPEN_WORKOUT_SESSION = "open_workout_session"

    private const val CHANNEL_ONGOING_ID = "rest_timer_ongoing"
    private const val CHANNEL_ALERT_ID = "rest_timer"
    private const val NOTIFICATION_ID = 2002

    /** Running countdown: ongoing, silent, live-counting via the notification's native chronometer. */
    fun showOngoing(context: Context, exerciseName: String, endAtMs: Long) {
        post(
            context,
            NotificationCompat.Builder(context, CHANNEL_ONGOING_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Satzpause läuft")
                .setContentText(exerciseName)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setShowWhen(true)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(endAtMs)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(openWorkoutPendingIntent(context))
                .build(),
        )
    }

    /** Paused countdown: ongoing, silent, static remaining time (no chronometer while frozen). */
    fun showPaused(context: Context, exerciseName: String, remainingSeconds: Int) {
        post(
            context,
            NotificationCompat.Builder(context, CHANNEL_ONGOING_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Satzpause pausiert")
                .setContentText("$exerciseName · ${formatMmSs(remainingSeconds)} verbleibend")
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(openWorkoutPendingIntent(context))
                .build(),
        )
    }

    /** Rest is over: replaces the ongoing notification with a dismissible, alerting one. */
    fun showExpired(context: Context) {
        post(
            context,
            NotificationCompat.Builder(context, CHANNEL_ALERT_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Pause vorbei")
                .setContentText("Zeit für den nächsten Satz.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                // Pre-O (channels ignore these): request sound + vibration explicitly.
                .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
                .setContentIntent(openWorkoutPendingIntent(context))
                .setAutoCancel(true)
                .build(),
        )
    }

    /** Dismisses whichever rest-timer notification is currently showing (skip/finish/discard). */
    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun post(context: Context, notification: android.app.Notification) {
        ensureChannels(context)
        // On Android 13+ posting silently no-ops without the runtime permission — guard to avoid noise.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
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
