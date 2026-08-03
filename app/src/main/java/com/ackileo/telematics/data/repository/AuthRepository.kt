package com.ackileo.telematics.data.repository
import android.net.Uri
import com.ackileo.telematics.data.remote.ApiService
import com.ackileo.telematics.data.remote.models.AuthResponse
import com.ackileo.telematics.data.remote.models.LoginRequest
import com.ackileo.telematics.data.remote.models.RegisterRequest
import com.ackileo.telematics.utils.StorageManager
import com.ackileo.telematics.utils.UploadStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enum for Driver Documents
 */
enum class DocumentType {
}

/**
 * Interface definition for Auth Operations
 */
interface AuthRepository {
    suspend fun register(request: RegisterRequest): Result<AuthResponse>
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    fun logout()
    fun getCurrentUser(): FirebaseUser?
    suspend fun uploadDriverDocument(
        userId: String,
        uri: Uri,
        docType: DocumentType,
    ): Flow<UploadStatus>
}

/**
 * Implementation that coordinates Firebase Auth, Firebase Storage, and Retrofit API
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val apiService: ApiService, // CORRECT TYPE (uses the import at the top)
    private val storageManager: StorageManager,
) : AuthRepository {

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            // 1. Register in Firebase Auth
            firebaseAuth.createUserWithEmailAndPassword(request.email, request.password).await()
            // 2. Sync with Backend API
            val response = apiService.register(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            // 1. Sign in to Firebase
            firebaseAuth.signInWithEmailAndPassword(request.email, request.password).await()
            // 2. Get session from Backend API
            val response = apiService.login(request)
            Result.success(response)
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
}