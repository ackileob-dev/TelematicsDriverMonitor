package com.ackileo.telematics.data.repository

import com.ackileo.telematics.data.local.SharedPrefManager
import com.ackileo.telematics.data.local.TokenManager
import com.ackileo.telematics.data.remote.ApiResponse
import com.ackileo.telematics.data.remote.ApiService
import com.ackileo.telematics.data.remote.dto.DriverDto
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

interface ProfileRepository {
    suspend fun getCurrentUser(): Result<DriverDto>
    fun logout()
}

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val prefManager: SharedPrefManager,
    private val firebaseAuth: FirebaseAuth,
) : ProfileRepository {

    /**
     * Fetches the authenticated user's profile from the backend.
     * Calls GET /api/auth/me endpoint with JWT bearer token in headers.
     */
    override suspend fun getCurrentUser(): Result<DriverDto> {
        return try {
            val response = apiService.getCurrentUser()

            if (response.isSuccessful) {
                val body = response.body()
                val driver = body?.data ?: throw ProfileBackendException(
                    response.code(),
                    body?.message ?: "The server returned an empty profile response."
                )
                Result.success(driver)
            } else {
                if (response.code() == 401) {
                    // Unauthorized - token is invalid or expired
                    tokenManager.clearAuthState()
                    prefManager.clearAll()
                }
                Result.failure(
                    ProfileBackendException(
                        response.code(),
                        extractErrorMessage(response)
                    )
                )
            }
        } catch (e: IOException) {
            Result.failure(
                ProfileBackendException(
                    null,
                    "Network error. Please check your internet connection.",
                    e
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clears all authentication state and cached data.
     * This prepares the app for logout:
     * 1. Removes JWT from encrypted storage (TokenManager)
     * 2. Clears all user profile data from shared preferences (SharedPrefManager)
     * 3. Signs out from Firebase
     *
     * Subsequent API requests will not have the old token because:
     * - JwtInterceptor only adds token if tokenManager.getAccessToken() returns non-null
     * - After clearAuthState(), getAccessToken() will return null
     */
    override fun logout() {
        // Clear JWT tokens from encrypted storage
        tokenManager.clearAuthState()

        // Clear user profile data from regular shared preferences
        prefManager.clearAll()

        // Sign out from Firebase if integrated
        try {
            firebaseAuth.signOut()
        } catch (_: Exception) {
            // Firebase sign out failed, but local state is already cleared
        }
    }

    private fun <T> extractErrorMessage(
        response: Response<ApiResponse<T>>,
    ): String {
        val bodyMessage = response.body()?.message?.takeIf { it.isNotBlank() }
        val errorMessage = response.body()?.error?.message?.takeIf { it.isNotBlank() }
        val parsedErrorMessage = parseErrorBodyMessage(response.errorBody()?.string())
        return when (response.code()) {
            400 -> bodyMessage ?: errorMessage ?: parsedErrorMessage ?: "Invalid request. Please check and try again."
            401 -> bodyMessage ?: errorMessage ?: parsedErrorMessage ?: "Unauthorized. Please sign in again."
            403 -> bodyMessage ?: errorMessage ?: parsedErrorMessage ?: "Access forbidden."
            404 -> bodyMessage ?: errorMessage ?: parsedErrorMessage ?: "Profile not found."
            500 -> bodyMessage ?: errorMessage ?: parsedErrorMessage ?: "Server error. Please try again later."
            else -> bodyMessage ?: errorMessage ?: parsedErrorMessage ?: "Failed to fetch profile"
        }
    }

    private fun parseErrorBodyMessage(rawBody: String?): String? {
        if (rawBody.isNullOrBlank()) return null
        return runCatching {
            val json = JSONObject(rawBody)
            val directMessage = json.optString("message").takeIf { it.isNotBlank() }
            if (directMessage != null) {
                directMessage
            } else {
                json.optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
            }
        }.getOrNull()
    }
}

private class ProfileBackendException(
    @Suppress("UNUSED_PARAMETER")
    code: Int?,
    override val message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

