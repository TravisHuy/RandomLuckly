package com.nhathuy.randomlucky.domain.model

/**
 * Lớp đại diện cho một phiên rút số xổ số (một lần quay đầy đủ các giải).
 *
 * @property id Mã định danh duy nhất cho phiên rút số (có thể là theo ngày hoặc UUID).
 * @property results Map chứa kết quả từng giải, với key là ID của giải (ví dụ: "giai_nhat") và value là kết quả tương ứng.
 * @property startTime Thời điểm bắt đầu phiên rút số (theo milliseconds từ epoch).
 * @property endTime Thời điểm kết thúc phiên (nullable – sẽ là null khi phiên chưa hoàn tất).
 * @property isCompleted Cờ đánh dấu phiên rút số đã hoàn thành hay chưa.
 */
data class LotterySession(
    val id: String,
    val results: Map<String, LotteryResult>,
    val startTime: Long,
    val endTime: Long? = null,
    val isCompleted: Boolean = false
)
