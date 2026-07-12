package dev.antonlammers.macrotrac.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RestTimerTest {

    @Test
    fun `start anchors the end time and reports remaining from now`() {
        val t = RestTimer.start(nowMs = 1_000, seconds = 90)
        assertEquals(90, t.totalSeconds)
        assertEquals(91_000, t.endAtMs)
        assertEquals(90_000, t.remainingMs(1_000))
        assertEquals(30_000, t.remainingMs(61_000))
        assertFalse(t.isFinished(1_000))
    }

    @Test
    fun `start clamps to the minimum duration`() {
        assertEquals(RestTimer.MIN_REST_SECONDS, RestTimer.start(0, seconds = 2).totalSeconds)
    }

    @Test
    fun `remaining never goes negative and finish triggers at the end`() {
        val t = RestTimer.start(0, 60)
        assertEquals(0, t.remainingMs(999_999))
        assertTrue(t.isFinished(60_000))
        assertTrue(t.isFinished(70_000))
    }

    @Test
    fun `pause freezes the remaining time`() {
        val paused = RestTimer.start(1_000, 90).paused(31_000)
        assertTrue(paused.isPaused)
        assertEquals(60_000, paused.remainingMs(31_000))
        assertEquals(60_000, paused.remainingMs(999_000)) // frozen regardless of now
        assertFalse(paused.isFinished(999_000))
    }

    @Test
    fun `pause is idempotent and resume re-anchors from now`() {
        val paused = RestTimer.start(1_000, 90).paused(31_000)
        assertEquals(paused, paused.paused(50_000))

        val resumed = paused.resumed(100_000)
        assertFalse(resumed.isPaused)
        assertEquals(160_000, resumed.endAtMs)
        assertEquals(60_000, resumed.remainingMs(100_000))
    }

    @Test
    fun `resume on a running timer is a no-op`() {
        val running = RestTimer.start(1_000, 90)
        assertEquals(running, running.resumed(5_000))
    }

    @Test
    fun `adjust changes running remaining and total together`() {
        val t = RestTimer.start(1_000, 90)
        val plus = t.adjusted(1_000, 15)
        assertEquals(105, plus.totalSeconds)
        assertEquals(105_000, plus.remainingMs(1_000))
    }

    @Test
    fun `adjust never pushes running remaining below zero`() {
        val t = RestTimer.start(1_000, 90)
        val minus = t.adjusted(1_000, -200)
        assertEquals(RestTimer.MIN_REST_SECONDS, minus.totalSeconds)
        assertEquals(0, minus.remainingMs(1_000))
    }

    @Test
    fun `adjust while paused changes the frozen remaining`() {
        val paused = RestTimer.start(1_000, 90).paused(31_000) // 60s left
        val plus = paused.adjusted(999_000, 15)
        assertTrue(plus.isPaused)
        assertEquals(105, plus.totalSeconds)
        assertEquals(75_000, plus.remainingMs(999_000))
    }
}
