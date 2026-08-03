package com.ackileo.telematics.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ackileo.telematics.data.local.SharedPrefManager
import com.ackileo.telematics.data.remote.models.AuthResponse
import com.ackileo.telematics.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    private val repository: AuthRepository = mock()
    private val prefManager: SharedPrefManager = mock()

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(repository, prefManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(AuthState.Idle, viewModel.authState.value)
    }

    @Test
    fun `login success updates state to Success and saves token`() = runTest {
        // Arrange
        val mockToken = "test_token"
        val mockResponse = AuthResponse(
            token = mockToken,
            driverId = "driver_123", // Fixed: Removed TODO()
            fullName = "John Doe"    // Fixed: Removed TODO()
        )
        whenever(repository.login(any())).thenReturn(Result.success(mockResponse))

        // Act
        viewModel.login("user@test.com", "password")

        // Assert: Immediate state change check
        assertTrue("State should be Loading immediately after call",
            viewModel.authState.value is AuthState.Loading)

        // Process all pending coroutine actions
        advanceUntilIdle()

        // Assert: Verify side effects and final state (Fixed: Added missing assertions)
        verify(prefManager).saveAuthToken(mockToken)
        assertTrue("State should be Success after successful login",
            viewModel.authState.value is AuthState.Success)
    }

    @Test
    fun `login failure updates state to Error`() = runTest {
        // Arrange
        val errorMessage = "Unauthorized"
        whenever(repository.login(any()))
            .thenReturn(Result.failure(Exception(errorMessage)))

        // Act
        viewModel.login("user@test.com", "wrong_password")
        advanceUntilIdle()

        // Assert: Smart casting used for cleaner code
        val state = viewModel.authState.value
        assertTrue("State should be Error on failure", state is AuthState.Error)
        assertEquals(errorMessage, (state as? AuthState.Error)?.message)
    }

    @Test
    fun `resetAuthState sets state back to Idle`() = runTest {
        // Arrange: Drive the VM into an Error state
        whenever(repository.login(any()))
            .thenReturn(Result.failure(Exception("Generic Failure")))

        viewModel.login("test", "test")
        advanceUntilIdle()

        // Sanity check that we are actually in an error state
        assertTrue(viewModel.authState.value is AuthState.Error)

        // Act
        viewModel.resetAuthState()

        // Assert
        assertEquals(AuthState.Idle, viewModel.authState.value)
    }
}