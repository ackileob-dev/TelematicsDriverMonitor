package com.ackileo.telematics.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ackileo.telematics.data.local.SharedPrefManager
import com.ackileo.telematics.data.remote.models.AuthResponse
import com.ackileo.telematics.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private val repository: AuthRepository = mock()
    private val prefManager: SharedPrefManager = mock()
    private val firebaseAuth: FirebaseAuth = mock()

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        viewModel = AuthViewModel(
            repository = repository,
            prefManager = prefManager,
            firebaseAuth = firebaseAuth
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStateIsIdle() {
        assertEquals(AuthState.Idle, viewModel.authState.value)
    }

    @Test
    fun testLoginSuccessUpdatesStateToSuccessAndSavesToken() = runTest {
        val token = "test_token"

        val response = AuthResponse(
            token = token,
            driverId = "driver_123",
            fullName = "John Doe"
        )

        whenever(repository.login(any()))
            .thenReturn(Result.success(response))

        viewModel.login(
            identifier = "user@test.com",
            pass = "password"
        )

        assertTrue(viewModel.authState.value is AuthState.Loading)

        advanceUntilIdle()

        verify(prefManager).saveAuthToken(token)

        assertTrue(viewModel.authState.value is AuthState.Success)
    }

    @Test
    fun testLoginFailureUpdatesStateToError() = runTest {
        val error = "Unauthorized"

        whenever(repository.login(any()))
            .thenReturn(Result.failure(Exception(error)))

        viewModel.login(
            identifier = "user@test.com",
            pass = "wrong_password"
        )

        advanceUntilIdle()

        val state = viewModel.authState.value

        assertTrue(state is AuthState.Error)
        assertEquals(error, (state as AuthState.Error).message)
    }

    @Test
    fun testLoginWithBlankFieldsReturnsErrorImmediately() {
        viewModel.login("", "")

        val state = viewModel.authState.value

        assertTrue(state is AuthState.Error)
        assertEquals(
            "Fields cannot be empty",
            (state as AuthState.Error).message
        )
    }

    @Test
    fun testLogoutSignsOutFirebaseAndClearsToken() {
        viewModel.logout()

        verify(firebaseAuth).signOut()
        verify(prefManager).saveAuthToken("")
    }

    @Test
    fun testResetAuthStateReturnsStateToIdle() = runTest {
        whenever(repository.login(any()))
            .thenReturn(Result.failure(Exception("Failure")))

        viewModel.login(
            identifier = "user@test.com",
            pass = "password"
        )

        advanceUntilIdle()

        assertTrue(viewModel.authState.value is AuthState.Error)

        viewModel.resetAuthState()

        assertEquals(
            AuthState.Idle,
            viewModel.authState.value
        )
    }
}