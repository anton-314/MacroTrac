package dev.antonlammers.macrotrac.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dev.antonlammers.macrotrac.domain.repository.FoodEntryRepository
import dev.antonlammers.macrotrac.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Runs at 17:00. If the reminder is enabled and no food entry exists for today, posts the reminder.
 * Always re-schedules itself for the next day so the daily chain keeps going (also across reboots,
 * which WorkManager persists).
 *
 * Dependencies are pulled via a Hilt [EntryPoint] rather than `@HiltWorker`, which keeps the setup
 * free of a custom `WorkerFactory`/`Configuration.Provider`.
 */
class MealReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Deps {
        fun foodEntryRepository(): FoodEntryRepository
        fun settingsRepository(): SettingsRepository
    }

    override suspend fun doWork(): Result {
        val deps = EntryPointAccessors.fromApplication(applicationContext, Deps::class.java)
        try {
            if (deps.settingsRepository().isReminderEnabled()) {
                val entries = deps.foodEntryRepository().entriesForDate(LocalDate.now()).first()
                if (entries.isEmpty()) {
                    MealReminderNotifier.show(applicationContext)
                }
            }
        } finally {
            MealReminderScheduler.schedule(applicationContext)
        }
        return Result.success()
    }
}
