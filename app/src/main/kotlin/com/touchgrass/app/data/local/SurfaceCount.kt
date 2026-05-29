package com.touchgrass.app.data.local

/**
 * Projection for grouped queries: how many blocks per surface in a time window.
 * Used by the Stats screen's "top surfaces" breakdown (spec §3.1.F).
 */
data class SurfaceCount(
    val surface: String,
    val count: Int,
)
