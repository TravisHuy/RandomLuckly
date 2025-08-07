package com.nhathuy.randomlucky.domain.usecase

import com.nhathuy.randomlucky.domain.model.LotteryPrize
import com.nhathuy.randomlucky.domain.model.LotteryResult
import com.nhathuy.randomlucky.domain.model.LotterySession
import com.nhathuy.randomlucky.domain.repository.LotteryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class RunLotterySessionUseCase  @Inject constructor(
    private val repository: LotteryRepository,
    private val generateNumbers: GenerateLotteryNumbersUseCase
) {
    sealed class LotteryEvent {
        data class Starting(val prize: LotteryPrize) : LotteryEvent()
        data class Rolling(val prize: LotteryPrize, val progress: Float) : LotteryEvent()
        data class Completed(val result: LotteryResult) : LotteryEvent()
        data class SessionCompleted(val session: LotterySession) : LotteryEvent()
    }

    operator fun invoke(
        prizes: List<LotteryPrize>,
        withAnimation: Boolean = true,
        startFromIndex: Int = 0,
        existingResults: Map<String, LotteryResult> = emptyMap(),
        sessionId: String? = null
    ): Flow<LotteryEvent> = flow{
        val currentSessionId = sessionId ?: UUID.randomUUID().toString()
        val results = existingResults.toMutableMap()
        val startTime = System.currentTimeMillis()

        // Chỉ quay các giải chưa có kết quả từ vị trí startFromIndex
        val prizesToProcess = prizes.drop(startFromIndex).filter { prize ->
            !results.containsKey(prize.id)
        }

        prizesToProcess.forEach { prize ->
            emit(LotteryEvent.Starting(prize))

            if(withAnimation){
                val rollingDuration = when (prize.id) {
                    "special" -> 6000L
                    else -> 3000L
                }

                val steps = 20
                repeat(steps) { step ->
                    delay(rollingDuration / steps)
                    emit(LotteryEvent.Rolling(prize, (step + 1) / steps.toFloat()))
                }
            }

            val numbers = generateNumbers(prize)
            val result = LotteryResult(prize, numbers)
            results[prize.id] = result

            emit(LotteryEvent.Completed(result))

            if (withAnimation) {
                delay(prize.delayTime)
            }
        }

        val session = LotterySession(
            id = currentSessionId,
            results = results,
            startTime = startTime,
            endTime = System.currentTimeMillis(),
            isCompleted = true
        )

        repository.saveLotterySession(session)
        emit(LotteryEvent.SessionCompleted(session))
    }
}