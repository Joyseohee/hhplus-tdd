package io.hhplus.tdd.point

data class UserPoint private constructor(
    val id: Long,
    val point: Long,
    val updateMillis: Long,
)
) {

    companion object {
        const val MIN_POINT = 0L
        const val MAX_POINT = 500_000L
        const val MIN_TRANSACTION_AMOUNT = 1L

        fun create(id: Long, point: Long = MIN_POINT, updateMillis: Long = System.currentTimeMillis()): UserPoint {
            return UserPoint(id, point, updateMillis)
        }
    }

