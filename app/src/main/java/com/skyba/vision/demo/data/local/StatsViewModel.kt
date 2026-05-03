package com.artems_apps.vision_pause.data.local

import android.app.Application
import android.content.Context
import android.text.format.DateFormat
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.ehsannarmani.compose_charts.models.Bars
import kotlinx.coroutines.flow.*
import java.util.*
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.skyba.vision.demo.data.local.db.AppDatabase
import com.skyba.vision.demo.data.local.db.StatsUiState
import com.skyba.vision.demo.data.local.db.TimerDao
import com.skyba.vision.demo.data.local.db.TimerSession
import com.skyba.vision.demo.data.repository.SettingsRepository
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars.Data


class StatsViewModel(
    private val dao: TimerDao,
    application: Application,
    private val repository: SettingsRepository
) : AndroidViewModel(application) {

    private val is24Hour = DateFormat.is24HourFormat(application)

    /*
     [Architecture] Reactive StateFlow exposed to the UI.
     Automatically re-processes data when the underlying database changes.
     'WhileSubscribed' ensures the flow is active only when the screen is visible.
     */

    val uiState: StateFlow<StatsUiState> = repository.allSessionsFlow
        .map { sessions ->
            processSessions(sessions, is24Hour)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatsUiState()
        )

    /*
      [Business Logic] Processes raw database sessions into a format compatible with the UI chart.
      Filters data for the current day and calculates hourly distribution.
     */
    private fun processSessions(sessions: List<TimerSession>, is24Hour: Boolean): StatsUiState {
        val today = Calendar.getInstance()
        val todaySessions = sessions.filter {
            TimeUtils.isSameDay(it.startTime, today.timeInMillis)
        }

        var workSec = 0
        var breakSec = 0
        var full = 0
        var interrupted = 0

        val hourlyData = Array(24) { DoubleArray(2) { 0.0 } }

        todaySessions.forEach { session ->
            val hour = TimeUtils.getHourFromTimestamp(session.startTime)
            if (hour in 0..23) {
                if (session.phaseType == "WORK") {
                    workSec += session.durationSeconds
                    hourlyData[hour][0] += session.durationSeconds.toDouble() / 60.0

                } else {
                    breakSec += session.durationSeconds
                    hourlyData[hour][1] += session.durationSeconds.toDouble() / 60.0


                    if (session.isCompleted) {
                        full++
                    } else {
                        interrupted++
                    }
                }
            }
        }

        val activeHours = hourlyData.indices.filter { hour ->
            hourlyData[hour][0] > 0.0 || hourlyData[hour][1] > 0.0
        }

        if (activeHours.isEmpty()) {
            return StatsUiState(
                isLoading = false,
                screenTimeTotal = workSec,
                breakTimeTotal = breakSec,
                fullSessions = full,
                interruptedSessions = interrupted
            )
        }

        /*
          [UX Optimization] Dynamically adjust the visible chart range.
          Ensures a minimum span of 5 hours for better visual balance on small datasets.
         */

        var startHour = activeHours.minOrNull()!!
        var endHour = activeHours.maxOrNull()!!
        val currentRange = endHour - startHour
        if (currentRange < 5) {
            val paddingNeeded = 5 - currentRange
            startHour = (startHour - paddingNeeded / 2).coerceAtLeast(0)
            endHour = (startHour + 5).coerceAtMost(23)
            if (endHour == 23) startHour = (23 - 5).coerceAtLeast(0)
        } else {
            startHour = (startHour - 1).coerceAtLeast(0)
            endHour = (endHour + 1).coerceAtMost(23)
        }

        // Auto-scale Y-axis based on the maximum value in the filtered dataset
        val highestPoint = (startHour..endHour).maxOf { hour ->
            maxOf(hourlyData[hour][0], hourlyData[hour][1])
        }
        val yAxisMax = if (highestPoint > 0) highestPoint * 1.2 else 10.0

        val workColor = Color(0xFFD67272)
        val breakColor = Color(0xFF106D4D)

        /*
         * [UI Mapping] Converts processed hourly data into 'Bars' objects for the Compose Charts library.
         */
        val bars = (startHour..endHour).map { hour ->
            val values = hourlyData[hour]
            val label = if (is24Hour) "$hour:00" else {
                val h = if (hour % 12 == 0) 12 else hour % 12
                val suffix = if (hour < 12) "AM" else "PM"
                "$h $suffix"
            }

            val barStyle = BarProperties(
                thickness = 20.dp,
                spacing = 8.dp,
                cornerRadius = Data.Radius.Rectangle(topLeft = 10.dp, topRight = 10.dp)
            )

            Bars(
                label = label,
                values = listOf(
                    Data(label = "Work", value = values[0],
                        color = SolidColor(workColor),
                        properties = barStyle),
                    Data(label = "Break", value = values[1],
                        color = SolidColor(breakColor),
                        properties = barStyle)
                )
            )
        }

        return StatsUiState(
            isLoading = false,
            screenTimeTotal = workSec,
            breakTimeTotal = breakSec,
            fullSessions = full,
            interruptedSessions = interrupted,
            chartData = bars,
            yAxisMax = yAxisMax
        )
    }


    /*
     [Dependency Injection] Simple Factory to provide required dependencies to the ViewModel.
     In a production environment, this would be replaced by Hilt or Koin.
     */
    class Factory(
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
                val repository = SettingsRepository(context)
                val database = AppDatabase.getDatabase(context)
                val application = context.applicationContext as Application

                @Suppress("UNCHECKED_CAST")
                return StatsViewModel(
                    dao = database.timerDao(),
                    application = application,
                    repository = repository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

object TimeUtils {
    fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun getHourFromTimestamp(timestamp: Long): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal.get(Calendar.HOUR_OF_DAY)
    }
}