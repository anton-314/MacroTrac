package dev.antonlammers.macrotrac.domain.model

/**
 * One reorderable card on the Stats screen. Persisted (as an ordered list of [name]s) via
 * [dev.antonlammers.macrotrac.domain.repository.SettingsRepository] so the user's chosen card
 * order survives app restarts; [parse] reads it back defensively so unknown/removed values are
 * dropped rather than crashing.
 */
enum class StatCardType {
    CALORIES, CLEAN_EATING, WEIGHT, TRAINING_FREQUENCY, STRENGTH;

    companion object {
        /** Shown the first time the app runs, before any custom order has been saved. */
        val DEFAULT_ORDER: List<StatCardType> = entries.toList()

        fun parse(raw: String?): StatCardType? =
            raw?.trim()?.uppercase()?.let { v -> entries.firstOrNull { it.name == v } }
    }
}
