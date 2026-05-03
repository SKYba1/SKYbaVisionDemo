package com.skyba.vision.demo.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * [Data Layer] Represents a single focus or rest session in the database.
 * This entity is the source of truth for the statistics screen.
 */
@Entity(tableName = "timer_sessions")
data class TimerSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,       //Unix timestamp when the session started. Used for grouping data by days/hours.
    val durationSeconds: Int,  //Actual duration in seconds. Captured as the difference between total and remaining time.
    val phaseType: String,     // Type of the phase: "WORK" or "BREAK". Used to colorize chart bars.
    val isCompleted: Boolean   // True if the timer finished naturally, false if interrupted by the user.
)