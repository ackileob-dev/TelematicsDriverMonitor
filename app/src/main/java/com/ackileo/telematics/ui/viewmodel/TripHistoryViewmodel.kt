package com.ackileo.telematics.ui.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ackileo.telematics.data.remote.models.TripSummary
import com.ackileo.telematics.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class TripHistoryUiState(
    val trips: List<TripSummary> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val sortBy: SortOption = SortOption.DATE_DESC
)

enum class SortOption { DATE_DESC, DATE_ASC, SCORE_HIGH, DISTANCE_HIGH }

@HiltViewModel
class TripHistoryViewModel @Inject constructor(
    private val repository: TripRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(SortOption.DATE_DESC)

    val uiState: StateFlow<TripHistoryUiState> = combine(
        repository.getTripHistory(), // Assuming this returns Flow<List<TripSummary>>
        _searchQuery,
        _sortOption
    ) { trips, query, sort ->
        val filtered = trips.filter {
            it.totalDistanceKm.toString().contains(query) ||
                    it.safetyScore.toString().contains(query)
        }

        val sorted = when (sort) {
            SortOption.DATE_DESC -> filtered.sortedByDescending { it.startTime }
            SortOption.DATE_ASC -> filtered.sortedBy { it.startTime }
            SortOption.SCORE_HIGH -> filtered.sortedByDescending { it.safetyScore }
            SortOption.DISTANCE_HIGH -> filtered.sortedByDescending { it.totalDistanceKm }
        }

        TripHistoryUiState(trips = sorted, searchQuery = query, sortBy = sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TripHistoryUiState(isLoading = true))

    fun onSearch(query: String) { _searchQuery.value = query }
    fun onSort(option: SortOption) { _sortOption.value = option }
}