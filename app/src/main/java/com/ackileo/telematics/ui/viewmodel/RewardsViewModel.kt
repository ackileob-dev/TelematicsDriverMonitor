package com.ackileo.telematics.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ackileo.telematics.data.remote.dto.AlertDto
import com.ackileo.telematics.data.remote.dto.RewardDto
import com.ackileo.telematics.data.remote.dto.SafetyScoreDto
import com.ackileo.telematics.data.repository.RewardsSafetyAlertsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RewardsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val rewards: List<RewardDto> = emptyList(),
    val availableRewards: List<RewardDto> = emptyList(),
    val latestSafetyScore: SafetyScoreDto? = null,
    val safetyScoreHistory: List<SafetyScoreDto> = emptyList(),
    val alerts: List<AlertDto> = emptyList(),
    val isEmpty: Boolean = false,
    val isUpdatingAlert: Boolean = false,
)

@HiltViewModel
class RewardsViewModel @Inject constructor(
    private val repository: RewardsSafetyAlertsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RewardsUiState(isLoading = true))
    val uiState: StateFlow<RewardsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun retry() {
        loadData()
    }

    fun markAlertRead(alert: AlertDto) {
        if (alert.isRead == true || alert.read == true || _uiState.value.isUpdatingAlert) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdatingAlert = true, errorMessage = null)

            repository.markAlertRead(alert).fold(
                onSuccess = { updated ->
                    val updatedAlerts = _uiState.value.alerts.map {
                        if (it.id == updated.id) updated else it
                    }
                    _uiState.value = _uiState.value.copy(
                        alerts = updatedAlerts,
                        isUpdatingAlert = false,
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdatingAlert = false,
                        errorMessage = error.message ?: "Failed to update alert",
                    )
                },
            )
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            repository.loadAll().fold(
                onSuccess = { data ->
                    val available = data.rewards.filter { reward ->
                        reward.available ?: !(reward.isRedeemed ?: false)
                    }

                    val isEmpty = data.rewards.isEmpty() && data.safetyScoreHistory.isEmpty() && data.alerts.isEmpty()

                    _uiState.value = RewardsUiState(
                        isLoading = false,
                        rewards = data.rewards,
                        availableRewards = available,
                        latestSafetyScore = data.latestSafetyScore,
                        safetyScoreHistory = data.safetyScoreHistory,
                        alerts = data.alerts,
                        isEmpty = isEmpty,
                    )
                },
                onFailure = { error ->
                    _uiState.value = RewardsUiState(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load data",
                    )
                },
            )
        }
    }
}

