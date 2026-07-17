package dev.antonlammers.trainist.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Background fallback for "rest is over": fires when [RestTimerScheduler]'s inexact alarm goes off,
 * i.e. only when the in-app countdown did not already handle the expiry in the foreground (that path
 * cancels this alarm — see `RestCommand.Expired`).
 *
 * Tries to start [RestTimerAlertService] for the full alert (sound + vibration). Since Android 12 a
 * foreground service may not be started from the background, and an *inexact* alarm is not one of
 * the documented exemptions — in that case the receiver falls back to posting the alert notification
 * directly, whose channel carries its own notification sound + vibration
 * ([RestTimerNotifier.postExpiredFallback]). That fallback respects ringer/Do Not Disturb like any
 * normal notification, which is acceptable for the backgrounded case.
 */
class RestTimerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!RestTimerAlertService.tryStart(context)) {
            RestTimerNotifier.postExpiredFallback(context)
        }
    }
}
