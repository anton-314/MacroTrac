package dev.antonlammers.trainist.notification

import dev.antonlammers.trainist.domain.model.FoodEntry
import dev.antonlammers.trainist.domain.model.MealCategory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MealReminderDecisionTest {

    @Test
    fun `reminds when enabled and nothing logged today`() {
        assertTrue(shouldRemind(enabled = true, todaysEntries = emptyList()))
    }

    @Test
    fun `does not remind when a meal is already logged`() {
        assertFalse(shouldRemind(enabled = true, todaysEntries = listOf(entry())))
    }

    @Test
    fun `does not remind when disabled even with nothing logged`() {
        assertFalse(shouldRemind(enabled = false, todaysEntries = emptyList()))
    }

    @Test
    fun `does not remind when disabled and meals logged`() {
        assertFalse(shouldRemind(enabled = false, todaysEntries = listOf(entry())))
    }

    private fun entry() = FoodEntry(
        foodName = "Testessen",
        brand = null,
        amountGrams = 100.0,
        kcal = 200.0,
        proteinG = 10.0,
        carbsG = 20.0,
        fatG = 5.0,
        mealCategory = MealCategory.BREAKFAST,
        date = LocalDate.now(),
        timestampMs = 0L,
    )
}
