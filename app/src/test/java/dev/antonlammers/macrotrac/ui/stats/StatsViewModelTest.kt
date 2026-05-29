package dev.antonlammers.macrotrac.ui.stats

import app.cash.turbine.test
import dev.antonlammers.macrotrac.domain.model.FoodEntry
import dev.antonlammers.macrotrac.domain.model.MealCategory
import dev.antonlammers.macrotrac.fake.FakeFoodEntryRepository
import dev.antonlammers.macrotrac.fake.FakeGoalRepository
import dev.antonlammers.macrotrac.fake.FakeWeightRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var foodRepo: FakeFoodEntryRepository
    private lateinit var weightRepo: FakeWeightRepository
    private lateinit var goalRepo: FakeGoalRepository
    private lateinit var viewModel: StatsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        foodRepo = FakeFoodEntryRepository()
        weightRepo = FakeWeightRepository()
        goalRepo = FakeGoalRepository()
        viewModel = StatsViewModel(foodRepo, weightRepo, goalRepo)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `initial state has WEEK time range`() = runTest {
        viewModel.uiState.test {
            assertEquals(TimeRange.WEEK, awaitItem().timeRange)
        }
    }

    @Test
    fun `switching to MONTH changes range and emits 30 calorie points`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial WEEK

            viewModel.setTimeRange(TimeRange.MONTH)
            val state = awaitItem()

            assertEquals(TimeRange.MONTH, state.timeRange)
            assertEquals(30, state.caloriePoints.size)
        }
    }

    @Test
    fun `switching to YEAR emits 12 monthly calorie points`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.setTimeRange(TimeRange.YEAR)
            val state = awaitItem()

            assertEquals(TimeRange.YEAR, state.timeRange)
            assertEquals(12, state.caloriePoints.size)
        }
    }

    @Test
    fun `today's food entry appears in calorie points for WEEK`() = runTest {
        val today = LocalDate.now()
        foodRepo.add(buildEntry(kcal = 500.0, date = today))

        viewModel.uiState.test {
            awaitItem() // initial empty state
            val state = awaitItem() // populated state

            val todayPoint = state.caloriePoints.last()
            assertEquals(500.0, todayPoint.value, 0.001)
        }
    }

    @Test
    fun `entries outside range are not included`() = runTest {
        foodRepo.add(buildEntry(kcal = 999.0, date = LocalDate.now().minusDays(10)))

        viewModel.uiState.test {
            awaitItem() // initial empty
            val state = awaitItem() // populated — 7 points, all 0
            assertEquals(7, state.caloriePoints.size)
            assertTrue(state.caloriePoints.all { it.value == 0.0 })
        }
    }

    private fun buildEntry(kcal: Double, date: LocalDate) = FoodEntry(
        foodName = "Test",
        brand = null,
        amountGrams = 100.0,
        kcal = kcal,
        proteinG = 10.0,
        carbsG = 20.0,
        fatG = 5.0,
        date = date,
        timestampMs = System.currentTimeMillis(),
        mealCategory = MealCategory.SNACK,
    )
}
