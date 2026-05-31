package dev.antonlammers.macrotrac.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MacroCalculatorTest {

    @Test
    fun `kcalFromMacros calculates correctly`() {
        // 100g protein × 4 + 200g carbs × 4 + 70g fat × 9 = 400 + 800 + 630 = 1830
        assertEquals(1830.0, MacroCalculator.kcalFromMacros(100.0, 200.0, 70.0), 0.001)
    }

    @Test
    fun `carbsFromKcalAndMacros calculates correctly`() {
        // (2000 - 100×4 - 70×9) / 4 = (2000 - 400 - 630) / 4 = 242.5
        assertEquals(242.5, MacroCalculator.carbsFromKcalAndMacros(2000.0, 100.0, 70.0), 0.001)
    }

    @Test
    fun `carbsFromKcalAndMacros clamps to zero when result would be negative`() {
        assertEquals(0.0, MacroCalculator.carbsFromKcalAndMacros(100.0, 200.0, 50.0), 0.001)
    }

    @Test
    fun `kcalDelta is positive when kcal goal exceeds macros`() {
        // Macros give 1830, goal is 2000 → delta = +170
        assertEquals(170.0, MacroCalculator.kcalDelta(2000.0, 100.0, 200.0, 70.0), 0.001)
    }

    @Test
    fun `kcalDelta is negative when kcal goal is below macros`() {
        assertEquals(-170.0, MacroCalculator.kcalDelta(1660.0, 100.0, 200.0, 70.0), 0.001)
    }

    @Test
    fun `isConsistent returns true within threshold`() {
        // Macros give 1830, goal 1845 → delta 15 ≤ 25
        assertTrue(MacroCalculator.isConsistent(1845.0, 100.0, 200.0, 70.0))
    }

    @Test
    fun `isConsistent returns true when perfectly matching`() {
        assertTrue(MacroCalculator.isConsistent(1830.0, 100.0, 200.0, 70.0))
    }

    @Test
    fun `isConsistent returns false when delta exceeds threshold`() {
        // Macros give 1830, goal 2000 → delta 170 > 25
        assertFalse(MacroCalculator.isConsistent(2000.0, 100.0, 200.0, 70.0))
    }

    @Test
    fun `recommendedProteinG uses 2_2 per kg`() {
        assertEquals(176.0, MacroCalculator.recommendedProteinG(80.0), 0.001)
    }

    @Test
    fun `recommendedFatG uses 1_0 per kg`() {
        assertEquals(80.0, MacroCalculator.recommendedFatG(80.0), 0.001)
    }

    @Test
    fun `kcalFromMacros and carbsFromKcalAndMacros are inverse operations`() {
        val protein = 150.0
        val carbs = 250.0
        val fat = 70.0
        val kcal = MacroCalculator.kcalFromMacros(protein, carbs, fat)
        val derivedCarbs = MacroCalculator.carbsFromKcalAndMacros(kcal, protein, fat)
        assertEquals(carbs, derivedCarbs, 0.001)
    }
}
