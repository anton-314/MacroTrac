package dev.antonlammers.macrotrac.domain.model

/**
 * The role a logged set plays. Warm-up sets are excluded from volume (handled later in the
 * calculations layer); the type is also a visual marker in the session/history UI.
 *
 * Persisted by [name]; [parse] reads it back defensively so unknown/missing values fall back to
 * [NORMAL].
 */
enum class SetType {
    WARMUP, NORMAL, DROP, FAILURE;

    companion object {
        /** The set types a user can pick, in display order. */
        val selectable: List<SetType> = entries.toList()

        fun parse(raw: String?): SetType =
            raw?.trim()?.uppercase()?.let { v -> entries.firstOrNull { it.name == v } } ?: NORMAL
    }
}
