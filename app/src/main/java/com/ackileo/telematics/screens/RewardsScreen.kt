package com.ackileo.telematics.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data classes for the UI
data class Badge(val name: String, val icon: ImageVector, val isUnlocked: Boolean)
data class RewardHistory(val title: String, val date: String, val points: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen() {
    val badges = listOf(
        Badge("Eco-Driver", Icons.Default.Eco, true),
        Badge("Night Owl", Icons.Default.NightlightRound, true),
        Badge("Safety Pro", Icons.Default.VerifiedUser, true),
        Badge("Long Haul", Icons.Default.Public, false)
    )

    val history = listOf(
        RewardHistory("Weekly Safety Bonus", "Oct 24, 2023", "+250 pts"),
        RewardHistory("Fuel Voucher Redeemed", "Oct 20, 2023", "-1000 pts"),
        RewardHistory("Perfect Trip Bonus", "Oct 18, 2023", "+50 pts")
    )

    // Wrap in Surface to set a solid background color and prevent "Black Screen"
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            // Ensure scaffold doesn't have its own opaque background blocking the Surface
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Driver Rewards", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 1. Points & Ranking Header
                item { PointsHeaderSection() }

                // 2. Fuel Discount Eligibility Card
                item { FuelDiscountCard() }

                // 3. Achievement Badges Row
                item { SectionLabel("Achievements") }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(badges) { badge -> BadgeItem(badge) }
                    }
                }

                // 4. Reward History
                item { SectionLabel("Recent Activity") }
                items(history) { item -> RewardHistoryItem(item) }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun PointsHeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Total Reward Points", color = Color.White.copy(alpha = 0.8f))
            Text("4,850", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White)

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Gold Level", color = Color.White, fontWeight = FontWeight.Bold)
                Text("150 pts to Platinum", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
            LinearProgressIndicator(
                progress = { 0.85f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(8.dp)
                    .clip(CircleShape),
                color = Color(0xFF4CAF50),
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Button(
                onClick = { /* Redeem Logic */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("REDEEM REWARDS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FuelDiscountCard() {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            // Use surface color for better theme integration
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE8F5E9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Color(0xFF2E7D32))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Fuel Discount Eligible", fontWeight = FontWeight.Bold)
                Text("Save $0.15/gal at partner stations", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(16.dp),
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun BadgeItem(badge: Badge) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(
                    if (badge.isUnlocked) Color(0xFFFFD700).copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = badge.icon,
                contentDescription = null,
                tint = if (badge.isUnlocked) Color(0xFFDAA520) else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = badge.name,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun RewardHistoryItem(history: RewardHistory) {
    ListItem(
        headlineContent = { Text(history.title, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(history.date) },
        trailingContent = {
            Text(
                history.points,
                fontWeight = FontWeight.Bold,
                color = if (history.points.startsWith("+")) Color(0xFF2E7D32) else Color.Red
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RewardsScreenPreview() {
    MaterialTheme {
        RewardsScreen()
    }
}