package dev.antonlammers.macrotrac

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.antonlammers.macrotrac.notification.MealReminderScheduler

@HiltAndroidApp
class MacroTracApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Keep the daily reminder aligned to the next 17:00. The worker itself respects the
        // enable/disable setting, so scheduling unconditionally here is safe.
        MealReminderScheduler.schedule(this)
    }
}
