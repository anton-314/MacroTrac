package dev.antonlammers.macrotrac.fake

import dev.antonlammers.macrotrac.domain.model.StatCardType
import dev.antonlammers.macrotrac.domain.repository.SettingsRepository

class FakeSettingsRepository(
    private var reminderEnabled: Boolean = true,
    private var statsCardOrder: List<StatCardType> = StatCardType.DEFAULT_ORDER,
) : SettingsRepository {

    override suspend fun isReminderEnabled(): Boolean = reminderEnabled

    override suspend fun setReminderEnabled(enabled: Boolean) {
        reminderEnabled = enabled
    }

    override suspend fun statsCardOrder(): List<StatCardType> = statsCardOrder

    override suspend fun setStatsCardOrder(order: List<StatCardType>) {
        statsCardOrder = order
    }
}
