package com.nhathuy.randomlucky.domain.repository

import com.nhathuy.randomlucky.domain.model.LotterySession
import kotlinx.coroutines.flow.Flow

/**
 * Interface định nghĩa các thao tác liên quan đến dữ liệu phiên rút số (LotterySession).
 * Áp dụng mô hình Repository để tách biệt tầng xử lý logic và tầng dữ liệu.
 */
interface LotteryRepository {

    /**
     * Lưu một phiên rút số (có thể là mới hoặc cập nhật phiên cũ).
     *
     * @param session phiên rút số cần lưu.
     */
    suspend fun saveLotterySession(session: LotterySession)

    /**
     * Lấy thông tin một phiên rút số theo ID.
     *
     * @param id ID của phiên rút số.
     * @return LotterySession nếu tìm thấy, ngược lại trả về null.
     */
    suspend fun getLotterySession(id: String): LotterySession?

    /**
     * Lấy danh sách tất cả các phiên rút số đã lưu.
     * Trả về dưới dạng Flow để dễ dàng quan sát dữ liệu thay đổi theo thời gian thực.
     *
     * @return Flow chứa danh sách các phiên rút số.
     */
    fun getAllLotterySessions(): Flow<List<LotterySession>>

    /**
     * Xoá một phiên rút số theo ID.
     *
     * @param id ID của phiên cần xoá.
     */
    suspend fun deleteLotterySession(id: String)

    /**
     * clear all session
     */
    suspend fun clearAllSessions()
}
