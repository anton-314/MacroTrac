package dev.antonlammers.macrotrac.notification

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

class MealReminderSchedulerTest {

    @Test
    fun `before 17h schedules for the same day`() {
        val now = LocalDateTime.of(2026, 6, 10, 9, 0)
        val expected = Duration.between(now, LocalDateTime.of(2026, 6, 10, 17, 0)).toMillis()
        assertEquals(expected, MealReminderScheduler.initialDelayMillis(now))
    }

    @Test
    fun `just before 17h is still the same day`() {
        val now = LocalDateTime.of(2026, 6, 10, 16, 59)
        val expected = Duration.ofMinutes(1).toMillis()
        assertEquals(expected, MealReminderScheduler.initialDelayMillis(now))
    }

    @Test
    fun `after 17h schedules for the next day`() {
        val now = LocalDateTime.of(2026, 6, 10, 18, 30)
        val expected = Duration.between(now, LocalDateTime.of(2026, 6, 11, 17, 0)).toMillis()
        assertEquals(expected, MealReminderScheduler.initialDelayMillis(now))
    }

    @Test
    fun `exactly at 17h schedules for the next day`() {
        val now = LocalDateTime.of(2026, 6, 10, 17, 0)
        val expected = Duration.ofDays(1).toMillis()
        assertEquals(expected, MealReminderScheduler.initialDelayMillis(now))
    }
}
