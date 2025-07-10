package io.hhplus.tdd.point

import io.kotest.core.spec.style.FreeSpec

class UserPointTest : FreeSpec({

    "포인트 충전" - {

        "양수 금액을 충전하면 포인트가 증가한다" {
        }

        "0원 이하 금액을 충전하면 예외가 발생한다" {
        }

        "충전 시 잔고가 50만원을 초과하면 예외가 발생한다" {
        }
    }

    "포인트 사용" - {

        "잔고를 초과하지 않은 금액을 사용하면 포인트를 차감한다" {
        }

        "0원 이하의 금액을 사용하면 예외가 발생한다" {
        }

        "잔고 이상의 금액을 사용하면 예외가 발생한다" {
        }
    }
})

