package dev.antonlammers.macrotrac.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Schedules the meal reminder as a self-rescheduling one-time work that fires at [REMINDER_HOUR]:00.
 *
 * A one-time work (rather than periodic) lets us hit the exact hour each day: [MealReminderWorker]
 * re-enqueues the next run when it finishes, and [schedule] is also called on app start so the work
 * stays aligned to the next 17:00 even if the chain was never started.
 */
object MealReminderScheduler {

    const val REMINDER_HOUR = 17
    private const val WORK_NAME = "meal_reminder"

    fun schedule(context: Context) {
        val request = OneTimeWorkRequestBuilder<MealReminderWorker>()
            .setInitialDelay(initialDelayMillis(LocalDateTime.now()), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    /**
     * Milliseconds from [now] until the next [REMINDER_HOUR]:00 — later today if that time is still
     * ahead, otherwise the same time tomorrow. Pure so it can be unit-tested.
     */
    fun initialDelayMillis(now: LocalDateTime): Long {
        var next = now.toLocalDate().atTime(REMINDER_HOUR, 0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next).toMillis()
    }
}
