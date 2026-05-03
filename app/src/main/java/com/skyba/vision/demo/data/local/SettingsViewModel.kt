package com.skyba.vision.demo.data.local

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.skyba.vision.demo.data.local.services.TimerService
import com.skyba.vision.demo.data.repository.SettingsRepository
import com.skyba.vision.demo.domain.model.TimerConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repository = SettingsRepository(application)
    private val prefs = context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)


    var currentPhase = mutableStateOf(TimerService.TimerPhase.IDLE)


    // ------------------- Recommended Pop-Up -------------------
    var showRecommendedPopUp by mutableStateOf(false)
        private set

    fun onStartTimerClicked() {
        viewModelScope.launch {
            val hasSeen = repository.hasSeenStabilityAdviceFlow.first()
            if (!hasSeen) showRecommendedPopUp = true
        }
    }

    fun dismissPopUp(permanently: Boolean) {
        showRecommendedPopUp = false
        if (permanently) viewModelScope.launch { repository.markStabilityAdviceAsSeen() }
    }

    // ------------------- Timer Settings -------------------

    // Use the repository as the single source of truth
    val timerConfig: StateFlow<TimerConfig> = repository.timerConfigFlow
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            TimerConfig(
                SettingsRepository.DEFAULT_WORK,
                SettingsRepository.DEFAULT_BREAK
            )
        )

    fun setWorkDuration(value: Int) {
        viewModelScope.launch {
            repository.saveWorkDuration(value)
        }
    }

    fun setBreakDuration(value: Int) {
        viewModelScope.launch {
            repository.saveBreakDuration(value)
        }
    }


    fun incrementWork() = setWorkDuration((timerConfig.value.work + 1).coerceAtMost(60))
    fun decrementWork() = setWorkDuration((timerConfig.value.work - 1).coerceAtLeast(1))

    fun incrementBreak() = setBreakDuration((timerConfig.value.breakTime + 1).coerceAtMost(60))
    fun decrementBreak() = setBreakDuration((timerConfig.value.breakTime - 1).coerceAtLeast(1))

    // ------------------- Intents -------------------
    fun buildStartIntent(context: Context): Intent {
        val intent = Intent(context, TimerService::class.java)
        intent.action = TimerService.TimerAction.START.name
        return intent
    }

    fun buildRestartIntent(context: Context): Intent {
        val intent = Intent(context, TimerService::class.java)
        intent.action = TimerService.TimerAction.RESTART.name
        return intent
    }

    // ------------------- Sound Settings -------------------
    private var previewPlayer: MediaPlayer? = null

    fun previewSound(fileName: String?) {
        if (fileName == null) return
        val resId = context.resources.getIdentifier(fileName, "raw",
            context.packageName)
        if (resId == 0) return

        previewPlayer?.release()
        previewPlayer = MediaPlayer.create(context, resId).apply {
            setOnCompletionListener {
                it.release()
                previewPlayer = null
            }
            start()
        }
    }

    fun stopPreviewSound() {
        previewPlayer?.stop()
        previewPlayer?.release()
        previewPlayer = null
    }

    private val _phaseSound = MutableStateFlow(loadSoundFromPrefs())
    val phaseSound: StateFlow<PhaseSoundOption> = _phaseSound

    fun setPhaseSound(option: PhaseSoundOption) {
        _phaseSound.value = option
        prefs.edit().putString("phaseSoundOption", option.name).apply()
    }

    private fun loadSoundFromPrefs(): PhaseSoundOption {
        val name = prefs.getString("phaseSoundOption", PhaseSoundOption.SOUND_1.name)
        return try { PhaseSoundOption.valueOf(name ?: PhaseSoundOption.SOUND_1.name) }
        catch (e: IllegalArgumentException) { PhaseSoundOption.SOUND_1 }
    }



    override fun onCleared() {
        super.onCleared()
        previewPlayer?.release()
        previewPlayer = null
    }
}