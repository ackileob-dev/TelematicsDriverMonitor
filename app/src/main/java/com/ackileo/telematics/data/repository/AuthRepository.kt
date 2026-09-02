package com.ackileo.telematics.data.repository

import android.net.Uri
import com.ackileo.telematics.data.local.SharedPrefManager
import com.ackileo.telematics.data.local.TokenManager
import com.ackileo.telematics.data.remote.ApiResponse
import com.ackileo.telematics.data.remote.ApiService
import com.ackileo.telematics.data.remote.dto.LoginRequest
import com.ackileo.telematics.data.remote.dto.LoginResponse
import com.ackileo.telematics.data.remote.dto.RegisterRequest
import com.ackileo.telematics.utils.StorageManager
import com.ackileo.telematics.utils.UploadStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.io.IOException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

enum class DocumentType {
}

interface AuthRepository {
    suspend fun register(request: RegisterRequest): Result<Unit>
    suspend fun login(request: LoginRequest): Result<LoginResponse>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    fun logout()
    fun getCurrentUser(): FirebaseUser?
    suspend fun uploadDriverDocument(
        userId: String,
        uri: Uri,
        docType: DocumentType,
    ): Flow<UploadStatus>
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val apiService: ApiService,
    private val storageManager: StorageManager,
    private val tokenManager: TokenManager,
    private val prefManager: SharedPrefManager,
) : AuthRepository {

    override suspend fun register(request: RegisterRequest): Result<Unit> {
        return executeRegisterCall(
            requestEmail = request.email,
            requestName = request.fullName,
            response = apiService.register(request),
            fallbackMessage = "Registration failed"
        )
    }

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return executeAuthCall(
            requestEmail = request.email,
            requestName = request.email,
            response = apiService.login(request),
            fallbackMessage = "Login failed"
        )
    }

    private suspend fun executeRegisterCall(
        requestEmail: String,
        requestName: String,
        response: Response<ApiResponse<LoginResponse>>,
        fallbackMessage: String,
    ): Result<Unit> {
        return try {
            if (response.isSuccessful) {
                val body = response.body()
                val session = body?.data
                if (session != null) {
                    // Some environments auto-login after register.
                    persistSession(session, requestEmail, requestName)
                }
                Result.success(Unit)
            } else {
                if (response.code() == 401) {
                    tokenManager.clearAuthState()
                    prefManager.clearAll()
                }
                Result.failure(AuthBackendException(response.code(), extractErrorMessage(response, fallbackMessage)))
            }
        } catch (e: IOException) {
            Result.failure(AuthBackendException(null, "Network error. Please check your internet connection.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        tokenManager.clearAuthState()
        prefManager.clearAll()
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    override suspend fun uploadDriverDocument(
        userId: String,
        uri: Uri,
        docType: DocumentType,
    ): Flow<UploadStatus> {
        val fileName = "${docType.name.lowercase()}_$userId.jpg"
        return storageManager.uploadFile(uri, "drivers/$userId/documents", fileName)
    }

    private suspend fun executeAuthCall(
        requestEmail: String,
        requestName: String,
        response: Response<ApiResponse<LoginResponse>>,
        fallbackMessage: String,
    ): Result<LoginResponse> {
        return try {
            if (response.isSuccessful) {
                val body = response.body()
                val session = body?.data ?: throw AuthBackendException(
                    response.code(),
                    body?.message ?: "The server returned an empty auth response."
                )
                persistSession(session, requestEmail, requestName)
                Result.success(session)
            } else {
                if (response.code() == 401) {
                    tokenManager.clearAuthState()
                    prefManager.clearAll()
                }
                Result.failure(AuthBackendException(response.code(), extractErrorMessage(response, fallbackMessage)))
            }
        } catch (e: IOException) {
            Result.failure(AuthBackendException(null, "Network error. Please check your internet connection.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun persistSession(
        session: LoginResponse,
        email: String,
        fallbackName: String,
    ) {
        tokenManager.saveAccessToken(session.token)
        tokenManager.saveDriverId(session.driverId.orEmpty())
        prefManager.saveUserEmail(email)
        prefManager.saveUserName(session.fullName?.takeIf { it.isNotBlank() } ?: fallbackName)
    }

    private fun <T> extractErrorMessage(
        response: Response<ApiResponse<T>>,
        fallbackMessage: String,
    ): String {
        val bodyMessage = response.body()?.message?.takeIf { it.isNotBlank() }
        val errorMessage = response.body()?.error?.message?.takeIf { it.isNotBlank() }
        val parsedErrorMessage = parseErrorBodyMessage(response.errorBody()?.string())
        return when (response.code()) {
            400 -> bodyMessage ?: errorMessage ?: parsedErrorMessage ?: "Invalid request. Please check the form and try again."
            401 -> bodyMessage ?: errorMessage ?: parsedErrorMessage ?: "Unauthorized. Please sign in again."
            409 -> bodyMessage ?: errorMessage ?: parsedErrorMessage ?: "An account with these details already exists."
            500 -> bodyMessage ?: errorMessage ?: parsedErrorMessage ?: "Server error. Please try again later."
            else -> bodyMessage ?: errorMessage ?: parsedErrorMessage ?: fallbackMessage
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

private class AuthBackendException(
    val code: Int?,
    override val message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
