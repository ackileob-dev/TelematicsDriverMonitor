package com.ackileo.telematics.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import android.content.SharedPreferences
import androidx.core.content.edit


/**
 * SharedPrefManager handles local persistence for user session and preferences.
 */
@Singleton
class SharedPrefManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREF_NAME = "telematics_driver_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
    }

    /**
     * Save the Authentication Token
     */
    fun saveAuthToken(token: String) {
        prefs.edit { putString(KEY_AUTH_TOKEN, token) }
    }

    fun saveUserName(name: String) {
        prefs.edit { putString(KEY_USER_NAME, name) }
    }

    fun saveUserEmail(email: String) {
        prefs.edit { putString(KEY_USER_EMAIL, email) }
    }

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun clearUserProfile() {
        prefs.edit {
            remove(KEY_USER_NAME)
            remove(KEY_USER_EMAIL)
        }
    }

    fun clearAuthToken() {
        prefs.edit { remove(KEY_AUTH_TOKEN) }
    }

    fun clearAll() {
        prefs.edit {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_USER_NAME)
            remove(KEY_USER_EMAIL)
        }
    }

}

//OLD CODE

