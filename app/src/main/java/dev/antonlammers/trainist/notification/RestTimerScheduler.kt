package dev.antonlammers.trainist.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/**
 * Schedules (and cancels) the **background fallback** for the rest-timer alert via [AlarmManager].
 *
 * This is deliberately an *inexact* alarm ([AlarmManager.setAndAllowWhileIdle]): the exact variants
 * require `SCHEDULE_EXACT_ALARM`/`USE_EXACT_ALARM`, and Google Play restricts those permissions to
 * apps whose *core functionality* is an alarm clock, timer, or calendar — a training app does not
 * qualify and would risk rejection. Instead, the precisely-timed alert is handled **in-app**: the
 * ticking countdown in `WorkoutSessionViewModel` emits `RestCommand.Expired` the moment it crosses
 * zero while the session screen is in the foreground (the dominant case — the user has the app open
 * mid-workout), which starts [RestTimerAlertService] on time and cancels this alarm. The inexact
 * alarm only covers the backgrounded/locked case, where it fires close to on-time when the device is
 * awake and may be deferred by Doze for a few minutes at worst — an acceptable trade-off for a
 * policy-clean manifest. When it fires, [RestTimerAlarmReceiver] posts the alert (or falls back to a
 * sounding notification if a foreground-service start is not allowed from the background).
 *
 * The delay itself is computed by the caller from the pure `RestTimer.remainingMs`, so there is no
 * Android-coupled timing math here.
 */
object RestTimerScheduler {

    private const val REQUEST_CODE = 2002

    fun schedule(context: Context, delayMs: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val triggerAtMs = System.currentTimeMillis() + delayMs.coerceAtLeast(0L)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent(context))
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, RestTimerAlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
