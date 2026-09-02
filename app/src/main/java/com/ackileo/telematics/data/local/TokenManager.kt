package com.ackileo.telematics.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface SessionStateStore {
    fun hasAccessToken(): Boolean
}

/**
 * TokenManager handles secure storage and retrieval of JWT tokens and driver information.
 * Uses EncryptedSharedPreferences for encrypted token storage.
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context,
) : SessionStateStore {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val PREFS_NAME = "telematics_jwt_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_DRIVER_ID = "driver_id"
        private const val KEY_ACTIVE_TRIP_ID = "active_trip_id"
        private const val KEY_ACTIVE_TRIP_STARTED_AT = "active_trip_started_at"
    }

    /**
     * Save the access token
     */
    fun saveAccessToken(token: String) {
        encryptedPrefs.edit().putString(KEY_ACCESS_TOKEN, token.trim()).apply()
    }

    /**
     * Retrieve the access token
     */
    fun getAccessToken(): String? {
        val token = encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
        return token?.takeIf { it.isNotBlank() }
    }

    /**
     * Save the refresh token
     */
    fun saveRefreshToken(token: String) {
        encryptedPrefs.edit().putString(KEY_REFRESH_TOKEN, token.trim()).apply()
    }

    /**
     * Retrieve the refresh token
     */
    fun getRefreshToken(): String? {
        val token = encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
        return token?.takeIf { it.isNotBlank() }
    }

    /**
     * Save the driver ID
     */
    fun saveDriverId(driverId: String) {
        encryptedPrefs.edit().putString(KEY_DRIVER_ID, driverId.trim()).apply()
    }

    /**
     * Retrieve the driver ID
     */
    fun getDriverId(): String? {
        val id = encryptedPrefs.getString(KEY_DRIVER_ID, null)
        return id?.takeIf { it.isNotBlank() }
    }

    /**
     * Check if an access token exists
     */
    override fun hasAccessToken(): Boolean {
        return getAccessToken() != null
    }

    fun saveActiveTrip(tripId: String, startedAtMillis: Long) {
        encryptedPrefs.edit()
            .putString(KEY_ACTIVE_TRIP_ID, tripId.trim())
            .putLong(KEY_ACTIVE_TRIP_STARTED_AT, startedAtMillis)
            .apply()
    }

    fun getActiveTripId(): String? {
        val id = encryptedPrefs.getString(KEY_ACTIVE_TRIP_ID, null)
        return id?.takeIf { it.isNotBlank() }
    }

    fun getActiveTripStartedAtMillis(): Long? {
        if (!encryptedPrefs.contains(KEY_ACTIVE_TRIP_STARTED_AT)) return null
        return encryptedPrefs.getLong(KEY_ACTIVE_TRIP_STARTED_AT, 0L).takeIf { it > 0L }
    }

    fun clearActiveTrip() {
        encryptedPrefs.edit()
            .remove(KEY_ACTIVE_TRIP_ID)
            .remove(KEY_ACTIVE_TRIP_STARTED_AT)
            .apply()
    }

    /**
     * Clear all tokens and driver ID
     */
    fun clearTokens() {
        encryptedPrefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_DRIVER_ID)
            .remove(KEY_ACTIVE_TRIP_ID)
            .remove(KEY_ACTIVE_TRIP_STARTED_AT)
            .apply()
    }

    /**
     * Unified auth-state clear entrypoint used by logout and 401 handling.
     */
    fun clearAuthState() {
        clearTokens()
    }
}
