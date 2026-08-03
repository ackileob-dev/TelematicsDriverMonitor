package com.ackileo.telematics.data.remote.models


// --- Auth Models ---
data class RegisterRequest(
    val fullName: String,
    val email: String,
    val nationalId: String,
    val licenseNumber: String,
    val licenseClass: String,
    val phoneNumber: String,
    val password: String
)



data class AuthResponse(
    val token: String,
    val driverId: String,
    val fullName: String
)

// --- Trip Models ---
data class TripStartRequest(
    val vehicleId: String,
    val startTime: String
)

data class TripEndRequest(
    val tripId: String,
    val endTime: String,
    val distanceKm: Double,
    val averageSpeed: Double
)



// --- Driving Event Model ---
data class EventRequest(
    val tripId: String,
    val eventType: String,
    val timestamp: String,
    val latitude: Double,
    val longitude: Double,
    val severity: String
)