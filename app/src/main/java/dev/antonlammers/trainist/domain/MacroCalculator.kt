package dev.antonlammers.trainist.domain

import kotlin.math.abs

object MacroCalculator {

    const val PROTEIN_PER_KG = 2.2
    const val FAT_PER_KG = 1.0

    private const val KCAL_PER_PROTEIN = 4.0
    private const val KCAL_PER_CARBS = 4.0
    private const val KCAL_PER_FAT = 9.0

    // Warn when kcal goal deviates more than this from what the macros add up to.
    const val CONSISTENCY_THRESHOLD_KCAL = 25.0

    fun kcalFromMacros(proteinG: Double, carbsG: Double, fatG: Double): Double =
        proteinG * KCAL_PER_PROTEIN + carbsG * KCAL_PER_CARBS + fatG * KCAL_PER_FAT

    fun carbsFromKcalAndMacros(kcal: Double, proteinG: Double, fatG: Double): Double =
        ((kcal - proteinG * KCAL_PER_PROTEIN - fatG * KCAL_PER_FAT) / KCAL_PER_CARBS)
            .coerceAtLeast(0.0)

    fun recommendedProteinG(bodyWeightKg: Double): Double = bodyWeightKg * PROTEIN_PER_KG
    fun recommendedFatG(bodyWeightKg: Double): Double = bodyWeightKg * FAT_PER_KG

    // Positive = kcal goal is higher than macros; negative = lower.
    fun kcalDelta(kcal: Double, proteinG: Double, carbsG: Double, fatG: Double): Double =
        kcal - kcalFromMacros(proteinG, carbsG, fatG)

    fun isConsistent(kcal: Double, proteinG: Double, carbsG: Double, fatG: Double): Boolean =
        abs(kcalDelta(kcal, proteinG, carbsG, fatG)) <= CONSISTENCY_THRESHOLD_KCAL
}
