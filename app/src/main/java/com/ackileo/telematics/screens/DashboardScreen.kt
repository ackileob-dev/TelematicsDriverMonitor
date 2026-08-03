package com.ackileo.telematics.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ackileo.telematics.ui.viewmodel.DashboardUiState
import com.ackileo.telematics.ui.viewmodel.DashboardViewModel
import com.ackileo.telematics.utils.DrivingEvent

/**
 * 1. STATEFUL COMPOSABLE
 * This is the entry point called by your NavGraph.
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToTrips: () -> Unit = {},
    onNavigateToTracking: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Surface wraps the entire screen to prevent the "black screen" issue
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        DashboardContent(
            uiState = uiState,
            onStartTrip = { viewModel.startTrip() },
            onEndTrip = { viewModel.endTrip() }
        )
    }
}

/**
 * 2. STATELESS COMPOSABLE
 * Handles only the UI layout. Easier to test and preview.
 */
@SuppressLint("DefaultLocale")
@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onStartTrip: () -> Unit,
    onEndTrip: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent // Surface handles the background color
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Live Telematics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Speed Display Section
            SpeedDisplayCard(speed = uiState.currentSpeed)

            Spacer(modifier = Modifier.height(24.dp))

            // Metrics Row (Score & Distance)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricCard(
                    label = "Safety Score",
                    value = "${uiState.safetyScore}%",
                    color = getScoreColor(uiState.safetyScore),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Distance",
                    value = String.format("%.2f km", uiState.totalDistance),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Control Button
            TripButton(
                isActive = uiState.isTripActive,
                onClick = { if (uiState.isTripActive) onEndTrip() else onStartTrip() }
            )

            // Alert Banner (Only shows if there is a warning)
            if (uiState.activeAlert != DrivingEvent.NORMAL && uiState.activeAlert != DrivingEvent.IDLE) {
                AlertBanner(event = uiState.activeAlert)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer Status
            GPSStatusIndicator(isEnabled = uiState.isGpsEnabled)
        }
    }
}

/**
 * 3. HELPER UI COMPONENTS
 * Kept private to this file for organization.
 */

@Composable
private fun SpeedDisplayCard(speed: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${speed.toInt()}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "km/h",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
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
            containerColor = if (isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Icon(
            imageVector = if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = null
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (isActive) "END TRIP" else "START TRIP",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AlertBanner(event: DrivingEvent) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠️ ALERT: ${event.name.replace("_", " ")}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun GPSStatusIndicator(isEnabled: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = androidx.compose.foundation.shape.CircleShape
    ) {
        Text(
            text = if (isEnabled) "● GPS Connected" else "○ Searching for GPS...",
            color = if (isEnabled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MetricCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = color,
                fontWeight = FontWeight.ExtraBold
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
                    totalDistance = 12.45
                ),
                onStartTrip = {},
                onEndTrip = {}
            )
        }
    }
}