package io.hhplus.tdd.point

import io.hhplus.tdd.point.UserPoint.Companion.MAX_POINT
import io.hhplus.tdd.point.UserPoint.Companion.MIN_TRANSACTION_AMOUNT

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe

class UserPointTest : FreeSpec({

    "포인트 충전" - {

        "양수 금액을 충전하면 포인트가 증가한다" {
            val userPoint = UserPoint.create(id = 1L, point = 100L)

            val chargedUserPoint = userPoint.charge(50L)

            chargedUserPoint.point shouldBe 150L
        }

        "0원 이하 금액을 충전하면 예외가 발생한다" {
            val userPoint = UserPoint.create(id = 1L)

            listOf(0L, -10L).forEach { invalidAmount ->
                val exception = shouldThrowExactly<IllegalArgumentException> {
                    userPoint.charge(invalidAmount)
                }
                exception.message shouldBe  "충전 금액은 $MIN_TRANSACTION_AMOUNT 이상이어야 합니다. 현재: $invalidAmount"
            }
        }

        "충전 시 잔고가 50만원을 초과하면 예외가 발생한다" {
            val currentPoint = 500_000L
            val chargeAmount = 10L

            val userPoint = UserPoint.create(id = 1L, point = currentPoint)

            val exception = shouldThrowExactly<IllegalArgumentException> {
                userPoint.charge(chargeAmount)
            }

            exception.message shouldBe "포인트 잔고는 ${MAX_POINT}를 초과할 수 없습니다. 현재 잔고: $currentPoint, 충전 금액: $chargeAmount"
        }
    }

    "포인트 사용" - {

        "잔고를 초과하지 않은 금액을 사용하면 포인트를 차감한다" {

            listOf(50L, 100L).forEach { useAmount ->
                val userPoint = UserPoint.create(id = 1L, point = 100L)
                val usedUserPoint = userPoint.use(useAmount)
                usedUserPoint.point shouldBe 100L - useAmount
                usedUserPoint.point shouldBeGreaterThanOrEqual 0L
            }
        }

        "0원 이하의 금액을 사용하면 예외가 발생한다" {
            listOf(0L, -10L).forEach{ invalidAmount ->
                val currentPoint = 100L
                val userPoint = UserPoint.create(id = 1L, point = currentPoint)
                val exception = shouldThrowExactly<IllegalArgumentException> {
                    userPoint.use(invalidAmount)
                }
                exception.message shouldBe  "사용 금액은 $MIN_TRANSACTION_AMOUNT 이상이어야 합니다. 현재: $invalidAmount"
            }
        }

        "잔고 이상의 금액을 사용하면 예외가 발생한다" {
            listOf(101L, 200L).forEach{ useAmount ->
                val currentPoint = 100L
                val userPoint = UserPoint.create(id = 1L, point = currentPoint)
                val exception = shouldThrowExactly<IllegalArgumentException> {
                    userPoint.use(useAmount)
                }
                exception.message shouldBe "잔고가 부족합니다. 현재 잔고 : $currentPoint , 사용 금액 : $useAmount"
            }
        }
    }
})


