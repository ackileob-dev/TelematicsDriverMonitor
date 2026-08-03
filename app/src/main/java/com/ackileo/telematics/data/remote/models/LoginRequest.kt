package com.ackileo.telematics.data.remote.models

import com.google.gson.annotations.SerializedName

/**
 * Request model for Authentication.
 * Note: [email] is used as the identifier for both Email or License Number
 * depending on your backend implementation.
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)