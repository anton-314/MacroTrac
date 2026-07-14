package dev.antonlammers.trainist.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/**
 * Schedules (and cancels) the background rest-timer alert via [AlarmManager.setAndAllowWhileIdle] —
 * unlike [MealReminderScheduler]'s WorkManager job (fine for a once-a-day, imprecise reminder),
 * a rest timer needs to fire close to on-time even while the device is idle/locked, which WorkManager
 * does not guarantee (its delayed one-time work can be deferred by Doze for minutes). No special
 * permission is needed — that's only required for the *exact* alarm variants, and a rest timer
 * doesn't need sub-second precision, just to reliably fire near the requested moment while idle.
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
