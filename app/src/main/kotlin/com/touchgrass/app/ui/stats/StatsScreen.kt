package com.touchgrass.app.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touchgrass.app.data.local.SurfaceCount
import com.touchgrass.app.ui.theme.TouchgrassTheme

/**
 * Stats screen (spec §3.1.F).
 *
 * No streaks, no badges, no gamification in V1. Just honest counts and an estimate of time saved.
 */
@Composable
fun StatsRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: StatsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StatsScreen(state = state, onNavigateBack = onNavigateBack, modifier = modifier)
}

@Composable
fun StatsScreen(
    state: StatsUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Stats") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Saves.",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text =
                    "Every reel and short Touchgrass stopped for you. " +
                        "On-device, never sent anywhere.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            CountCard(
                label = "Today",
                value = state.today,
                estimateMinutesSaved = state.today * MINUTES_PER_BLOCK,
            )
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallCountCard(label = "This week", value = state.thisWeek, modifier = Modifier.weight(1f))
                SmallCountCard(label = "All time", value = state.allTime, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))

            if (state.topSurfacesThisWeek.isNotEmpty()) {
                Text(
                    text = "Top this week",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(8.dp))
                state.topSurfacesThisWeek.forEach { surface ->
                    SurfaceRow(surface)
                    Spacer(Modifier.height(4.dp))
                }
            } else if (!state.loading) {
                Text(
                    text =
                        "Nothing yet. That's either great or you haven't opened a reel — " +
                            "either way, well played.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CountCard(
    label: String,
    value: Int,
    estimateMinutesSaved: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value.toString(),
                fontSize = 56.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (estimateMinutesSaved > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "≈ ${formatMinutes(estimateMinutesSaved)} saved",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SmallCountCard(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SurfaceRow(surface: SurfaceCount) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = humanizeSurface(surface.surface),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = surface.count.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private fun humanizeSurface(surface: String): String =
    when (surface) {
        "youtube-shorts" -> "YouTube Shorts"
        "instagram-reels-tab" -> "Instagram Reels (tab)"
        "instagram-reels-feed" -> "Instagram Reels (feed)"
        "instagram-reels-explore" -> "Instagram Reels (Explore)"
        "instagram-reels-dm" -> "Instagram Reels (DM)"
        "tiktok-foryou" -> "TikTok For You"
        "tiktok-following" -> "TikTok Following"
        "facebook-reels-viewer" -> "Facebook Reels"
        "facebook-reels-tab" -> "Facebook Reels (tab)"
        "snapchat-spotlight" -> "Snapchat Spotlight"
        "chrome-youtube-shorts" -> "Chrome: YouTube Shorts"
        "samsung-internet-youtube-shorts" -> "Samsung Internet: YouTube Shorts"
        else -> surface
    }

private fun formatMinutes(minutes: Int): String =
    when {
        minutes < 60 -> "$minutes min"
        minutes < 60 * 24 -> "${minutes / 60}h ${minutes % 60}m"
        else -> "${minutes / (60 * 24)}d ${(minutes / 60) % 24}h"
    }

/**
 * Conservative estimate: every block prevents ~4 minutes of doomscroll on average. Calibrate
 * against real user data post-launch.
 */
private const val MINUTES_PER_BLOCK = 4

@Preview(showBackground = true, name = "Stats — empty")
@Composable
private fun StatsEmptyPreview() {
    TouchgrassTheme(darkTheme = false) {
        StatsScreen(StatsUiState(loading = false), onNavigateBack = {})
    }
}

@Preview(showBackground = true, name = "Stats — populated")
@Composable
private fun StatsPopulatedPreview() {
    TouchgrassTheme(darkTheme = false) {
        StatsScreen(
            StatsUiState(
                today = 23,
                thisWeek = 187,
                allTime = 1042,
                topSurfacesThisWeek =
                    listOf(
                        SurfaceCount("instagram-reels-tab", 67),
                        SurfaceCount("tiktok-foryou", 54),
                        SurfaceCount("youtube-shorts", 38),
                        SurfaceCount("snapchat-spotlight", 18),
                        SurfaceCount("facebook-reels-viewer", 10),
                    ),
                loading = false,
            ),
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Stats — populated (dark)")
@Composable
private fun StatsPopulatedDarkPreview() {
    TouchgrassTheme(darkTheme = true) {
        StatsScreen(
            StatsUiState(
                today = 23,
                thisWeek = 187,
                allTime = 1042,
                topSurfacesThisWeek =
                    listOf(
                        SurfaceCount("instagram-reels-tab", 67),
                        SurfaceCount("tiktok-foryou", 54),
                    ),
                loading = false,
            ),
            onNavigateBack = {},
        )
    }
}
