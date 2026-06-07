package dev.antonlammers.macrotrac.util

import org.junit.Assert.assertEquals
import org.junit.Test

class DecimalInputTest {

    @Test
    fun `normalizeDecimal replaces comma with period`() {
        assertEquals("1.5", "1,5".normalizeDecimal())
    }

    @Test
    fun `normalizeDecimal leaves period unchanged`() {
        assertEquals("1.5", "1.5".normalizeDecimal())
    }

    @Test
    fun `normalizeDecimal handles whole numbers`() {
        assertEquals("150", "150".normalizeDecimal())
    }

    @Test
    fun `normalizeDecimal handles empty string`() {
        assertEquals("", "".normalizeDecimal())
    }

    @Test
    fun `normalizeDecimal result parses correctly`() {
        assertEquals(1.5, "1,5".normalizeDecimal().toDoubleOrNull())
        assertEquals(1.5, "1.5".normalizeDecimal().toDoubleOrNull())
    }
}
