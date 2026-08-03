

package com.ackileo.telematics.domain.model data class Driver(
    val id: String,
    val fullName: String,
    val nationalId: String,
    val licenseNumber: String,
    val licenseClass: String,
    val phoneNumber: String,
    val email: String,
    val profilePictureUrl: String? = null
)