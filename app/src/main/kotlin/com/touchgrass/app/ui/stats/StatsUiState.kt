package com.touchgrass.app.ui.stats

import com.touchgrass.app.data.local.SurfaceCount

data class StatsUiState(
    val today: Int = 0,
    val thisWeek: Int = 0,
    val allTime: Int = 0,
    val topSurfacesThisWeek: List<SurfaceCount> = emptyList(),
    val loading: Boolean = true,
)
