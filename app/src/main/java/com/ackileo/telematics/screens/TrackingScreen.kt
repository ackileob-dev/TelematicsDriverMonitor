package com.ackileo.telematics.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ackileo.telematics.ui.theme.TelematicsTheme
import com.ackileo.telematics.ui.viewmodel.TrackingViewModel
import com.ackileo.telematics.utils.DrivingEvent
import java.text.SimpleDateFormat
import java.util.Locale

// Data class for recent driving events
data class DriveEvent(
    val title: String,
    val time: String,
    val icon: ImageVector,
    val color: Color,
)

@Composable
fun TrackingScreen(
    onBack: () -> Unit = {},
    viewModel: TrackingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onLocationPermissionResult(granted)
    }

    val events = uiState.recentEvents.map {
        DriveEvent(
            title = formatEventTitle(it.eventType),
            time = formatEventTime(it.timestampIso),
            icon = iconForEventType(it.eventType),
            color = colorForEventType(it.eventType),
        )
    }

    val riskStatus = when {
        uiState.activeEvent == DrivingEvent.PHONE_USAGE -> "Critical"
        uiState.activeEvent == DrivingEvent.HARSH_BRAKING || uiState.activeEvent == DrivingEvent.RAPID_ACCELERATION -> "Warning"
        uiState.activeEvent == DrivingEvent.SHARP_CORNERING -> "Warning"
        else -> "Safe"
    }

    // 1. Root Surface prevents the "Black Screen" issue
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // 2. Map Placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant), // Themed gray
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        "Google Maps View Placeholder",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }

            // 3. Risk Indicator Overlay
            RiskIndicatorOverlay(status = riskStatus)

            // 4. Telemetry Cards (Floating at top)
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 110.dp) // Adjusted for Risk Indicator
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TelemetryCard(
                        label = "Speed",
                        value = uiState.location.speedKmh.toInt().toString(),
                        unit = "km/h",
                        icon = Icons.Default.Speed,
                        Modifier.weight(1f)
                    )
                    TelemetryCard(
                        label = "Distance",
                        value = String.format(Locale.US, "%.1f", uiState.location.totalDistanceKm),
                        unit = "km",
                        icon = Icons.Default.Route,
                        Modifier.weight(1f)
                    )
                    TelemetryCard(
                        label = "Duration",
                        value = formatDuration(uiState.tripDurationSeconds),
                        unit = "",
                        icon = Icons.Default.Timer,
                        Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Max ${uiState.maxSpeedKmh.toInt()} km/h  •  GPS ±${if (uiState.location.accuracyMeters.isFinite()) uiState.location.accuracyMeters.toInt() else "-"}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!uiState.currentTripId.isNullOrBlank()) {
                    Text(
                        text = "Trip: ${uiState.currentTripId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 5. Bottom Control Panel
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Recent Driving Events",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Event List
                    Box(modifier = Modifier.height(130.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (events.isEmpty()) {
                                item {
                                    Text(
                                        text = "No detected events yet",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                            items(events) { event ->
                                EventItem(event)
                            }
                        }
                    }

                    if (!uiState.errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Start/Stop Button
                    Button(
                        onClick = {
                            if (!uiState.hasLocationPermission) {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            } else {
                                viewModel.toggleTracking()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isTracking) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !uiState.isLoading,
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = if (uiState.isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isTracking) "END TRIP" else "START TRIP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Back Button (Optional - for overlaying on maps)
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(top = 40.dp, start = 8.dp)
                    .align(Alignment.TopStart)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
            ) {

                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    }
}

private fun formatEventTitle(type: String): String {
    return type.replace("_", " ").split(" ")
        .joinToString(" ") { token -> token.replaceFirstChar { it.uppercaseChar() } }
}

private fun formatDuration(durationSeconds: Long): String {
    val hours = durationSeconds / 3600
    val minutes = (durationSeconds % 3600) / 60
    val seconds = durationSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

private fun formatEventTime(timestampIso: String): String {
    return runCatching {
        val parsers = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        )
        val parsed = parsers.firstNotNullOfOrNull { parser ->
            parser.timeZone = java.util.TimeZone.getTimeZone("UTC")
            parser.parse(timestampIso)
        }

        if (parsed != null) {
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(parsed)
        } else {
            timestampIso
        }
    }.getOrDefault(timestampIso)
}

private fun iconForEventType(type: String): ImageVector {
    return when (type) {
        "speeding" -> Icons.Default.Speed
        "harsh_braking" -> Icons.Default.Warning
        "harsh_acceleration" -> Icons.Default.KeyboardArrowUp
        "sharp_cornering" -> Icons.Default.TurnSharpLeft
        "phone_distraction" -> Icons.Default.PhoneAndroid
        else -> Icons.Default.Info
    }
}

private fun colorForEventType(type: String): Color {
    return when (type) {
        "phone_distraction" -> Color(0xFFE53935)
        "speeding" -> Color(0xFFFFB74D)
        "harsh_braking", "harsh_acceleration", "sharp_cornering" -> Color(0xFFE57373)
        else -> Color(0xFF81C784)
    }
}

@Composable
fun RiskIndicatorOverlay(status: String) {
    val backgroundColor by animateColorAsState(
        targetValue = when (status) {
            "Safe" -> Color(0xFF2E6C00)
            "Warning" -> Color(0xFFFFA500)
            else -> Color.Red
        }, label = "risk_color"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp, start = 60.dp, end = 60.dp) // Adjusted for back button
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = 0.9f))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Status: $status",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TelemetryCard(label: String, value: String, unit: String, icon: ImageVector, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(text = unit, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun EventItem(event: DriveEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(event.color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                event.icon,
                contentDescription = null,
                tint = event.color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                event.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                event.time,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TrackingScreenPreview() {
    TelematicsTheme {
        TrackingScreen()
    }
}