package com.nhathuy.randomlucky.data.repository

import com.nhathuy.randomlucky.data.local.dao.LotteryDao
import com.nhathuy.randomlucky.data.mapper.LotteryMapper
import com.nhathuy.randomlucky.domain.model.LotteryPrize
import com.nhathuy.randomlucky.domain.model.LotterySession
import com.nhathuy.randomlucky.domain.repository.LotteryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LotteryRepositoryImpl(
    private val dao : LotteryDao,
    private val mapper: LotteryMapper,
    private val prizes : List<LotteryPrize>
) : LotteryRepository{

    override suspend fun saveLotterySession(session: LotterySession) {
        dao.insertSession(mapper.mapToEntity(session))
    }

    override suspend fun getLotterySession(id: String): LotterySession? {
        return dao.getSession(id)?.let { mapper.mapToDomain(it,prizes) }
    }

    override fun getAllLotterySessions(): Flow<List<LotterySession>> {
        return dao.getAllSessions().map {
            entities ->
            entities.map { mapper.mapToDomain(it,prizes ) }
        }
    }

    override suspend fun deleteLotterySession(id: String) {
        dao.deleteSessionById(id)
    }

    override suspend fun clearAllSessions() {
        dao.clearAllSessions()
    }

}