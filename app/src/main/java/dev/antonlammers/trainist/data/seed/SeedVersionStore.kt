package dev.antonlammers.trainist.data.seed

/**
 * Remembers which exercise-catalog snapshot version has already been seeded, so seeding runs once
 * per snapshot version (idempotent). Abstracted so the seeder stays Android-free and unit-testable.
 */
interface SeedVersionStore {
    suspend fun seededVersion(): Int
    suspend fun setSeededVersion(version: Int)
}
