package dev.antonlammers.macrotrac.domain.model

/**
 * Whether an exercise is a multi-joint [COMPOUND] or single-joint [ISOLATION] movement (from the
 * free-exercise-db `mechanic` field). Optional — many catalog entries and custom exercises leave it
 * unset. Persisted by [name]; [parse] returns `null` for missing/unknown values.
 */
enum class Mechanic {
    COMPOUND, ISOLATION;

    companion object {
        fun parse(raw: String?): Mechanic? =
            raw?.trim()?.uppercase()?.let { v -> entries.firstOrNull { it.name == v } }
    }
}
