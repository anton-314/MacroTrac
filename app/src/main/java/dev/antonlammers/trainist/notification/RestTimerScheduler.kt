package dev.antonlammers.trainist.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Schedules (and cancels) the background rest-timer alert via [AlarmManager]. Unlike
 * [MealReminderScheduler]'s WorkManager job (fine for a once-a-day, imprecise reminder), a rest timer
 * has to fire *on time to the second* even while the device is idle/locked — a rest that ends a minute
 * late reads to the user as "no beep went off" (and the ongoing countdown notification overruns into
 * negative time). WorkManager's delayed work and even [AlarmManager.setAndAllowWhileIdle] (inexact)
 * can be deferred by Doze for minutes, so this uses the **exact** variant
 * [AlarmManager.setExactAndAllowWhileIdle], which Doze fires precisely. Exact alarms need a permission
 * on API 31+; a rest timer legitimately qualifies for the auto-granted `USE_EXACT_ALARM`
 * (declared in the manifest), so no runtime request is needed. If exact scheduling is somehow
 * unavailable we fall back to the inexact variant rather than dropping the alert.
 *
 * The delay itself is computed by the caller from the pure `RestTimer.remainingMs`, so there is no
 * Android-coupled timing math here.
 */
object RestTimerScheduler {

    private const val REQUEST_CODE = 2002

    fun schedule(context: Context, delayMs: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val triggerAtMs = System.currentTimeMillis() + delayMs.coerceAtLeast(0L)
        val pendingIntent = pendingIntent(context)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
        }
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
