package com.ackileo.telematics.ui.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ackileo.telematics.data.local.SharedPrefManager
import com.ackileo.telematics.data.remote.models.LoginRequest
import com.ackileo.telematics.data.remote.models.RegisterRequest
import com.ackileo.telematics.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
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
    private val prefManager: SharedPrefManager,
    private val firebaseAuth: FirebaseAuth, // 2. Add this line
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    // Now firebaseAuth is resolved!
    val isUserLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null

    fun logout() {
        firebaseAuth.signOut()
        prefManager.saveAuthToken("") // Clear token from prefs too if needed
    }
    /**
     * Authenticate user via Email or License Number
     */
    fun login(identifier: String, pass: String) {
        if (identifier.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Fields cannot be empty")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(LoginRequest(email = identifier, password = pass))

            result.fold(
                onSuccess = { response ->
                    prefManager.saveAuthToken(response.token)
                    _authState.value = AuthState.Success
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Authentication Failed")
                }
            )
        }
    }

    /**
     * Register a new driver account
     */
    fun register(
        fullName: String,
        email: String,
        nationalId: String,
        licenseNumber: String,
        licenseClass: String,
        phoneNumber: String,
        password: String,
    ) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all required fields")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val request = RegisterRequest(
                fullName = fullName,
                email = email,
                nationalId = nationalId,
                licenseNumber = licenseNumber,
                licenseClass = licenseClass,
                phoneNumber = phoneNumber,
                password = password
            )

            repository.register(request).fold(
                onSuccess = {
                    _authState.value = AuthState.Success
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Registration Failed")
                }
            )
        }
    }

    //firebase

    /**
     * Call this when navigating between Auth screens to clear previous errors/loading states
     */
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}