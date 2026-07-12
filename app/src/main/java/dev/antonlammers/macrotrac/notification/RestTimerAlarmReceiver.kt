package dev.antonlammers.macrotrac.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Fires when the rest period is over and posts the alerting notification. Triggered by
 * [RestTimerScheduler] via [android.app.AlarmManager] (not WorkManager) so it wakes the device and
 * fires close to on-time even while idle/locked — WorkManager's delayed one-time work has no such
 * guarantee and can be deferred by Doze for minutes, which reads to the user as "no sound went off".
 */
class RestTimerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        RestTimerNotifier.showExpired(context)
    }
}
