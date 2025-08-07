package com.nhathuy.randomlucky.domain.model

/**
 * Lớp đại diện cho kết quả của một giải thưởng trong lượt xổ số.
 *
 * @property prize Thông tin về giải thưởng tương ứng (ví dụ: Giải Nhất, Giải Đặc Biệt).
 * @property numbers Danh sách các số đã rút được cho giải này (có thể có nhiều kết quả nếu prize.numberOfResults > 1).
 * @property timestamp Thời điểm rút kết quả, tính bằng milliseconds kể từ thời điểm epoch (mặc định là thời gian hiện tại).
 */
data class LotteryResult(
    val prize: LotteryPrize,
    val numbers: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)
