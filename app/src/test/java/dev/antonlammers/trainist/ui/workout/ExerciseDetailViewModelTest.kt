package dev.antonlammers.trainist.ui.workout

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import dev.antonlammers.trainist.domain.model.Exercise
import dev.antonlammers.trainist.domain.model.ExerciseType
import dev.antonlammers.trainist.domain.model.SessionExercise
import dev.antonlammers.trainist.domain.model.SetEntry
import dev.antonlammers.trainist.domain.model.WorkoutSession
import dev.antonlammers.trainist.fake.FakeExerciseCatalogRepository
import dev.antonlammers.trainist.fake.FakeWeightRepository
import dev.antonlammers.trainist.fake.FakeWorkoutSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var sessions: FakeWorkoutSessionRepository
    private lateinit var catalog: FakeExerciseCatalogRepository
    private lateinit var weight: FakeWeightRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        sessions = FakeWorkoutSessionRepository()
        catalog = FakeExerciseCatalogRepository()
        weight = FakeWeightRepository()
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(exerciseStableId: String) =
        ExerciseDetailViewModel(sessions, catalog, weight, SavedStateHandle(mapOf("exerciseStableId" to exerciseStableId)))

    private suspend fun saveSession(id: String, date: LocalDate, weightKg: Double, reps: Int) {
        sessions.save(
            WorkoutSession(
                stableId = id,
                date = date,
                isActive = false,
                startedAtMs = date.toEpochDay(),
                endedAtMs = date.toEpochDay() + 1,
                exercises = listOf(
                    SessionExercise(
                        exerciseStableId = "bench",
                        position = 0,
                        sets = listOf(SetEntry(position = 0, weightKg = weightKg, reps = reps)),
                    ),
                ),
            ),
        )
    }

    @Test
    fun `history, PR and strength progression surface for the exercise`() = runTest {
        catalog.upsertAll(
            listOf(Exercise("bench", "Bench Press", ExerciseType.WEIGHT_REPS, isCustom = false, primaryMuscles = listOf("chest"))),
        )
        saveSession("s1", LocalDate.of(2026, 7, 1), 100.0, 5)
        saveSession("s2", LocalDate.of(2026, 7, 8), 105.0, 5)

        viewModel("bench").uiState.test {
            var state = awaitItem()
            while (state.loading || state.history.sessions.isEmpty()) state = awaitItem()

            assertEquals("Bench Press", state.exercise?.name)
            assertEquals(2, state.history.sessions.size)
            assertEquals(LocalDate.of(2026, 7, 8), state.history.sessions.first().date) // newest first
            assertEquals(105.0, state.history.personalRecordKg!!, 0.0)
            assertEquals(2, state.strength.samples.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `an unknown exercise resolves to empty history`() = runTest {
        viewModel("does_not_exist").uiState.test {
            var state = awaitItem()
            while (state.loading) state = awaitItem()

            assertNull(state.exercise)
            assertFalse(state.history.hasData)
            assertTrue(state.strength.samples.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
