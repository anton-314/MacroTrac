package dev.antonlammers.macrotrac.domain.repository

import dev.antonlammers.macrotrac.domain.model.StatCardType

/**
 * Lightweight key/value app settings. Kept Android-free so ViewModels and the reminder worker
 * can depend on the interface and tests can substitute a fake.
 */
interface SettingsRepository {
    /** Whether the daily "you haven't tracked anything yet" reminder is enabled. Default: true. */
    suspend fun isReminderEnabled(): Boolean

    suspend fun setReminderEnabled(enabled: Boolean)

    /** User-chosen Stats-screen card order. Defaults to [StatCardType.DEFAULT_ORDER]. */
    suspend fun statsCardOrder(): List<StatCardType>

    suspend fun setStatsCardOrder(order: List<StatCardType>)
}
