package com.nhathuy.randomlucky.presentation.state

data class SettingsUiState(
    val isSoundEnabled: Boolean = true,
    val isDarkModeEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)
