package com.nhathuy.randomlucky.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nhathuy.randomlucky.data.local.entity.LotterySessionEntity
import com.nhathuy.randomlucky.domain.model.LotteryPrize
import com.nhathuy.randomlucky.domain.model.LotteryResult
import com.nhathuy.randomlucky.domain.model.LotterySession

/**
 * Lớp dùng để chuyển đổi giữa LotterySessionEntity
 * và LotterySession
 */
class LotteryMapper(private val gson: Gson) {

    /**
     * Chuyển đổi từ entity (từ local database) sang model trong domain.
     *
     * @param entity Dữ liệu phiên xổ số được lưu trữ trong database (dưới dạng JSON).
     * @param prizes Danh sách các giải thưởng để ánh xạ ID giải thưởng sang đối tượng LotteryPrize tương ứng.
     * @return Đối tượng LotterySession thuộc domain.
     */
    fun mapToDomain(entity: LotterySessionEntity, prizes: List<LotteryPrize>) : LotterySession {
        val resultsType = object  : TypeToken<Map<String, List<String>>>() {}.type
        val resultsMap : Map<String, List<String>> = gson.fromJson(entity.resultsJson,resultsType)

        val domainResults = resultsMap.mapNotNull {
            (prizeId,numbers) ->
            prizes.find {
                it.id == prizeId
            }?.let {
                prize ->
                prizeId to LotteryResult(prize,numbers)
            }
        }.toMap()

        return LotterySession(
            id = entity.id,
            results = domainResults,
            startTime = entity.startTime,
            endTime = entity.endTime,
            isCompleted = entity.isCompleted
        )
    }

    /**
     * Chuyển đổi từ model domain sang entity để lưu trữ trong local database.
     *
     * @param session Đối tượng LotterySession cần lưu trữ.
     * @return LotterySessionEntity để lưu vào database.
     */
    fun mapToEntity(session: LotterySession) : LotterySessionEntity {
        val resultsMap = session.results.mapValues { it.value.numbers }
        val resultsJson = gson.toJson(resultsMap)

        return LotterySessionEntity(
            id = session.id,
            resultsJson = resultsJson,
            startTime = session.startTime,
            endTime = session.endTime,
            isCompleted = session.isCompleted
        )
    }
}