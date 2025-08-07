package com.nhathuy.randomlucky.domain.usecase

import com.nhathuy.randomlucky.domain.model.LotteryPrize
import javax.inject.Inject
import kotlin.random.Random

class GenerateLotteryNumbersUseCase @Inject constructor() {
    operator fun invoke(prize: LotteryPrize): List<String> {
        return List(prize.numberOfResults) {
            Random.nextInt(0, Math.pow(10.0, prize.numberOfDigits.toDouble()).toInt()).toString()
                .padStart(prize.numberOfDigits, '0')
        }
    }
}