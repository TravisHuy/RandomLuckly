package com.nhathuy.randomlucky.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhathuy.randomlucky.data.sound.SettingManager
import com.nhathuy.randomlucky.data.sound.SoundManager
import com.nhathuy.randomlucky.presentation.state.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val settingsManager: SettingManager,
    private val soundManager: SoundManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        // Combine all settings flows into UI state
        combine(
            settingsManager.isSoundEnabled,
            settingsManager.isDarkModeEnabled,
            settingsManager.isVibrationEnabled
        ) { soundEnabled, darkModeEnabled, vibrationEnabled ->
            SettingsUiState(
                isSoundEnabled = soundEnabled,
                isDarkModeEnabled = darkModeEnabled,
                isVibrationEnabled = vibrationEnabled,
                isLoading = false,
                error = null
            )
        }.onEach { newState ->
            _uiState.value = newState
        }.launchIn(viewModelScope)
    }

    fun toggleSound() {
        viewModelScope.launch {
            try {
                val newValue = !_uiState.value.isSoundEnabled
                settingsManager.setSoundEnabled(newValue)

                // Play test sound when enabling
                if (newValue) {
                    soundManager.play(SoundManager.SoundEffect.DING)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Không thể thay đổi cài đặt âm thanh: ${e.message}"
                )
            }
        }
    }

    fun toggleVibration() {
        viewModelScope.launch {
            try {
                val newValue = !_uiState.value.isVibrationEnabled
                settingsManager.setVibrationEnabled(newValue)

                // Test vibration when enabling
                if (newValue) {
                    soundManager.vibrateShort()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Không thể thay đổi cài đặt rung: ${e.message}"
                )
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            try {
                val newValue = !_uiState.value.isDarkModeEnabled
                settingsManager.setDarkModeEnabled(newValue)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Không thể thay đổi chế độ tối: ${e.message}"
                )
            }
        }
    }

    fun resetAllSettings() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                settingsManager.clearAllSettings()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Không thể đặt lại cài đặt: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}