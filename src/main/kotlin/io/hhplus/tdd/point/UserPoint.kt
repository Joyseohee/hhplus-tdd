package io.hhplus.tdd.point

data class UserPoint private constructor(
    val id: Long,
    val point: Long,
    val updateMillis: Long,
)
) {

    init {
        require(point in MIN_POINT..MAX_POINT) {
            "포인트는 $MIN_POINT 이상 $MAX_POINT 이하여야 합니다. 현재: $point"
        }
    }

    companion object {
        const val MIN_POINT = 0L
        const val MAX_POINT = 500_000L
        const val MIN_TRANSACTION_AMOUNT = 1L

        fun create(id: Long, point: Long = MIN_POINT, updateMillis: Long = System.currentTimeMillis()): UserPoint {
            return UserPoint(id, point, updateMillis)
        }
    }

