package com.skyba.vision.demo.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * [Data Layer] Data Access Object for the 'timer_sessions' table.
 * Defines the database operations used to track and retrieve user productivity data.
 */
@Dao
interface TimerDao {

    /**
     * [Create] Inserts a new session into the database.
     * @return The row ID of the newly inserted session.
     */
    @Insert
    suspend fun insertSession(session: TimerSession): Long

    /**
     * [Read] Retrieves all sessions from a specific timestamp onwards.
     * Used by the Statistics screen to fetch data for the current day or week.
     * Returns a [Flow] to provide real-time updates when the database changes.
     */
    @Query("SELECT * FROM timer_sessions WHERE startTime >= :sinceTime ORDER BY startTime ASC")
    fun getSessionsSince(sinceTime: Long): Flow<List<TimerSession>>

    /**
     * [Read] Calculates the total accumulated focus time across all "WORK" sessions.
     * Returns a nullable Long Flow in case the table is empty.
     */
    @Query("SELECT SUM(durationSeconds) FROM timer_sessions WHERE phaseType = 'WORK'")
    fun getTotalWorkTime(): Flow<Long?>

    /**
     * [Delete] Wipes all session history.
     * Typically used for a "Reset Data" feature in settings.
     * @return The number of rows deleted.
     */
    @Query("DELETE FROM timer_sessions")
    suspend fun clearAll(): Int
}