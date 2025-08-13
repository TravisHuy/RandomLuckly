package com.nhathuy.randomlucky.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhathuy.randomlucky.data.sound.SettingManager
import com.nhathuy.randomlucky.data.sound.SoundManager
import com.nhathuy.randomlucky.domain.model.LotteryPrize
import com.nhathuy.randomlucky.domain.model.LotteryResult
import com.nhathuy.randomlucky.domain.usecase.RunLotterySessionUseCase
import com.nhathuy.randomlucky.presentation.state.LotteryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LotteryViewModel @Inject constructor(
    private val runLotterySession: RunLotterySessionUseCase,
    private val soundManager: SoundManager,
    private val settingsManager: SettingManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(LotteryUiState())
    val uiState: StateFlow<LotteryUiState> = _uiState.asStateFlow()

    val isSoundEnabled = settingsManager.isSoundEnabled
    val isVibrationEnabled = settingsManager.isVibrationEnabled
    val isDarkModeEnabled = settingsManager.isDarkModeEnabled


    private var currentLotteryJob: Job? = null
    private var currentSessionId: String? = null
    private var pausedAtPrizeIndex: Int = 0

    private val prizes = listOf(
        LotteryPrize("eighth", "Giải Tám", "Giải Tám", 1, 2, 4000L),
        LotteryPrize("seventh", "Giải Bảy", "Giải Bảy", 1, 3, 4500L),
        LotteryPrize("sixth", "Giải Sáu", "Giải Sáu", 3, 4, 4500L),
        LotteryPrize("fifth", "Giải Năm", "Giải Năm", 1, 4, 5000L),
        LotteryPrize("fourth", "Giải Tư", "Giải Tư", 7, 5, 8000L),
        LotteryPrize("third", "Giải Ba", "Giải Ba", 2, 5, 5500L),
        LotteryPrize("second", "Giải Hai", "Giải Hai", 1, 5, 4500L),
        LotteryPrize("first", "Giải Nhất", "Giải Nhất", 1, 5, 5000L),
        LotteryPrize("special", "Giải Đặc Biệt", "GIẢI ĐẶC BIỆT", 1, 6, 6000L)
    )

    init {
        observeSettingsChanges()
    }

    private fun observeSettingsChanges() {
        settingsManager.isSoundEnabled
            .onEach {
                    soundEnabled ->
                if(!soundEnabled){
                    soundManager.stop()
                }
            }.launchIn(viewModelScope)

        settingsManager.isVibrationEnabled
            .onEach { vibrationEnabled ->
                if(!vibrationEnabled){
                    soundManager.cancelVibration()
                }
            }.launchIn(viewModelScope)

        settingsManager.resetSignal
            .onEach { shouldReset ->
                if (shouldReset) {
                    resetLottery()
                    settingsManager.clearResetSignal()
                }
            }.launchIn(viewModelScope)
    }

    fun startLottery(withAnimation: Boolean = true) {
        val currentState = _uiState.value
        if (currentState.isCompleted || currentState.completedSession != null) {
            resetLottery()
        }

        if (_uiState.value.isRunning || _uiState.value.isRolling) {
            return // Prevent multiple starts
        }

        // Reset trạng thái cho phiên mới
        currentSessionId = UUID.randomUUID().toString()
        pausedAtPrizeIndex = 0

        startLotteryFromIndex(0, withAnimation, emptyMap())
    }

    private fun startLotteryFromIndex(
        startIndex: Int,
        withAnimation: Boolean = true,
        existingResults: Map<String, LotteryResult> = emptyMap()
    ) {
        currentLotteryJob?.cancel()

        currentLotteryJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRunning = true,
                    isPaused = false,
                    currentPrizeIndex = startIndex,
                    remainingPrizes = prizes.drop(startIndex),
                    results = existingResults,
                    completedSession = null
                )
            }

            runLotterySession(
                prizes = prizes,
                withAnimation = withAnimation,
                startFromIndex = startIndex,
                existingResults = existingResults,
                sessionId = currentSessionId
            ).collect { event ->
                when (event) {
                    is RunLotterySessionUseCase.LotteryEvent.Starting -> {
                        // Cập nhật index hiện tại
                        val currentIndex = prizes.indexOfFirst { it.id == event.prize.id }
                        pausedAtPrizeIndex = currentIndex

                        _uiState.update {
                            it.copy(
                                currentPrize = event.prize,
                                isRolling = true,
                                currentPrizeIndex = currentIndex
                            )
                        }

                        if (!_uiState.value.isPaused) {
                            if (event.prize.id == "special") {
                                soundManager.play(SoundManager.SoundEffect.DRUM_ROLL)
                                // Rung nhẹ khi bắt đầu giải đặc biệt
                                soundManager.vibrateShort()
                            } else {
                                soundManager.play(SoundManager.SoundEffect.BALL_SPIN)
                            }
                        }
                    }

                    is RunLotterySessionUseCase.LotteryEvent.Rolling -> {
                        if (!_uiState.value.isPaused) {
                            _uiState.update {
                                it.copy(rollingProgress = event.progress)
                            }

                            if (event.progress > 0.8f) {
                                soundManager.play(SoundManager.SoundEffect.TICK)
                                if (event.progress > 0.95f) {
                                    soundManager.vibrateShort()
                                }
                            }
                        }
                    }

                    is RunLotterySessionUseCase.LotteryEvent.Completed -> {
                        _uiState.update { state ->
                            state.copy(
                                results = state.results + (event.result.prize.id to event.result),
                                isRolling = false,
                                rollingProgress = 0f
                            )
                        }

                        if (!_uiState.value.isPaused) {
                            soundManager.stop()
                            soundManager.play(SoundManager.SoundEffect.BALL_DROP)

                            if (event.result.prize.id == "special") {
                                soundManager.play(SoundManager.SoundEffect.WIN_FANFARE)
                                soundManager.vibrateSpecialWin()
                            }
                            else{
                                soundManager.vibrateSuccess()
                            }
                        }
                    }

                    is RunLotterySessionUseCase.LotteryEvent.SessionCompleted -> {
                        _uiState.update {
                            it.copy(
                                isRunning = false,
                                isPaused = false,
                                currentPrize = null,
                                completedSession = event.session,
                                currentPrizeIndex = 0,
                                remainingPrizes = emptyList(),
                                isRolling = false,
                                rollingProgress = 0f
                            )
                        }

                        soundManager.vibrateLong()

                        currentSessionId = null
                        pausedAtPrizeIndex = 0
                    }
                }
            }
        }
    }

    fun pauseLottery() {
        _uiState.update { it.copy(isPaused = true) }
        soundManager.stop()
        currentLotteryJob?.cancel()
    }

    fun resumeLottery() {
        if (!_uiState.value.isPaused) return

        val currentState = _uiState.value

        // Tiếp tục từ vị trí đã tạm dừng
        startLotteryFromIndex(
            startIndex = pausedAtPrizeIndex,
            withAnimation = true,
            existingResults = currentState.results
        )
    }

    fun resetLottery(){
        currentLotteryJob?.cancel()
        soundManager.stop()

        currentSessionId = null
        pausedAtPrizeIndex = 0

        _uiState.value = LotteryUiState(
            isRunning = false,
            currentPrize = null,
            isRolling = false,
            isPaused = false,
            rollingProgress = 0f,
            results = emptyMap(),
            completedSession = null,
            isLoading = false,
            error = null,
            currentPrizeIndex = 0,
            remainingPrizes = emptyList()
        )
    }

    fun resetForAppReset() {
        // Stop current lottery
        resetLottery()

        // Reinitialize SoundManager
        soundManager.reinitialize()

        // Force reset state và refresh settings
        viewModelScope.launch {
            // Refresh settings từ SharedPreferences
            settingsManager.refreshAllSettings()

            // Reset UI state
            _uiState.emit(LotteryUiState())

                // Wait a bit để đảm bảo settings được update
            delay(100)

            // Re-observe settings sau reset
            observeSettingsChanges()
        }
    }
    override fun onCleared() {
        super.onCleared()
        currentLotteryJob?.cancel()
        soundManager.release()
    }
}