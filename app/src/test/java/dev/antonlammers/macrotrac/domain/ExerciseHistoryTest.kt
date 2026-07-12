package dev.antonlammers.macrotrac.domain

import dev.antonlammers.macrotrac.domain.model.ExerciseType
import dev.antonlammers.macrotrac.domain.model.SessionExercise
import dev.antonlammers.macrotrac.domain.model.SetEntry
import dev.antonlammers.macrotrac.domain.model.SetType
import dev.antonlammers.macrotrac.domain.model.WorkoutSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ExerciseHistoryTest {

    private val noBodyWeight: (LocalDate) -> Double? = { null }

    private fun set(weightKg: Double, reps: Int, type: SetType = SetType.NORMAL, position: Int = 0) =
        SetEntry(position = position, weightKg = weightKg, reps = reps, type = type)

    private fun session(
        id: Long,
        date: LocalDate,
        exerciseStableId: String,
        sets: List<SetEntry>,
        startedAtMs: Long = 0L,
        isActive: Boolean = false,
    ) = WorkoutSession(
        id = id,
        stableId = "s$id",
        date = date,
        isActive = isActive,
        startedAtMs = startedAtMs,
        endedAtMs = if (isActive) null else startedAtMs + 1,
        exercises = listOf(SessionExercise(exerciseStableId = exerciseStableId, position = 0, sets = sets)),
    )

    @Test
    fun `set log is newest-first and only includes sessions with performed sets`() {
        val history = listOf(
            session(1, LocalDate.of(2026, 7, 1), "bench", listOf(set(80.0, 5))),
            session(2, LocalDate.of(2026, 7, 8), "bench", listOf(set(85.0, 5))),
            session(3, LocalDate.of(2026, 7, 5), "bench", listOf(set(0.0, 0))), // no performed set → dropped
        )

        val data = ExerciseHistory.build(history, "bench", ExerciseType.WEIGHT_REPS, noBodyWeight)

        assertEquals(listOf(LocalDate.of(2026, 7, 8), LocalDate.of(2026, 7, 1)), data.sessions.map { it.date })
    }

    @Test
    fun `set numbers are re-based over the performed sets in order`() {
        val history = listOf(
            session(1, LocalDate.of(2026, 7, 1), "bench", listOf(
                set(60.0, 8, position = 0),
                set(0.0, 0, position = 1),   // skipped empty set
                set(80.0, 5, position = 2),
            )),
        )

        val data = ExerciseHistory.build(history, "bench", ExerciseType.WEIGHT_REPS, noBodyWeight)

        val sets = data.sessions.single().sets
        assertEquals(listOf(1, 2), sets.map { it.setNumber })
        assertEquals(listOf(60.0, 80.0), sets.map { it.weightKg })
    }

    @Test
    fun `personal record is the max effective weight over non-warm-up sets`() {
        val history = listOf(
            session(1, LocalDate.of(2026, 7, 1), "bench", listOf(
                set(120.0, 3, type = SetType.WARMUP), // heaviest but a warm-up → not the PR
                set(100.0, 5),
            )),
        )

        val data = ExerciseHistory.build(history, "bench", ExerciseType.WEIGHT_REPS, noBodyWeight)

        assertEquals(100.0, data.personalRecordKg!!, 0.0)
        val sets = data.sessions.single().sets
        assertFalse(sets.first { it.weightKg == 120.0 }.isPersonalRecord) // warm-up
        assertTrue(sets.first { it.weightKg == 100.0 }.isPersonalRecord)
    }

    @Test
    fun `on a tie the earliest set keeps the PR trophy`() {
        val history = listOf(
            session(1, LocalDate.of(2026, 7, 1), "bench", listOf(set(100.0, 5))),
            session(2, LocalDate.of(2026, 7, 8), "bench", listOf(set(100.0, 8))), // same weight later → not a new PR
        )

        val data = ExerciseHistory.build(history, "bench", ExerciseType.WEIGHT_REPS, noBodyWeight)

        assertEquals(100.0, data.personalRecordKg!!, 0.0)
        val julyFirst = data.sessions.first { it.date == LocalDate.of(2026, 7, 1) }
        val julyEighth = data.sessions.first { it.date == LocalDate.of(2026, 7, 8) }
        assertTrue(julyFirst.sets.single().isPersonalRecord)
        assertFalse(julyEighth.sets.single().isPersonalRecord)
    }

    @Test
    fun `bodyweight PR uses tracked body weight plus added weight`() {
        val history = listOf(session(1, LocalDate.of(2026, 7, 1), "pullup", listOf(set(10.0, 8))))

        val data = ExerciseHistory.build(history, "pullup", ExerciseType.BODYWEIGHT) { 80.0 }

        assertEquals(90.0, data.personalRecordKg!!, 0.0) // 80 body + 10 added
        assertTrue(data.sessions.single().sets.single().isPersonalRecord)
    }

    @Test
    fun `an exercise never performed has no data and no PR`() {
        val history = listOf(session(1, LocalDate.of(2026, 7, 1), "squat", listOf(set(140.0, 5))))

        val data = ExerciseHistory.build(history, "bench", ExerciseType.WEIGHT_REPS, noBodyWeight)

        assertFalse(data.hasData)
        assertTrue(data.sessions.isEmpty())
        assertNull(data.personalRecordKg)
    }

    @Test
    fun `only warm-up sets yield a log but no PR`() {
        val history = listOf(
            session(1, LocalDate.of(2026, 7, 1), "bench", listOf(set(60.0, 10, type = SetType.WARMUP))),
        )

        val data = ExerciseHistory.build(history, "bench", ExerciseType.WEIGHT_REPS, noBodyWeight)

        assertEquals(1, data.sessions.single().sets.size) // the warm-up is still shown
        assertNull(data.personalRecordKg)
        assertFalse(data.sessions.single().sets.single().isPersonalRecord)
    }
}
