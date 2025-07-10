package io.hhplus.tdd.point

data class UserPoint private constructor(
    val id: Long,
    val point: Long,
    val updateMillis: Long,
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

    // 충전 정책(외부에서 알 필요 없음)
    private fun validateChargePolicy(chargingPoint: Long) {
        require(chargingPoint >= MIN_TRANSACTION_AMOUNT) {  "충전 금액은 $MIN_TRANSACTION_AMOUNT 이상이어야 합니다. 현재: $chargingPoint" }
        require(point + chargingPoint <= MAX_POINT) {"포인트 잔고는 ${MAX_POINT}를 초과할 수 없습니다. 현재 잔고: $point, 충전 금액: $chargingPoint"}
    }

    // 사용 정책(외부에서 알 필요 없음)
    private fun validateUsePolicy(usingPoint: Long) {
        require(usingPoint >= MIN_TRANSACTION_AMOUNT) {  "사용 금액은 $MIN_TRANSACTION_AMOUNT 이상이어야 합니다. 현재: $usingPoint" }
        require(point >= usingPoint){ "잔고가 부족합니다. 현재 잔고 : $point , 사용 금액 : $usingPoint" }
    }

    // point 충전
    fun charge(amount: Long): UserPoint {
        validateChargePolicy(amount)
        return copy(point = point + amount)
    }

    // point 사용
    fun use(amount: Long): UserPoint {
        validateUsePolicy(amount)
        return copy(point = point - amount)
    }
}