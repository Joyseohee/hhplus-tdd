package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable

import io.hhplus.tdd.point.UserPoint.Companion.MAX_POINT
import io.hhplus.tdd.point.UserPoint.Companion.MIN_TRANSACTION_AMOUNT

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.mockk.*

class PointServiceTest : BehaviorSpec({

	/**
	 * 도메인에서 검증할 수 없는 비즈니스 로직 적용 여부를 검증
	 * 1. 어떤 행위가
	 * 2. 어떤 순서로 이뤄졌으며
	 * 3. 어떤 객체로 위임되었는가
	 * 4. 트랜잭션의 범위
	 */

	context("단위 테스트 : mock으로 행위 검증") {
		val userPointTable = mockk<UserPointTable>()
		val pointHistoryTable = mockk<PointHistoryTable>()
		val pointService = PointService(userPointTable, pointHistoryTable)

		beforeTest {
			clearMocks(userPointTable, pointHistoryTable, answers = false, recordedCalls = true)
		}

		given("포인트 조회 요청이 들어 왔을 때") {
			val validUserId = 1L
			val nonExistentUserId = 999L
			val expectedUserPoint = UserPoint.create(id = validUserId, point = 10_000L)

			`when`("유저 정보가 있다면") {
				then("포인트 테이블에서 포인트를 조회한다") {
					every { userPointTable.selectById(validUserId) } returns expectedUserPoint

					val result = pointService.getUserPoint(validUserId)

					result shouldBe expectedUserPoint
					verify(exactly = 1) { userPointTable.selectById(validUserId) }

				}
			}

			`when`("유저 정보가 없다면") {
				then("예외가 발생한다") {
					every { userPointTable.selectById(nonExistentUserId) } returns null

					val exception = shouldThrow<UserNotFoundException> {
						pointService.getUserPoint(nonExistentUserId)
					}

					exception.message shouldBe "${nonExistentUserId}는 존재하지 않는 사용자입니다."
					verify(exactly = 1) { userPointTable.selectById(nonExistentUserId) }
				}
			}
		}

		given("포인트 충전 요청이 들어 왔을 때") {
			val userId = 1L
			val initialPoint = 10_000L
			val chargeAmount = 10_000L
			val userPoint = UserPoint.create(id = userId, point = initialPoint)

			`when`("유저 정보가 존재하고 양수 금액을 한도 이내로 요청한다면") {
				then("증가한 포인트가 테이블에 적절히 반영되고 updateTime이 갱신되며 CHARGE로 히스토리가 남는다") {
					val expectedPoint = initialPoint + chargeAmount
					val updatedUserPoint = UserPoint.create(id = userId, point = expectedPoint)

					every { userPointTable.selectById(userId) } returns userPoint
					every { userPointTable.insertOrUpdate(userId, expectedPoint) } returns updatedUserPoint
					every { pointHistoryTable.insert(userId, chargeAmount, TransactionType.CHARGE) } returns
							PointHistory.create(1L, userId, TransactionType.CHARGE, chargeAmount)

					val result = pointService.chargePoint(userId, chargeAmount)

					result.point shouldBe expectedPoint
					result.updateMillis shouldBeGreaterThan userPoint.updateMillis

					verify(exactly = 1) { userPointTable.selectById(userId) }
					verify(exactly = 1) { userPointTable.insertOrUpdate(userId, expectedPoint) }
					verify(exactly = 1) { pointHistoryTable.insert(userId, chargeAmount, TransactionType.CHARGE) }
				}
			}

			`when`("유저 정보가 없다면") {
				then("예외가 발생하고 테이블은 변경되지 않으며 히스토리도 남지 않는다") {
					val nonExistUserId = 999L
					every { userPointTable.selectById(nonExistUserId) } returns null

					val exception = shouldThrow<UserNotFoundException> {
						pointService.chargePoint(nonExistUserId, chargeAmount)
					}

					exception.message shouldBe "${nonExistUserId}는 존재하지 않는 사용자입니다."

					verify(exactly = 1) { userPointTable.selectById(nonExistUserId) }
					verify { userPointTable.insertOrUpdate(any(), any()) wasNot Called }
					verify { pointHistoryTable.insert(any(), any(), any()) wasNot Called }
				}
			}

			`when`("0원 이하의 금액을 충전하면") {
				then("예외가 발생하고 테이블은 변경되지 않으며 히스토리도 남지 않는다") {
					listOf(0L, -100L, -10_000L).forEach { invalidAmount ->
						every { userPointTable.selectById(userId) } returns userPoint

						val exception = shouldThrow<IllegalArgumentException> {
							pointService.chargePoint(userId, invalidAmount)
						}

						exception.message shouldBe "충전 금액은 $MIN_TRANSACTION_AMOUNT 이상이어야 합니다. 현재: $invalidAmount"

						verify(exactly = 1) { userPointTable.selectById(userId) }
						verify(exactly = 0) { userPointTable.insertOrUpdate(any(), any()) }
						verify(exactly = 0) { pointHistoryTable.insert(any(), any(), any()) }

						clearMocks(userPointTable, pointHistoryTable)
					}
				}
			}

			`when`("충전 시 잔고가 50만원을 초과하면") {
				then("예외가 발생하고 테이블은 변경되지 않으며 히스토리도 남지 않는다") {
					val overAmount = 600_000L
					val currentPoint = 400_000L
					val overUser = UserPoint.create(userId, currentPoint)

					every { userPointTable.selectById(userId) } returns overUser

					val exception = shouldThrow<IllegalArgumentException> {
						pointService.chargePoint(userId, overAmount)
					}

					exception.message shouldBe "포인트 잔고는 ${MAX_POINT}를 초과할 수 없습니다. 현재 잔고: $currentPoint, 충전 금액: $overAmount"

					verify(exactly = 1) { userPointTable.selectById(userId) }
					verify(exactly = 0) { userPointTable.insertOrUpdate(any(), any()) }
					verify(exactly = 0) { pointHistoryTable.insert(any(), any(), any()) }
				}
			}

			`when`("충전에는 성공했지만 히스토리 적재에 실패했다면") {
				then("예외가 발생하고 테이블은 롤백된다") {
					val expectedPoint = initialPoint + chargeAmount
					val updatedUserPoint = UserPoint.create(userId, expectedPoint)

					every { userPointTable.selectById(userId) } returns userPoint
					every { userPointTable.insertOrUpdate(userId, expectedPoint) } returns updatedUserPoint
					every { userPointTable.insertOrUpdate(userId, initialPoint) } returns userPoint
					every { pointHistoryTable.insert(userId, chargeAmount, TransactionType.CHARGE) } throws RuntimeException("예외 발생")

					val exception = shouldThrow<PointTransactionFailedException> {
						pointService.chargePoint(userId, chargeAmount)
					}

					exception.message shouldBe "정상적으로 처리되지 않았습니다."

					verify(exactly = 1) { userPointTable.selectById(userId) }
					verify(exactly = 1) { userPointTable.insertOrUpdate(userId, expectedPoint) }
					verify(exactly = 1) { pointHistoryTable.insert(userId, chargeAmount, TransactionType.CHARGE) }
					verify(exactly = 1) { userPointTable.insertOrUpdate(userId, initialPoint) }
				}
			}
		}

		given("포인트 사용 요청이 들어 왔을 때") {
			val userId = 1L
			val pointToUse = 5_000L
			val currentPoint = 10_000L
			val userPoint = UserPoint.create(userId, currentPoint)

			`when`("유저 정보가 존재하고 잔고를 초과하지 않는 금액을 요청한다면") {
				then("포인트를 차감해서 반영하고 updateTime이 갱신되며 USE 히스토리가 남는다") {
					val expectedPoint = currentPoint - pointToUse
					val updatedUserPoint = UserPoint.create(userId, expectedPoint)
					val expectedHistory = PointHistory.create(1L, userId, TransactionType.USE, pointToUse)

					every { userPointTable.selectById(userId) } returns userPoint
					every { userPointTable.insertOrUpdate(userId, expectedPoint) } returns updatedUserPoint
					every { pointHistoryTable.insert(userId, pointToUse, TransactionType.USE) } returns expectedHistory

					val result = pointService.usePoint(userId, pointToUse)

					result.point shouldBe expectedPoint
					result.updateMillis shouldBeGreaterThan userPoint.updateMillis

					verify(exactly = 1) { userPointTable.selectById(userId) }
					verify(exactly = 1) { userPointTable.insertOrUpdate(userId, expectedPoint) }
					verify(exactly = 1) { pointHistoryTable.insert(userId, pointToUse, TransactionType.USE) }
				}
			}

			`when`("유저 정보가 없다면") {
				then("예외 발생 후 어떠한 변경도 발생하지 않는다") {
					val unknownUserId = 999L
					every { userPointTable.selectById(unknownUserId) } returns null

					val exception = shouldThrow<UserNotFoundException> {
						pointService.usePoint(unknownUserId, pointToUse)
					}

					exception.message shouldBe "${unknownUserId}는 존재하지 않는 사용자입니다."

					verify(exactly = 1) { userPointTable.selectById(unknownUserId) }
					verify { userPointTable.insertOrUpdate(any(), any()) wasNot Called }
					verify { pointHistoryTable.insert(any(), any(), any()) wasNot Called }
				}
			}

			`when`("0원 이하의 금액을 사용하면") {
				then("예외 발생 후 어떠한 변경도 발생하지 않는다") {
					listOf(0L, -1L, -10_000L).forEach { invalidAmount ->
						every { userPointTable.selectById(userId) } returns userPoint

						val exception = shouldThrow<IllegalArgumentException> {
							pointService.usePoint(userId, invalidAmount)
						}

						exception.message shouldBe "사용 금액은 $MIN_TRANSACTION_AMOUNT 이상이어야 합니다. 현재: $invalidAmount"

						verify(exactly = 1) { userPointTable.selectById(userId) }
						verify { userPointTable.insertOrUpdate(any(), any()) wasNot Called }
						verify { pointHistoryTable.insert(any(), any(), any()) wasNot Called }

						clearMocks(userPointTable, pointHistoryTable)
					}
				}
			}

			`when`("잔고보다 많은 금액을 사용하면") {
				then("예외 발생 후 테이블은 변경되지 않는다") {
					val overAmount = currentPoint + 1_000L
					every { userPointTable.selectById(userId) } returns userPoint

					val exception = shouldThrow<IllegalArgumentException> {
						pointService.usePoint(userId, overAmount)
					}

					exception.message shouldBe "잔고가 부족합니다. 현재 잔고 : $currentPoint , 사용 금액 : $overAmount"

					verify(exactly = 1) { userPointTable.selectById(userId) }
					verify { userPointTable.insertOrUpdate(any(), any()) wasNot Called }
					verify { pointHistoryTable.insert(any(), any(), any()) wasNot Called }
				}
			}

			`when`("사용은 성공했지만 히스토리 적재에 실패하면") {
				then("예외 발생 후 롤백 로직이 동작한다") {
					val expectedPoint = currentPoint - pointToUse
					val updatedUserPoint = UserPoint.create(userId, expectedPoint)

					every { userPointTable.selectById(userId) } returns userPoint
					every { userPointTable.insertOrUpdate(userId, expectedPoint) } returns updatedUserPoint
					every { userPointTable.insertOrUpdate(userId, currentPoint) } returns userPoint
					every { pointHistoryTable.insert(userId, pointToUse, TransactionType.USE) } throws RuntimeException("예외 발생")

					val exception = shouldThrow<PointTransactionFailedException> {
						pointService.usePoint(userId, pointToUse)
					}

					exception.message shouldBe "정상적으로 처리되지 않았습니다."

					verify(exactly = 1) { userPointTable.selectById(userId) }
					verify(exactly = 1) { userPointTable.insertOrUpdate(userId, expectedPoint) } // 롤백 포함
					verify(exactly = 1) { pointHistoryTable.insert(userId, pointToUse, TransactionType.USE) }
					verify(exactly = 1) { userPointTable.insertOrUpdate(userId, currentPoint) } // 롤백 포함
				}
			}
		}

		given("포인트 내역 조회 요청이 들어 왔을 때") {

			val userId = 1L
			val userPoint = UserPoint.create(id = userId, point = 10_000L)

			val unorderedHistories = listOf(
				PointHistory.create(id = 10L, userId = userId, type = TransactionType.CHARGE, amount = 3_000L, timeMillis = 3000L),
				PointHistory.create(id = 11L, userId = userId, type = TransactionType.USE, amount = 2_000L, timeMillis = 2000L),
				PointHistory.create(id = 12L, userId = userId, type = TransactionType.CHARGE, amount = 5_000L, timeMillis = 1000L),
			)

			`when`("유저 정보가 있다면") {
				then("해당 유저의 히스토리만 시간순으로 반환된다") {
					every { userPointTable.selectById(userId) } returns userPoint
					every { pointHistoryTable.selectAllByUserId(userId) } returns unorderedHistories

					val result = pointService.getUserPointHistory(userId)

					result shouldHaveSize 3
					result.all { it.userId == userId } shouldBe true
					result shouldBe unorderedHistories.sortedBy { it.timeMillis }

					verify(exactly = 1) { userPointTable.selectById(userId) }
					verify(exactly = 1) { pointHistoryTable.selectAllByUserId(userId) }
				}
			}

			`when`("유저 정보가 없다면") {
				then("예외를 발생시킨다") {
					val nonExistentUserId = 999L
					every { userPointTable.selectById(nonExistentUserId) } returns null

					val exception = shouldThrow<UserNotFoundException> {
						pointService.getUserPointHistory(nonExistentUserId)
					}

					exception.message shouldBe "${nonExistentUserId}는 존재하지 않는 사용자입니다."

					verify(exactly = 1) { userPointTable.selectById(nonExistentUserId) }
					verify(exactly = 0) { pointHistoryTable.selectAllByUserId(any()) }
				}
			}
		}
	}

		/**
		 * 도메인 객체 테스트 및 함수 호출 여부 만으로 확인하기 어려운 영역 검증
		 * 1. 실제 충전/사용 시 상태 변경이 의도한 대로 되었는가
		 * 2. 예외 발생 시 롤백이 되었는가
		 */
		context("통합 테스트 : 실제 객체로 상태 검증") {

			given("실제 저장소에 포인트를 충전하고 사용하면") {
				`when`("충전과 사용이 성공적으로 이뤄질 때") {
					then("잔고와 히스토리가 모두 반영된다") {

					}
				}

				`when`("충전 금액이 최대 잔고를 초과하면") {
					then("예외가 발생하고 저장소에는 변화가 없다") {

					}
				}

				`when`("사용 금액이 잔고보다 크면") {
					then("예외가 발생하고 저장소에는 변화가 없다") {

					}
				}
			}
		}
	}

})