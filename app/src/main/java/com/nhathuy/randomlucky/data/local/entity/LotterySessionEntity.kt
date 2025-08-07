package com.nhathuy.randomlucky.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lottery_sessions")
data class LotterySessionEntity(
    @PrimaryKey
    val id: String,
    val resultsJson: String,
    val startTime: Long,
    val endTime: Long?,
    val isCompleted: Boolean
)
