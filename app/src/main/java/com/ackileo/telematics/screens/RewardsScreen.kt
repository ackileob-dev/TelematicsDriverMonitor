package com.ackileo.telematics.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ackileo.telematics.data.remote.dto.AlertDto
import com.ackileo.telematics.data.remote.dto.RewardDto
import com.ackileo.telematics.data.remote.dto.SafetyScoreDto
import com.ackileo.telematics.ui.viewmodel.RewardsUiState
import com.ackileo.telematics.ui.viewmodel.RewardsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(viewModel: RewardsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        RewardsContent(
            uiState = uiState,
            onRetry = viewModel::retry,
            onAlertRead = viewModel::markAlertRead,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RewardsContent(
    uiState: RewardsUiState,
    onRetry: () -> Unit,
    onAlertRead: (AlertDto) -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Rewards, Safety & Alerts", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (!uiState.errorMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onRetry) { Text("Retry") }
                }
            }
            return@Scaffold
        }

        if (uiState.isEmpty) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No rewards, safety scores, or alerts available yet.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SafetySummaryCard(
                    latest = uiState.latestSafetyScore,
                    historyCount = uiState.safetyScoreHistory.size,
                )
            }

            item {
                SectionTitle("Safety Score History")
            }

            items(uiState.safetyScoreHistory) { item ->
                SafetyHistoryItem(item)
            }

            item {
                SectionTitle("Available Rewards")
            }

            if (uiState.availableRewards.isEmpty()) {
                item { EmptyLine("No available rewards") }
            } else {
                items(uiState.availableRewards) { reward ->
                    RewardItem(reward)
                }
            }

            item {
                SectionTitle("All Rewards")
            }

            items(uiState.rewards) { reward ->
                RewardItem(reward)
            }

            item {
                SectionTitle("Driver Alerts")
            }

            if (uiState.alerts.isEmpty()) {
                item { EmptyLine("No alerts") }
            } else {
                items(uiState.alerts) { alert ->
                    AlertItem(
                        alert = alert,
                        isUpdating = uiState.isUpdatingAlert,
                        onMarkRead = onAlertRead,
                    )
                }
            }
        }
    }
}

@Composable
private fun SafetySummaryCard(latest: SafetyScoreDto?, historyCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Latest Safety Score", fontWeight = FontWeight.Bold)
            Text(
                text = latest?.score?.toString() ?: "N/A",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
            )
            Text("History entries: $historyCount", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Smooth driving: ${latest?.smoothDrivingScore ?: "N/A"}")
            Text("Speeding: ${latest?.speedingScore ?: "N/A"}")
            Text("Focus: ${latest?.focusScore ?: "N/A"}")
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun SafetyHistoryItem(item: SafetyScoreDto) {
    ListItem(
        headlineContent = { Text("Score ${item.score}") },
        supportingContent = {
            Text("Computed: ${item.computedAt ?: "Unknown"}")
        },
        trailingContent = {
            Text(
                text = "HB ${item.harshBrakingCount ?: 0}  RA ${item.rapidAccelerationCount ?: 0}",
                fontSize = 11.sp
            )
        }
    )
}

@Composable
private fun RewardItem(reward: RewardDto) {
    val status = reward.status
        ?: when {
            reward.available == true -> "available"
            reward.isRedeemed == true -> "redeemed"
            else -> "unknown"
        }

    ListItem(
        headlineContent = { Text(reward.title) },
        supportingContent = {
            Text(reward.description ?: "No description")
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text("${reward.points} pts", fontWeight = FontWeight.Bold)
                Text(status)
            }
        }
    )
}

@Composable
private fun AlertItem(
    alert: AlertDto,
    isUpdating: Boolean,
    onMarkRead: (AlertDto) -> Unit,
) {
    val isRead = alert.isRead ?: alert.read
    val status = when (isRead) {
        true -> "read"
        false -> "unread"
        null -> "unknown"
    }

    ListItem(
        leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
        headlineContent = { Text(alert.message) },
        supportingContent = { Text(alert.timestamp ?: "No timestamp") },
        trailingContent = {
            if (isRead == false) {
                TextButton(onClick = { onMarkRead(alert) }, enabled = !isUpdating) {
                    Text(if (isUpdating) "..." else "Mark read")
                }
            } else {
                Text(
                    text = status,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    )
}

@Composable
private fun EmptyLine(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RewardsScreenPreview() {
    MaterialTheme {
        RewardsContent(
            uiState = RewardsUiState(
                rewards = listOf(),
                availableRewards = listOf(),
                safetyScoreHistory = listOf(),
                alerts = listOf(),
                isEmpty = true,
            ),
            onRetry = {},
            onAlertRead = {}
        )
    }
}