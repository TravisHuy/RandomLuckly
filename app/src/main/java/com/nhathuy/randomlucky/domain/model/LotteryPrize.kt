package com.nhathuy.randomlucky.domain.model

/**
 * Lớp đại diện cho một giải thưởng xổ số.
 *
 * @property id ID duy nhất của giải thưởng.
 * @property name Tên kỹ thuật hoặc định danh (ví dụ: "giai_nhat", "giai_dac_biet").
 * @property displayName Tên hiển thị của giải (dùng để hiển thị trên giao diện, ví dụ: "Giải Nhất").
 * @property numberOfResults Số lượng kết quả sẽ được rút cho giải này (ví dụ: giải ba có thể có 2 kết quả).
 * @property numberOfDigits Số chữ số của mỗi kết quả (ví dụ: 5 số cho giải đặc biệt, 2 số cho giải tám).
 * @property delayTime Thời gian trễ (milliseconds) trước khi hiển thị kết quả giải này, dùng để tạo hiệu ứng từng bước rút giải.
 */
data class LotteryPrize(
    val id: String,
    val name: String,
    val displayName: String,
    val numberOfResults :Int,
    val numberOfDigits: Int,
    val delayTime : Long
)
