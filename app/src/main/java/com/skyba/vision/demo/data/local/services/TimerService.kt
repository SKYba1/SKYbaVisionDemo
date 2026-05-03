package com.skyba.vision.demo.data.local.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import android.app.NotificationManager
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.skyba.vision.demo.MainActivity
import com.skyba.vision.demo.R
import com.skyba.vision.demo.data.local.PhaseSoundOption
import com.skyba.vision.demo.data.repository.SettingsRepository
import com.skyba.vision.demo.domain.usecase.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import com.skyba.vision.demo.data.local.db.TimerSession

class TimerService : Service() {

    /*
     [Architecture] NotificationLayouts allow dynamic button updates
      without recreating the notification channel.
     */
    private enum class NotificationLayout {
        LAYOUT_1, // Pause, Skip, Stop
        LAYOUT_2, // Pause, Restart, Stop
        LAYOUT_3  // Continue, Restart, Stop
    }

    private var currentLayout = NotificationLayout.LAYOUT_1
    private var currentPhase = TimerPhase.IDLE

    // Texts
    private lateinit var notificationUsageTime: String
    private lateinit var notificationBreakTime: String
    private lateinit var notificationStarted: String
    private lateinit var notificationTimerPaused: String
    private lateinit var notificationRemaining: String

    // Buttons
    private lateinit var continueButton: String
    private lateinit var pauseButton: String
    private lateinit var stopButton: String
    private lateinit var restartButton: String
    private lateinit var skipButton: String

    private val timer = Timer()
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var repository: SettingsRepository
    private lateinit var prefs: SharedPreferences
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)


    companion object {
        val timerLiveData = MutableLiveData<String>()
        val phaseLiveData = MutableLiveData<TimerPhase>().apply { value = TimerPhase.IDLE }
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("timer_prefs", MODE_PRIVATE)
        repository = SettingsRepository(applicationContext)
        loadNotificationTextsAndButtons()
    }

    private fun loadNotificationTextsAndButtons() {
        notificationUsageTime = prefs.getString("status_bar_usage_time", getString(R.string.status_bar_usage_time))!!
        notificationBreakTime = prefs.getString("status_bar_break_time", getString(R.string.status_bar_break_time))!!
        notificationStarted = prefs.getString("status_bar_default", getString(R.string.status_bar_default))!!
        notificationTimerPaused = prefs.getString("timer_paused", getString(R.string.timer_paused))!!
        notificationRemaining = prefs.getString("notification_remaining", getString(R.string.notification_remaining))!!

        continueButton = prefs.getString("continue_button", getString(R.string.continue_button))!!
        pauseButton = prefs.getString("pause_button", getString(R.string.pause_button))!!
        restartButton = prefs.getString("restart_button", getString(R.string.restart_button))!!
        stopButton = prefs.getString("stop_button", getString(R.string.stop_button))!!
        skipButton = prefs.getString("skip_button", getString(R.string.skip_button))!!
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            TimerAction.START.name -> {
                if (timer.isRunning()) return START_STICKY
                serviceScope.launch {
                    val config = repository.loadTimerConfig()
                    val initialTime = "%02d:00".format(config.work)
                    startForeground(NOTIFICATION_ID, buildNotification(initialTime))

                    startTimer(config.work * 60, config.breakTime * 60)
                    updateLayoutState()
                }
            }
            TimerAction.SKIP.name -> {

                val isInterruptedBreak = (currentPhase == TimerPhase.BREAK)
                saveCurrentSession(isCompleted = !isInterruptedBreak, manualStop = true)

                timer.skipPhase()
                updateLayoutState()
            }
            TimerAction.RESTART.name -> {
                serviceScope.launch {
                    val isInterruptedBreak = (currentPhase == TimerPhase.BREAK)

                    saveCurrentSession(isCompleted = !isInterruptedBreak, manualStop = true)

                    val config = repository.loadTimerConfig()
                    timer.configure(config)
                    timer.restart()
                    updateLayoutState()
                }
            }
            TimerAction.PAUSE.name -> pause()
            TimerAction.RESUME.name -> resume()
            TimerAction.STOP.name -> stop()
        }
        return START_STICKY
    }

    private fun startTimer(workSeconds: Int, brkSeconds: Int) {
        timer.configure(workSeconds, brkSeconds)

        timer.start(
            onPhaseChanged = { isWork ->
                handlePhaseChange(isWork)
                updateLayoutState()
            },
            onTick = ::onTick,
            onCycle = {

                saveCurrentSession(isCompleted = true)
            }
        )
    }

    private fun onTick(formattedTime: String) {
        timerLiveData.postValue(formattedTime)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(formattedTime))
    }

    private fun handlePhaseChange(isWorkPhase: Boolean) {
        currentPhase = if (isWorkPhase) TimerPhase.WORK else TimerPhase.BREAK
        phaseLiveData.postValue(currentPhase)

        val title = if (isWorkPhase) notificationUsageTime else notificationBreakTime
        showPhaseChangeNotification(title)
        updateLayoutState()
    }

    private fun updateLayoutState() {
        val isSkipEnabled = prefs.getBoolean("skip_enabled", true)
        currentLayout = when {
            currentPhase == TimerPhase.PAUSE -> NotificationLayout.LAYOUT_3
            !isSkipEnabled -> NotificationLayout.LAYOUT_2
            else -> NotificationLayout.LAYOUT_1
        }
    }

    /*
     [Data Flow] Saves timer data to the local database (Room).
     Calculates the difference between total phase time and remaining time
     to handle manual interruptions correctly.
     */

    private fun saveCurrentSession(isCompleted: Boolean, manualStop: Boolean = false) {
        val totalMs = timer.getTotalPhaseMillis()
        val remainingMs = timer.getRemainingMillis()

        val durationMs = if (manualStop) (totalMs - remainingMs) else totalMs
        val durationSeconds = (durationMs / 1000).toInt()

        if (durationSeconds < 2) return

        val phaseType = when (currentPhase) {
            TimerPhase.WORK -> "WORK"
            TimerPhase.BREAK -> "BREAK"
            else -> if (timer.isWorkPhase()) "WORK" else "BREAK"
        }

        serviceScope.launch {
            repository.saveSession(
                TimerSession(
                    startTime = System.currentTimeMillis() - durationMs,
                    durationSeconds = durationSeconds,
                    phaseType = phaseType,
                    isCompleted = isCompleted
                )
            )
        }
    }

    /*
     [UI/UX] Updates the persistent notification with a progress bar.
     Uses 'setSilent(true)' to avoid annoying beep sounds every second.
     */

    private fun buildNotification(time: String): Notification {

        var totalMs = timer.getTotalPhaseMillis()
        val totalSec = (totalMs / 1000).toInt()
        val remainingMs = timer.getRemainingMillis()

        val progressSec = ((totalMs - remainingMs) / 1000).toInt().coerceIn(0, totalSec)

        if (totalMs <= 0L) {
            val config = timer.getCurrentConfig()
            totalMs = if (timer.isWorkPhase()) config.first * 60 * 1000L
            else config.second * 60 * 1000L
        }

        val progress = (totalMs - remainingMs).coerceIn(0, totalMs).toInt()

        val title = when (currentPhase) {
            TimerPhase.WORK -> notificationUsageTime
            TimerPhase.BREAK -> notificationBreakTime
            TimerPhase.PAUSE -> notificationTimerPaused
            else -> notificationStarted
        }

        val pauseStatus = if (currentPhase == TimerPhase.PAUSE) {
            val phaseName = if (timer.isWorkPhase()) notificationUsageTime else notificationBreakTime
            " ($phaseName)"
        } else ""

        val builder = NotificationCompat.Builder(this, "timer_channel")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText("$notificationRemaining $time$pauseStatus")
            .setProgress(totalSec, progressSec, false)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(createContentIntent())

        when (currentLayout) {
            NotificationLayout.LAYOUT_1 -> {
                builder.addAction(0, pauseButton, createPI(TimerAction.PAUSE))
                builder.addAction(0, skipButton, createPI(TimerAction.SKIP))
                builder.addAction(0, stopButton, createPI(TimerAction.STOP))
            }
            NotificationLayout.LAYOUT_2 -> {
                builder.addAction(0, pauseButton, createPI(TimerAction.PAUSE))
                builder.addAction(0, restartButton, createPI(TimerAction.RESTART))
                builder.addAction(0, stopButton, createPI(TimerAction.STOP))
            }
            NotificationLayout.LAYOUT_3 -> {
                builder.addAction(0, continueButton, createPI(TimerAction.RESUME))
                builder.addAction(0, restartButton, createPI(TimerAction.RESTART))
                builder.addAction(0, stopButton, createPI(TimerAction.STOP))
            }
        }
        return builder.build()
    }


    private fun createPI(action: TimerAction): PendingIntent {
        val intent = Intent(this, TimerService::class.java).apply {
            this.action = action.name
        }
        return PendingIntent.getService(
            this,
            action.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun pause() {
        timer.pause()
        currentPhase = TimerPhase.PAUSE
        phaseLiveData.postValue(TimerPhase.PAUSE)
        updateLayoutState()
        onTick(timerLiveData.value ?: "00:00")
    }

    private fun resume() {
        timer.resume()
        currentPhase = if (timer.isWorkPhase()) TimerPhase.WORK else TimerPhase.BREAK
        phaseLiveData.postValue(currentPhase)
        updateLayoutState()
        onTick(timerLiveData.value ?: "00:00")
    }

    private fun stop() {

        val isInterruptedBreak = (currentPhase == TimerPhase.BREAK)
        saveCurrentSession(isCompleted = !isInterruptedBreak, manualStop = true)

        timer.stop()

        timerLiveData.postValue("00:00")
        phaseLiveData.postValue(TimerPhase.IDLE)
        mediaPlayer?.release()
        mediaPlayer = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createContentIntent() = PendingIntent.getActivity(
        this, 0, Intent(this, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun showPhaseChangeNotification(title: String) {
        val notification = NotificationCompat.Builder(this, "phase_change_channel")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(2, notification)
        playPhaseChangeSound()
        Handler(Looper.getMainLooper()).postDelayed({ (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(2) }, 5000)
    }

    private fun playPhaseChangeSound() {
        val selectedName = prefs.getString("phaseSoundOption", PhaseSoundOption.SOUND_1.name)
        val option = try { PhaseSoundOption.valueOf(selectedName!!) } catch (e: Exception) { PhaseSoundOption.SOUND_1 }
        if (option == PhaseSoundOption.NONE) return
        val resId = resources.getIdentifier(option.fileName, "raw", packageName)
        if (resId != 0) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, resId).apply { start() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        mediaPlayer?.release()
    }

    override fun onBind(intent: Intent?) = null
    enum class TimerAction { START, PAUSE, RESUME, STOP, RESTART, SKIP }
    enum class TimerPhase { IDLE, WORK, BREAK, PAUSE }
}
