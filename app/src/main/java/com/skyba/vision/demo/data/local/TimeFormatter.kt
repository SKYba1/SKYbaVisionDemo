package com.skyba.vision.demo.data.local

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.skyba.vision.demo.R

/**
 * [Architecture] Utility object for time formatting.
 * Designed to work both within the Compose UI tree and in background/drawing threads.
 */
object TimeFormatter {

    /**
     * [UI Layer] Formats seconds into a human-readable string using Composable resources.
     * Best used for static text elements within the UI.
     */

    @Composable
    fun formatSeconds(totalSeconds: Int): String {

        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val h = stringResource(R.string.hours_abbreviation)
        val m = stringResource(R.string.minutes_abbreviation)
        val s = stringResource(R.string.seconds_abbreviation)

        return when {
            hours > 0 -> {
                // Якщо є хвилини — додаємо їх, якщо 0 — не пишемо нічого
                val minutesPart = if (minutes > 0) " ${minutes}${m}" else ""
                "${hours}${h}$minutesPart"
            }
            minutes > 0 -> {
                // Малюємо секунди тільки якщо вони не нульові
                val secondsPart = if (seconds > 0) " ${seconds}${s}" else ""
                "${minutes}${m}$secondsPart"
            }
            else -> "${seconds}${s}"
        }
    }

    /**
     * [Logic Layer] A "Raw" version of the formatter that requires an explicit Context.
     * Essential for chart builders and notification updates where @Composable is not available.
     *
     * @param isPopup If true, returns high-precision format (m + s).
     * If false, returns simplified format for chart axes.
     */
    fun formatSecondsRaw(
        context: Context,
        totalSeconds: Int,
        isPopup: Boolean = false // Додаємо цей прапорець
    ): String {
        val h = context.getString(R.string.hours_abbreviation)
        val m = context.getString(R.string.minutes_abbreviation)
        val s = context.getString(R.string.seconds_abbreviation)

        // Cap values for chart axis consistency (display max 1h)
        if (totalSeconds >= 3600) return "1$h"

        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        // Precision logic for interactive popups
        if (isPopup) {
            return when {
                minutes > 0 -> {
                    if (seconds > 0) "${minutes}$m ${seconds}$s" else "${minutes}$m"
                }
                else -> "${seconds}$s"
            }
        }

        // Clean logic for chart Y-axis (rounding to nearest minute)
        if (totalSeconds >= 60) {
            val roundedMinutes = Math.round(totalSeconds / 60f)
            return if (roundedMinutes >= 60) "1$h" else "${roundedMinutes}$m"
        }

        return "${totalSeconds}$s"
    }
}