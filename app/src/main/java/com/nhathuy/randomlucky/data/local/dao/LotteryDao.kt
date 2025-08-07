package com.nhathuy.randomlucky.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nhathuy.randomlucky.data.local.entity.LotterySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LotteryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: LotterySessionEntity)

    @Query("SELECT * FROM lottery_sessions WHERE id = :id")
    suspend fun getSession(id: String): LotterySessionEntity?

    @Query("SELECT * FROM lottery_sessions WHERE endTime IS NOT NULL ORDER BY startTime DESC LIMIT :limit")
    suspend fun getCompletedSessions(limit: Int = 10): List<LotterySessionEntity>

    @Query("SELECT * FROM lottery_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSession(): LotterySessionEntity?

    @Query("SELECT * FROM lottery_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<LotterySessionEntity>>

    @Query("DELETE FROM lottery_sessions")
    suspend fun clearAllSessions()

    @Delete
    suspend fun deleteSession(session: LotterySessionEntity)

    @Query("DELETE FROM lottery_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)
}