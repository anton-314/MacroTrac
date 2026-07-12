package dev.antonlammers.macrotrac.fake

import dev.antonlammers.macrotrac.domain.model.Exercise
import dev.antonlammers.macrotrac.domain.model.ExerciseType
import dev.antonlammers.macrotrac.domain.model.SetType
import dev.antonlammers.macrotrac.domain.model.TemplateExercise
import dev.antonlammers.macrotrac.domain.model.WorkoutSession
import dev.antonlammers.macrotrac.domain.model.WorkoutTemplate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/** Guards the test doubles the upcoming workout ViewModel tests will depend on. */
class WorkoutFakeRepositoriesTest {

    @Test
    fun `exercise fake upserts by stableId and sorts by name`() = runTest {
        val repo = FakeExerciseCatalogRepository()
        repo.upsertAll(listOf(ex("z", "Zucchini"), ex("a", "Apfel")))
        repo.upsertAll(listOf(ex("a", "Apfel neu")))

        assertEquals(listOf("Apfel neu", "Zucchini"), repo.exercises().first().map { it.name })
        assertEquals("Apfel neu", repo.exercise("a").first()?.name)
    }

    @Test
    fun `template fake assigns id and normalizes positions`() = runTest {
        val repo = FakeWorkoutTemplateRepository()
        val id = repo.save(
            WorkoutTemplate(
                stableId = "t", name = "Push",
                exercises = listOf(
                    TemplateExercise("a", 5, List(3) { SetType.NORMAL }),
                    TemplateExercise("b", 5, List(4) { SetType.NORMAL }),
                ),
            ),
        )
        val saved = repo.template(id).first()!!
        assertEquals(listOf(0, 1), saved.exercises.map { it.position })
    }

    @Test
    fun `session fake keeps at most one active session`() = runTest {
        val repo = FakeWorkoutSessionRepository()
        repo.save(WorkoutSession(stableId = "a", date = LocalDate.of(2026, 7, 9), isActive = true, startedAtMs = 1))
        repo.save(WorkoutSession(stableId = "b", date = LocalDate.of(2026, 7, 10), isActive = true, startedAtMs = 2))

        assertEquals("b", repo.activeSession().first()?.stableId)
        assertEquals(1, repo.sessions().first().count { it.isActive })

        repo.delete(repo.sessions().first().first { it.stableId == "b" }.id)
        // "a" was deactivated when "b" started; finishing/removing "b" leaves none active.
        assertNull(repo.activeSession().first())
    }
}

private fun ex(stableId: String, name: String) =
    Exercise(stableId = stableId, name = name, type = ExerciseType.WEIGHT_REPS, isCustom = false)
