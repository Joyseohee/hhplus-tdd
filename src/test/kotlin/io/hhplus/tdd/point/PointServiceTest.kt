package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearMocks
import io.mockk.mockk

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

			`when`("유저 정보가 있다면") {
				then("포인트 테이블에서 포인트를 조회한다") {

				}
			}

			`when`("유저 정보가 없다면") {
				then("예외가 발생한다") {

				}
			}
		}

		given("포인트 충전 요청이 들어 왔을 때") {

			`when`("유저 정보가 존재하고 양수 금액을 한도 이내로 요청한다면") {
				then("증가한 포인트가 테이블에 적절히 반영되고 updateTime이 갱신되며 CHARGE로 히스토리가 남는다") {

				}
			}

			`when`("유저 정보가 없다면") {
				then("예외가 발생하고 테이블은 변경되지 않으며 히스토리도 남지 않는다") {

				}
			}

			`when`("0원 이하의 금액을 충전하면") {
				then("예외가 발생하고 테이블은 변경되지 않으며 히스토리도 남지 않는다") {

				}
			}

			`when`("충전 시 잔고가 50만원을 초과하면") {
				then("예외가 발생하고 테이블은 변경되지 않으며 히스토리도 남지 않는다") {

				}
			}

			`when`("충전에는 성공했지만 히스토리 적재에 실패했다면") {
				then("예외가 발생하고 테이블은 롤백된다") {

				}
			}
		}

		given("포인트 사용 요청이 들어 왔을 때") {

			`when`("유저 정보가 존재하고 잔고를 초과하지 않는 금액을 요청한다면") {

			}

			`when`("유저 정보가 없다면") {
				then("예외 발생 후 어떠한 변경도 발생하지 않는다") {

				}
			}

			`when`("0원 이하의 금액을 사용하면") {
				then("예외 발생 후 어떠한 변경도 발생하지 않는다") {

				}
			}

			`when`("잔고보다 많은 금액을 사용하면") {
				then("예외 발생 후 테이블은 변경되지 않는다") {

				}
			}

			`when`("사용은 성공했지만 히스토리 적재에 실패하면") {
				then("예외 발생 후 롤백 로직이 동작한다") {

				}
			}
		}

		given("포인트 내역 조회 요청이 들어 왔을 때") {

			`when`("유저 정보가 있다면") {
				then("해당 유저의 히스토리만 시간순으로 반환된다") {

				}
			}

			`when`("유저 정보가 없다면") {
				then("예외를 발생시킨다") {

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