package dev.antonlammers.macrotrac.data.repository

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.antonlammers.macrotrac.domain.repository.SettingsRepository
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
) : SettingsRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun isReminderEnabled(): Boolean =
        prefs.getBoolean(KEY_REMINDER_ENABLED, true)

    override suspend fun setReminderEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_REMINDER_ENABLED, enabled) }
    }

    private companion object {
        const val PREFS_NAME = "macrotrac_settings"
        const val KEY_REMINDER_ENABLED = "meal_reminder_enabled"
    }
}
