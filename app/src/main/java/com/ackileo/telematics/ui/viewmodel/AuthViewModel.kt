package com.ackileo.telematics.ui.viewmodel
import androidx.core.util.PatternsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ackileo.telematics.data.local.SessionStateStore
import com.ackileo.telematics.data.remote.dto.LoginRequest
import com.ackileo.telematics.data.remote.dto.RegisterRequest
import com.ackileo.telematics.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val tokenManager: SessionStateStore,
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    val isUserLoggedIn: Boolean
        get() = tokenManager.hasAccessToken()

    fun logout() {
        repository.logout()
        _authState.value = AuthState.Idle
    }

    fun login(email: String, password: String) {
        val validationError = validateLogin(email, password)
        if (validationError != null) {
            _authState.value = AuthState.Error(validationError)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(LoginRequest(email = email.trim(), password = password))

            result.fold(
                onSuccess = {
                    _authState.value = AuthState.Success
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Authentication failed")
                }
            )
        }
    }

    fun register(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String,
        nationalId: String = "",
        licenseNumber: String = "",
        licenseClass: String = "",
    ) {
        val validationError = validateRegister(fullName, email, password)
        if (validationError != null) {
            _authState.value = AuthState.Error(validationError)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val request = RegisterRequest(
                fullName = fullName.trim(),
                email = email.trim(),
                password = password
                    .trim(),
                phone = phoneNumber.trim().takeIf { it.isNotBlank() }
            )

            repository.register(request).fold(
                onSuccess = {
                    _authState.value = AuthState.Success
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Registration failed")
                }
            )
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    private fun validateLogin(email: String, password: String): String? {
        if (email.isBlank()) return "Email is required"
        if (!PatternsCompat.EMAIL_ADDRESS.matcher(email.trim()).matches()) return "Enter a valid email address"
        if (password.isBlank()) return "Password is required"
        return null
    }

    private fun validateRegister(fullName: String, email: String, password: String): String? {
        if (fullName.isBlank()) return "Full name is required"
        if (email.isBlank()) return "Email is required"
        if (!PatternsCompat.EMAIL_ADDRESS.matcher(email.trim()).matches()) return "Enter a valid email address"
        if (password.isBlank()) return "Password is required"
        if (password.length < 6) return "Password must be at least 6 characters"
        return null
    }
}