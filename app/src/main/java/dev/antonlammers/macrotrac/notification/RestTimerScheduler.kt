package dev.antonlammers.macrotrac.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules (and cancels) the background rest-timer notification via WorkManager — the same
 * infrastructure as [MealReminderScheduler]. A single unique one-time work fires once, [delayMs]
 * from now, when the rest is over. Re-scheduling (resume / adjust) replaces the pending work;
 * pausing or skipping cancels it.
 *
 * The delay itself is computed by the caller from the pure `RestTimer.remainingMs`, so there is no
 * Android-coupled timing math here.
 */
object RestTimerScheduler {

    private const val WORK_NAME = "rest_timer"

    fun schedule(context: Context, delayMs: Long) {
        val request = OneTimeWorkRequestBuilder<RestTimerWorker>()
            .setInitialDelay(delayMs.coerceAtLeast(0L), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
