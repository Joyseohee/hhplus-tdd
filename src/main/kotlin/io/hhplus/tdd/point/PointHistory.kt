package io.hhplus.tdd.point

data class PointHistory private constructor(
    val id: Long,
    val userId: Long,
    val type: TransactionType,
    val amount: Long,
    val timeMillis: Long,
) {
    companion object {
        fun create(
            id: Long,
            userId: Long,
            type: TransactionType,
            amount: Long,
            timeMillis: Long = System.currentTimeMillis()
        ): PointHistory {
            require(amount > 0) { "금액은 0보다 커야 합니다." }
            return PointHistory(id, userId, type, amount, timeMillis)
        }
    }
}


/**
 * 포인트 트랜잭션 종류
 * - CHARGE : 충전
 * - USE : 사용
 */
enum class TransactionType {
    CHARGE, USE
}

