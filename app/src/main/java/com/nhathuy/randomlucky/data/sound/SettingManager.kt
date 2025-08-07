package com.nhathuy.randomlucky.data.sound

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class SettingManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_DARK_MODE_ENABLED = "dark_mode_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"

        // Default values
        private const val DEFAULT_SOUND_ENABLED = true
        private const val DEFAULT_DARK_MODE_ENABLED = true
        private const val DEFAULT_VIBRATION_ENABLED = true
    }

    private val _isSoundEnabled = MutableStateFlow(getSoundEnabled())
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _isDarkModeEnabled = MutableStateFlow(getDarkModeEnabled())
    val isDarkModeEnabled: StateFlow<Boolean> = _isDarkModeEnabled.asStateFlow()

    private val _isVibrationEnabled = MutableStateFlow(getVibrationEnabled())
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()

    private val _resetSignal = MutableStateFlow(false)
    val resetSignal: StateFlow<Boolean> = _resetSignal.asStateFlow()

    private fun getSoundEnabled(): Boolean =
        sharedPreferences.getBoolean(KEY_SOUND_ENABLED, DEFAULT_SOUND_ENABLED)

    private fun getDarkModeEnabled(): Boolean =
        sharedPreferences.getBoolean(KEY_DARK_MODE_ENABLED, DEFAULT_DARK_MODE_ENABLED)

    private fun getVibrationEnabled(): Boolean =
        sharedPreferences.getBoolean(KEY_VIBRATION_ENABLED, DEFAULT_VIBRATION_ENABLED)

    fun setSoundEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_SOUND_ENABLED, enabled) }
        _isSoundEnabled.value = enabled
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_DARK_MODE_ENABLED, enabled) }
        _isDarkModeEnabled.value = enabled
    }

    fun setVibrationEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_VIBRATION_ENABLED, enabled) }
        _isVibrationEnabled.value = enabled
    }

    fun clearAllSettings() {
        sharedPreferences.edit { clear() }
        // Reset to default values
        resetToDefaults()
    }

    // Thêm method để reset về default values một cách rõ ràng
    fun resetToDefaults() {
        sharedPreferences.edit {
            putBoolean(KEY_SOUND_ENABLED, DEFAULT_SOUND_ENABLED)
            putBoolean(KEY_DARK_MODE_ENABLED, DEFAULT_DARK_MODE_ENABLED)
            putBoolean(KEY_VIBRATION_ENABLED, DEFAULT_VIBRATION_ENABLED)
        }

        // Update StateFlow values
        _isSoundEnabled.value = DEFAULT_SOUND_ENABLED
        _isDarkModeEnabled.value = DEFAULT_DARK_MODE_ENABLED
        _isVibrationEnabled.value = DEFAULT_VIBRATION_ENABLED
        _resetSignal.value = true
    }

    fun clearResetSignal() {
        _resetSignal.value = false
    }

    // Thêm method để refresh tất cả StateFlow từ SharedPreferences
    fun refreshAllSettings() {
        _isSoundEnabled.value = getSoundEnabled()
        _isDarkModeEnabled.value = getDarkModeEnabled()
        _isVibrationEnabled.value = getVibrationEnabled()
    }
}