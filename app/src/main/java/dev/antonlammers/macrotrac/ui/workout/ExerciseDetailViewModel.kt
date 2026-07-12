package dev.antonlammers.macrotrac.ui.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.antonlammers.macrotrac.domain.ExerciseHistory
import dev.antonlammers.macrotrac.domain.ExerciseHistoryData
import dev.antonlammers.macrotrac.domain.WorkoutMetrics
import dev.antonlammers.macrotrac.domain.model.Exercise
import dev.antonlammers.macrotrac.domain.model.ExerciseType
import dev.antonlammers.macrotrac.domain.model.WeightEntry
import dev.antonlammers.macrotrac.domain.repository.ExerciseCatalogRepository
import dev.antonlammers.macrotrac.domain.repository.WeightRepository
import dev.antonlammers.macrotrac.domain.repository.WorkoutSessionRepository
import dev.antonlammers.macrotrac.ui.stats.StrengthChartData
import dev.antonlammers.macrotrac.ui.stats.WorkoutSeries
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseDetailUiState(
    val loading: Boolean = true,
    val exercise: Exercise? = null,
    val history: ExerciseHistoryData = ExerciseHistoryData(),
    val strength: StrengthChartData = StrengthChartData(),
)

/**
 * Drives the per-exercise detail screen (spec §3.8): it joins the exercise's catalog metadata with
 * its complete training history — the chronological set log + current max-weight PR ([ExerciseHistory],
 * pure) — and the all-time strength (estimated-1RM) progression for the mini-chart ([WorkoutSeries],
 * reused from the stats tab). Body weight for bodyweight-exercise effective weights is resolved from
 * all weigh-ins (loaded once).
 */
@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    private val sessions: WorkoutSessionRepository,
    private val catalog: ExerciseCatalogRepository,
    private val weight: WeightRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val exerciseStableId: String = savedStateHandle.get<String>("exerciseStableId").orEmpty()

    private val _allWeights = MutableStateFlow<List<WeightEntry>>(emptyList())

    init {
        viewModelScope.launch { _allWeights.value = weight.allEntries() }
    }

    val uiState: StateFlow<ExerciseDetailUiState> = combine(
        catalog.exercise(exerciseStableId),
        sessions.sessions(),
        _allWeights,
    ) { exercise, history, weights ->
        val type = exercise?.type ?: ExerciseType.WEIGHT_REPS
        val bodyWeightForDate = { date: java.time.LocalDate -> WorkoutMetrics.resolveBodyWeightKg(weights, date) }

        val historyData = ExerciseHistory.build(history, exerciseStableId, type, bodyWeightForDate)
        val samples = WorkoutSeries.strengthHistory(history, exerciseStableId, { type }, bodyWeightForDate)
        val strength = if (samples.isEmpty()) {
            StrengthChartData()
        } else {
            val (minKg, maxKg) = WorkoutSeries.bounds(samples)
            StrengthChartData(samples, samples.first().date, samples.last().date, minKg, maxKg)
        }

        ExerciseDetailUiState(loading = false, exercise = exercise, history = historyData, strength = strength)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExerciseDetailUiState())
}
