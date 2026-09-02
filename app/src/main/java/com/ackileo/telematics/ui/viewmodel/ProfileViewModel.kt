package com.ackileo.telematics.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ackileo.telematics.data.local.SharedPrefManager
import com.ackileo.telematics.data.local.TokenManager
import com.ackileo.telematics.data.remote.dto.DriverDto
import com.ackileo.telematics.data.remote.dto.VehicleDto
import com.ackileo.telematics.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val driver: DriverDto) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val tokenManager: TokenManager,
    private val prefManager: SharedPrefManager,
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState = _profileState.asStateFlow()

    private val _actionState = MutableStateFlow<AuthState>(AuthState.Idle)
    val actionState = _actionState.asStateFlow()

    init {
        loadUserProfile()
    }

    /**
     * Fetches the current authenticated user's profile from the backend.
     * Uses the GET /api/auth/me endpoint.
     */
    fun loadUserProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            val result = repository.getCurrentUser()

            result.fold(
                onSuccess = { driver ->
                    _profileState.value = ProfileState.Success(driver)
                },
                onFailure = { error ->
                    _profileState.value =
                        ProfileState.Error(error.message ?: "Failed to load profile")
                }
            )
        }
    }

    /**
     * Logout functionality that:
     * 1. Removes JWT from local storage
     * 2. Clears authentication state
     * 3. Clears cached sensitive user information
     * 4. Ensures subsequent protected API requests do not contain the old token
     */
    fun logout() {
        viewModelScope.launch {
            _actionState.value = AuthState.Loading

            repository.logout()

            // Clear all tokens and driver information from encrypted storage
            tokenManager.clearAuthState()

            // Clear all user profile data from shared preferences
            prefManager.clearUserProfile()
            prefManager.clearAll()

            _profileState.value = ProfileState.Idle
            _actionState.value = AuthState.Success
        }
    }

    fun resetActionState() {
        _actionState.value = AuthState.Idle
    }
}

