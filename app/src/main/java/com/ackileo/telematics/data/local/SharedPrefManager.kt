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
    }

    /**
     * Save the Authentication Token
     */
    fun saveAuthToken(token: String) {
        prefs.edit { putString(KEY_AUTH_TOKEN, token) }
    }

}

//OLD CODE

