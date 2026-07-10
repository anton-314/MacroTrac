package dev.antonlammers.macrotrac.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class WorkoutModelTest {

    @Test
    fun `ExerciseType parse falls back to WEIGHT_REPS`() {
        assertEquals(ExerciseType.BODYWEIGHT, ExerciseType.parse("bodyweight"))
        assertEquals(ExerciseType.WEIGHT_REPS, ExerciseType.parse("WEIGHT_REPS"))
        assertEquals(ExerciseType.WEIGHT_REPS, ExerciseType.parse(null))
        assertEquals(ExerciseType.WEIGHT_REPS, ExerciseType.parse("nonsense"))
    }

    @Test
    fun `SetType parse falls back to NORMAL`() {
        assertEquals(SetType.WARMUP, SetType.parse(" warmup "))
        assertEquals(SetType.FAILURE, SetType.parse("FAILURE"))
        assertEquals(SetType.NORMAL, SetType.parse(null))
        assertEquals(SetType.NORMAL, SetType.parse("x"))
        assertEquals(listOf(SetType.WARMUP, SetType.NORMAL, SetType.DROP, SetType.FAILURE), SetType.selectable)
    }

    @Test
    fun `Mechanic parse returns null for missing or unknown`() {
        assertEquals(Mechanic.COMPOUND, Mechanic.parse("compound"))
        assertEquals(Mechanic.ISOLATION, Mechanic.parse("ISOLATION"))
        assertNull(Mechanic.parse(null))
        assertNull(Mechanic.parse(""))
        assertNull(Mechanic.parse("static"))
    }

    @Test
    fun `session duration is null while active and computed once ended`() {
        val active = WorkoutSession(
            stableId = "s", date = LocalDate.of(2026, 7, 10), isActive = true, startedAtMs = 1_000,
        )
        assertNull(active.durationMs)

        val ended = active.copy(isActive = false, endedAtMs = 4_500)
        assertEquals(3_500L, ended.durationMs)
    }
}
