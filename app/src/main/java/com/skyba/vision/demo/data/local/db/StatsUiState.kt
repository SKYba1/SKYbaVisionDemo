package com.skyba.vision.demo.data.local.db

import ir.ehsannarmani.compose_charts.models.Bars

/**
 * [UI Model] Represents the complete state of the Statistics screen.
 *
 * This immutable data class is used by the UI layer to render all components
 * simultaneously, ensuring that the screen always reflects a consistent state
 * of the underlying data.
 */
data class StatsUiState(
    val isLoading: Boolean = true,
    val screenTimeTotal: Int = 0,
    val breakTimeTotal: Int = 0,
    val fullSessions: Int = 0,
    val interruptedSessions: Int = 0,
    val chartData: List<Bars> = emptyList(),
    val yAxisMax: Double = 10.0
)

