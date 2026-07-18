package dev.antonlammers.trainist

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.antonlammers.trainist.data.repository.SettingsRepositoryImpl
import dev.antonlammers.trainist.data.seed.ExerciseCatalogSeeder
import dev.antonlammers.trainist.notification.MealReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TrainistApp : Application() {

    @Inject lateinit var exerciseCatalogSeeder: ExerciseCatalogSeeder

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Re-apply the persisted per-app language before any Activity is created (API < 33 only;
        // 33+ persists it in the framework LocaleManager natively).
        SettingsRepositoryImpl.applyPersistedAppLanguage(this)
        // Keep the daily reminder aligned to the next 17:00. The worker itself respects the
        // enable/disable setting, so scheduling unconditionally here is safe.
        MealReminderScheduler.schedule(this)
        // Seed the bundled exercise catalog once per snapshot version (custom exercises preserved).
        appScope.launch { exerciseCatalogSeeder.seedIfNeeded() }
    }
}
