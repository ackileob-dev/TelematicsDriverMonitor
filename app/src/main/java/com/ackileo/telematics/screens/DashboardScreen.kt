package com.ackileo.telematics.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ackileo.telematics.data.repository.DashboardData
import com.ackileo.telematics.ui.viewmodel.DashboardUiState
import com.ackileo.telematics.ui.viewmodel.DashboardViewModel
import com.ackileo.telematics.utils.DrivingEvent

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToTrips: () -> Unit = {},
    onNavigateToTracking: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        DashboardContent(
            uiState = uiState,
            onStartTrip = { viewModel.startTrip() },
            onEndTrip = { viewModel.endTrip() },
            onRetry = { viewModel.retryDashboardLoad() },
            onNavigateToTrips = onNavigateToTrips,
            onNavigateToTracking = onNavigateToTracking,
            onNavigateToProfile = onNavigateToProfile,
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onStartTrip: () -> Unit,
    onEndTrip: () -> Unit,
    onRetry: () -> Unit,
    onNavigateToTrips: () -> Unit,
    onNavigateToTracking: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    val dashboardData = uiState.dashboardData
    val driverName = dashboardData?.driver?.fullName?.takeIf { it.isNotBlank() } ?: "Driver"

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Welcome, $driverName",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                TextButton(onClick = onNavigateToTrips) { Text("Trips") }
                TextButton(onClick = onNavigateToTracking) { Text("Tracking") }
                TextButton(onClick = onNavigateToProfile) { Text("Profile") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!uiState.errorMessage.isNullOrBlank()) {
                DashboardStateCard(
                    title = "Unable to load dashboard",
                    message = uiState.errorMessage,
                    actionLabel = "Retry",
                    onAction = onRetry,
                    actionIcon = Icons.Default.Refresh,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.isEmpty && dashboardData != null) {
                DashboardStateCard(
                    title = "No recent dashboard activity",
                    message = "We found your profile, but there are no recent trips, alerts, rewards, or events yet.",
                    actionLabel = null,
                    onAction = null,
                    actionIcon = null,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            DashboardSection(title = "Driver Profile") {
                ProfileInfoCard(dashboardData)
            }

            Spacer(modifier = Modifier.height(16.dp))

            DashboardSection(title = "Safety") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    MetricCard(
                        label = "Current Safety Score",
                        value = "${uiState.safetyScore}%",
                        color = getScoreColor(uiState.safetyScore),
                        modifier = Modifier.weight(1f),
                    )
                    MetricCard(
                        label = "Trips",
                        value = dashboardData?.recentTrips?.size?.toString() ?: "0",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            DashboardSection(title = "Vehicle Information") {
                VehicleInfoCard(dashboardData)
            }

            Spacer(modifier = Modifier.height(16.dp))

            DashboardSection(title = "Recent Trips") {
                DashboardListCard(
                    emptyText = "No recent trips available",
                    items = dashboardData?.recentTrips.orEmpty().map {
                        "Trip #${it.id} • ${it.totalDistance} km • ${it.averageSpeed} km/h • score ${it.safetyScore}"
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            DashboardSection(title = "Recent Driving Events") {
                DashboardListCard(
                    emptyText = "No driving events available",
                    items = dashboardData?.recentDrivingEvents.orEmpty().map {
                        "${it.type} • ${it.timestamp}"
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            DashboardSection(title = "Rewards") {
                DashboardListCard(
                    emptyText = "No rewards available",
                    items = dashboardData?.rewards.orEmpty().map {
                        "${it.title} • ${it.points} points"
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            DashboardSection(title = "Alerts") {
                DashboardListCard(
                    emptyText = "No alerts available",
                    items = dashboardData?.alerts.orEmpty().map {
                        "${it.level ?: "info"} • ${it.message}"
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Live Telematics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(12.dp))

            SpeedDisplayCard(speed = uiState.currentSpeed)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MetricCard(
                    label = "Safety Score",
                    value = "${uiState.safetyScore}%",
                    color = getScoreColor(uiState.safetyScore),
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    label = "Distance",
                    value = String.format("%.2f km", uiState.totalDistance),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TripButton(
                isActive = uiState.isTripActive,
                onClick = { if (uiState.isTripActive) onEndTrip() else onStartTrip() },
            )

            if (uiState.activeAlert != DrivingEvent.NORMAL && uiState.activeAlert != DrivingEvent.IDLE) {
                AlertBanner(event = uiState.activeAlert)
            }

            Spacer(modifier = Modifier.weight(1f))

            GPSStatusIndicator(isEnabled = uiState.isGpsEnabled)
        }
    }
}

@Composable
private fun DashboardSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        content()
    }
}

@Composable
private fun DashboardStateCard(
    title: String,
    message: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    actionIcon: ImageVector?,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium)
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onAction) {
                    if (actionIcon != null) {
                        Icon(actionIcon, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
private fun DashboardListCard(
    emptyText: String,
    items: List<String>,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (items.isEmpty()) {
                Text(
                    emptyText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                items.take(5).forEach { item ->
                    Text(text = item, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(dashboardData: DashboardData?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dashboardData?.driver?.fullName ?: "No driver profile loaded",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = dashboardData?.driver?.email ?: "Email unavailable", style = MaterialTheme.typography.bodyMedium)
            Text(text = dashboardData?.driver?.phone ?: "Phone unavailable", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = dashboardData?.driver?.id ?: "Driver ID unavailable",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VehicleInfoCard(dashboardData: DashboardData?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            val vehicle = dashboardData?.vehicle
            val vehicleTitle = vehicle?.let {
                listOfNotNull(it.make?.takeIf(String::isNotBlank), it.model?.takeIf(String::isNotBlank)).joinToString(" ")
            }.orEmpty().ifBlank { "No vehicle linked" }

            Text(
                text = vehicleTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = vehicle?.plateNumber ?: "Plate number unavailable", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = vehicle?.id ?: "Vehicle ID unavailable",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SpeedDisplayCard(speed: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "${speed.toInt()}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "km/h",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun TripButton(isActive: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
    ) {
        Icon(
            imageVector = if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (isActive) "END TRIP" else "START TRIP",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun AlertBanner(event: DrivingEvent) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "⚠️ ALERT: ${event.name.replace("_", " ")}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
private fun GPSStatusIndicator(isEnabled: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = CircleShape,
    ) {
        Text(
            text = if (isEnabled) "● GPS Connected" else "○ Searching for GPS...",
            color = if (isEnabled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun MetricCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = color,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

private fun getScoreColor(score: Int): Color = when {
    score > 80 -> Color(0xFF4CAF50)
    score > 50 -> Color(0xFFFFC107)
    else -> Color(0xFFF44336)
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DashboardContent(
                uiState = DashboardUiState(
                    currentSpeed = 65f,
                    safetyScore = 85,
                    isGpsEnabled = true,
                    isTripActive = true,
                    totalDistance = 12.45,
                    isLoading = false,
                    errorMessage = null,
                    isEmpty = false,
                    dashboardData = null,
                ),
                onStartTrip = {},
                onEndTrip = {},
                onRetry = {},
                onNavigateToTrips = {},
                onNavigateToTracking = {},
                onNavigateToProfile = {},
            )
        }
    }
}