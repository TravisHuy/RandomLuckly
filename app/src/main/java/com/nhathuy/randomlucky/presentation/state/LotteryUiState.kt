package com.nhathuy.randomlucky.presentation.state

import com.nhathuy.randomlucky.domain.model.LotteryPrize
import com.nhathuy.randomlucky.domain.model.LotteryResult
import com.nhathuy.randomlucky.domain.model.LotterySession

data class LotteryUiState(
    val isRunning : Boolean = false,
    val currentPrize : LotteryPrize? = null,
    val isRolling: Boolean = false,
    val isPaused: Boolean = false,
    val rollingProgress: Float = 0f,
    val results: Map<String, LotteryResult> = emptyMap(),
    val completedSession: LotterySession? = null,
    val isLoading : Boolean = false,
    val error: String? = null,
    // Thêm để lưu trạng thái tạm dừng
    val currentPrizeIndex: Int = 0,
    val remainingPrizes: List<LotteryPrize> = emptyList()
){
    val isCompleted: Boolean
        get() = completedSession != null && !isRunning

    val hasResults: Boolean
        get() = results.isNotEmpty()

    val currentResult: LotteryResult?
        get() = currentPrize?.let { results[it.id] }

    val totalPrizes: Int
        get() = results.size

    val totalNumbers: Int
        get() = results.values.sumOf { it.numbers.size }

    val progress: Float
        get() = if (totalPrizes == 0) 0f else totalPrizes / 9f

    val canStart: Boolean
        get() = !isRolling && completedSession == null


    val canReset: Boolean
        get() = hasResults && !isRolling

    val canPause: Boolean
        get() = isRunning && !isPaused && isRolling

    val canResume: Boolean
        get() = isRunning && isPaused


}