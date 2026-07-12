package dev.antonlammers.macrotrac.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Fires when the rest period is over and posts the alerting notification. One-shot (unlike the
 * self-rescheduling [MealReminderWorker]) — a fresh timer enqueues a new unique work each time.
 */
class RestTimerWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        RestTimerNotifier.showExpired(applicationContext)
        return Result.success()
    }
}
