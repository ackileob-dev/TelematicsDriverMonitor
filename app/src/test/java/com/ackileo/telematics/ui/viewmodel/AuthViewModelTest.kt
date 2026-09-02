package com.ackileo.telematics.ui.viewmodel

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ackileo.telematics.data.local.SessionStateStore
import com.ackileo.telematics.data.remote.dto.LoginResponse
import com.ackileo.telematics.data.remote.dto.LoginRequest
import com.ackileo.telematics.data.remote.dto.RegisterRequest
import com.ackileo.telematics.data.repository.AuthRepository
import com.ackileo.telematics.data.repository.DocumentType
import com.ackileo.telematics.utils.UploadStatus
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import com.ackileo.telematics.test.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeAuthRepository : AuthRepository {
        var loginResult: Result<LoginResponse> = Result.failure(IllegalStateException("Login result not configured"))
        var registerResult: Result<Unit> = Result.success(Unit)
        var loginCallCount: Int = 0
        var logoutCallCount: Int = 0
        var loginHandler: (suspend (LoginRequest) -> Result<LoginResponse>)? = null

        override suspend fun register(request: RegisterRequest): Result<Unit> = registerResult

        override suspend fun login(request: LoginRequest): Result<LoginResponse> {
            loginCallCount++
            return loginHandler?.invoke(request) ?: loginResult
        }

        override suspend fun sendPasswordReset(email: String): Result<Unit> = Result.success(Unit)

        override fun logout() {
            logoutCallCount++
        }

        override fun getCurrentUser(): FirebaseUser? = null

        override suspend fun uploadDriverDocument(
            userId: String,
            uri: Uri,
            docType: DocumentType,
        ): Flow<UploadStatus> = emptyFlow()
    }

    // AuthViewModel only depends on AuthRepository + TokenManager.
    // SharedPrefManager and FirebaseAuth are used inside AuthRepositoryImpl, not the ViewModel.
    private val repository = FakeAuthRepository()
    private val tokenManager = object : SessionStateStore {
        override fun hasAccessToken(): Boolean = false
    }

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        viewModel = AuthViewModel(
            repository = repository,
            tokenManager = tokenManager,
        )
    }

    @Test
    fun testInitialStateIsIdle() {
        assertEquals(AuthState.Idle, viewModel.authState.value)
    }

    @Test
    fun testLoginSuccessUpdatesStateToSuccess() = runTest {
        val response = LoginResponse(
            token = "test_token",
            driverId = "driver_123",
            fullName = "John Doe",
        )
        val allowLoginToFinish = CompletableDeferred<Unit>()
        repository.loginHandler = {
            allowLoginToFinish.await()
            Result.success(response)
        }

        // AuthViewModel.login() parameter names are `email` and `password`.
        viewModel.login(email = "user@test.com", password = "password123")

        assertTrue(viewModel.authState.value is AuthState.Loading)

        allowLoginToFinish.complete(Unit)

        advanceUntilIdle()

        assertTrue(viewModel.authState.value is AuthState.Success)
    }

    @Test
    fun testLoginFailureUpdatesStateToError() = runTest {
        val error = "Unauthorized"
        repository.loginResult = Result.failure(Exception(error))

        viewModel.login(email = "user@test.com", password = "wrong_password")

        advanceUntilIdle()

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals(error, (state as AuthState.Error).message)
    }

    @Test
    fun testLoginWithBlankEmailReturnsValidationErrorImmediately() = runTest {
        // validateLogin() returns "Email is required" when email is blank — not "Fields cannot be empty".
        viewModel.login(email = "", password = "")

        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Email is required", (state as AuthState.Error).message)
        assertEquals(0, repository.loginCallCount)
    }

    @Test
    fun testLogoutCallsRepositoryLogoutAndResetsState() {
        // AuthViewModel.logout() delegates to repository.logout() then resets state to Idle.
        viewModel.logout()

        assertEquals(1, repository.logoutCallCount)
        assertEquals(AuthState.Idle, viewModel.authState.value)
    }

    @Test
    fun testResetAuthStateReturnsStateToIdle() = runTest {
        repository.loginResult = Result.failure(Exception("Failure"))

        viewModel.login(email = "user@test.com", password = "password123")

        advanceUntilIdle()

        assertTrue(viewModel.authState.value is AuthState.Error)

        viewModel.resetAuthState()

        assertEquals(AuthState.Idle, viewModel.authState.value)
    }
}