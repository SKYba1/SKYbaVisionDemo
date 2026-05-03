package com.skyba.vision.demo.domain.usecase
import android.os.CountDownTimer
import com.skyba.vision.demo.domain.model.TimerConfig


/**
 * [Architecture] Core timer engine based on Android's CountDownTimer.
 * Handles switching between Work and Break phases and tracks state.
 */

class Timer {
    private var countDownTimer: CountDownTimer? = null

    private var isRunning = false
    private var isPaused = false
    private var isWorkPhase = true

    // Duration is kept in Milliseconds for system precision
    private var currentPhaseDurationMillis: Long = 0L
    var remainingTimeMillis: Long = 0L
        private set

    private var workDurationMillis: Long = 0L
    private var breakDurationMillis: Long = 0L

    // Callbacks to communicate with TimerService
    private var currentPhaseChangedCallback: ((Boolean) -> Unit)? = null
    private var currentTickCallback: ((String) -> Unit)? = null
    private var currentCycleCallback: (() -> Unit)? = null

    // ---------- CONFIG ----------

    /**
     * Converts minutes from configuration into milliseconds once.
     */

    fun configure(config: TimerConfig) {
        // Конвертуємо хвилини в мс один раз ТУТ
        this.workDurationMillis = config.work * 60 * 1000L
        this.breakDurationMillis = config.breakTime * 60 * 1000L
    }

    fun configure(workSec: Int, breakSec: Int) {
        this.workDurationMillis = workSec * 1000L
        this.breakDurationMillis = breakSec * 1000L
    }

    // ---------- START / RESUME ----------

    /**
     * @param onPhaseChanged Link to the UI/Service action when phase toggles.
     */

    fun start(
        onPhaseChanged: (Boolean) -> Unit,
        onTick: (String) -> Unit,
        onCycle: () -> Unit
    ) {
        currentPhaseChangedCallback = onPhaseChanged
        currentTickCallback = onTick
        currentCycleCallback = onCycle

        if (!isRunning) {
            isRunning = true
            isPaused = false
            isWorkPhase = true
            remainingTimeMillis = workDurationMillis
            currentPhaseDurationMillis = workDurationMillis
        } else if (isPaused) {
            isPaused = false
        } else {
            return
        }

        runTimer()
    }

    // ---------- CORE TIMER ----------

    /**
     * [Logic] Manages CountDownTimer lifecycle.
     * Handles transitions between phases and prevents time reset during pause.
     */

    private fun runTimer() {
        countDownTimer?.cancel()

        // If not paused and time is empty, initialize full duration for a new phase
        if (!isPaused && remainingTimeMillis <= 0L) {
            remainingTimeMillis = if (isWorkPhase) workDurationMillis else breakDurationMillis
            currentPhaseDurationMillis = remainingTimeMillis
        }

        currentPhaseChangedCallback?.invoke(isWorkPhase)

        countDownTimer = object : CountDownTimer(remainingTimeMillis, 1000L) {
            override fun onTick(ms: Long) {
                remainingTimeMillis = ms

                val totalSeconds = ((ms + 500) / 1000).toInt()

                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                currentTickCallback?.invoke("%02d:%02d".format(minutes, seconds))
            }

            override fun onFinish() {
                remainingTimeMillis = 0L
                isPaused = false
                isWorkPhase = !isWorkPhase
                currentCycleCallback?.invoke()
                runTimer()
            }
        }.start()
    }

    // ---------- CONTROL ----------

    fun pause() {
        if (!isRunning || isPaused) return
        isPaused = true
        countDownTimer?.cancel()
    }

    fun resume() {
        if (isRunning && isPaused) {

            runTimer()
            isPaused = false
        }
    }

    fun stop() {
        countDownTimer?.cancel()
        countDownTimer = null
        isRunning = false
        isPaused = false
        isWorkPhase = true
        remainingTimeMillis = 0L
    }

    // ---------- SKIP ----------

    fun skipPhase() {
        if (!isRunning) return
        countDownTimer?.cancel()

        isPaused = false
        isWorkPhase = !isWorkPhase

        // Reset time to 0 so runTimer loads the duration for the next phase
        remainingTimeMillis = 0L
        currentPhaseDurationMillis = 0L

        currentCycleCallback?.invoke()
        runTimer()
    }

    fun restart() {
        countDownTimer?.cancel()
        isRunning = false
        isPaused = false
        isWorkPhase = true

        start(
            currentPhaseChangedCallback ?: return,
            currentTickCallback ?: return,
            currentCycleCallback ?: return
        )
    }

    // ---------- STATE ----------


    fun getCurrentConfig(): Pair<Int, Int> =
        (workDurationMillis / 60000).toInt() to (breakDurationMillis / 60000).toInt()

    fun isWorkPhase(): Boolean = isWorkPhase
    fun isPaused(): Boolean = isPaused
    fun isRunning(): Boolean = isRunning

    fun getRemainingMillis(): Long = remainingTimeMillis
    fun getTotalPhaseMillis(): Long = currentPhaseDurationMillis
}