package com.skyba.vision.demo.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.skyba.vision.demo.data.local.db.AppDatabase
import com.skyba.vision.demo.data.local.db.TimerSession
import com.skyba.vision.demo.domain.model.TimerConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val HAS_SEEN_STABILITY_ADVICE = booleanPreferencesKey("has_seen_stability_advice")
        val WORK_DURATION = intPreferencesKey("work_duration")
        val BREAK_DURATION = intPreferencesKey("break_duration")

        const val DEFAULT_WORK = 20
        const val DEFAULT_BREAK = 5
    }


    val hasSeenStabilityAdviceFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[HAS_SEEN_STABILITY_ADVICE] ?: false }

    suspend fun markStabilityAdviceAsSeen() {
        context.dataStore.edit { it[HAS_SEEN_STABILITY_ADVICE] = true }
    }

    val timerConfigFlow: Flow<TimerConfig> = context.dataStore.data
        .map { prefs ->
            TimerConfig(
                work = prefs[WORK_DURATION] ?: DEFAULT_WORK,
                breakTime = prefs[BREAK_DURATION] ?: DEFAULT_BREAK
            )
        }

    suspend fun loadTimerConfig(): TimerConfig {
        return timerConfigFlow.first()
    }

    suspend fun saveWorkDuration(value: Int) {
        context.dataStore.edit { it[WORK_DURATION] = value }
    }

    suspend fun saveBreakDuration(value: Int) {
        context.dataStore.edit { it[BREAK_DURATION] = value }
    }

    // ------------------- Database -------------------

    private val database = AppDatabase.getDatabase(context)
    private val timerDao = database.timerDao()

    val allSessionsFlow: Flow<List<TimerSession>> = timerDao.getSessionsSince(0)

    // --- Room ---

    suspend fun saveSession(session: TimerSession) {
        timerDao.insertSession(session)
    }

    fun getSessionsSince(fromTime: Long): Flow<List<TimerSession>> {
        return timerDao.getSessionsSince(fromTime)
    }
}