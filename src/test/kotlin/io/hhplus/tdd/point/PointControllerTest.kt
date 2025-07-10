package io.hhplus.tdd.point

import io.hhplus.tdd.ApiControllerAdvice

import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.setup.MockMvcBuilders

/**
 * 외부로부터의 입력과 출력에 대한 테스트를 수행하는 컨트롤러 테스트
 */
@WebMvcTest(PointController::class)
@Import(ApiControllerAdvice::class)
class PointControllerTest : BehaviorSpec({
	val pointService = mockk<PointService>()
	val mockMvc = MockMvcBuilders.standaloneSetup(PointController(pointService))
		.setControllerAdvice(ApiControllerAdvice())
		.build()

	beforeTest {
		clearMocks(pointService, answers = false, recordedCalls = true)
	}

	val userId = 1L
	given("GET /point/{id} 요청") {
		// 성공 케이스
		`when`("요청에 성공한다면") {
			then("200 OK와 포인트를 반환한다") {
				// given
				val userId = 1L
				val userPoint = UserPoint.create(id = userId)

				every { pointService.getUserPoint(userId) } returns userPoint

				// when & then
				mockMvc.get("/point/$userId")
					.andExpect {
						status { isOk() }
						jsonPath("$.id") { value(userId) }
						jsonPath("$.point") { value(0L) }
					}
			}
		}
		// Request 요청 매핑 오류
		`when`("요청이 잘못돼서 예외가 발생했다면") {
			then("400 BAD_REQUEST와 에러 메시지를 반환한다") {
				// given
				val invalidUserId = "invalid"

				// when & then
				mockMvc.get("/point/$invalidUserId")
					.andExpect {
						status { isBadRequest() }
						jsonPath("$.code") { value("400") }
						jsonPath("$.message") { value("요청 파라미터 타입이 올바르지 않습니다.") }
					}
			}
		}
		// 서비스에서 예외 전파
		`when` ("유저 정보가 없다면") {
			then("404 NOT_FOUND와 에러 메시지를 반환한다") {
				// given
				val nonExistentUserId = 999L
				every { pointService.getUserPoint(nonExistentUserId) } throws UserNotFoundException(nonExistentUserId)

				// when & then
				mockMvc.get("/point/$nonExistentUserId")
					.andExpect {
						status { isNotFound() }
						jsonPath("$.code") { value("404") }
						jsonPath("$.message") { value("${nonExistentUserId}는 존재하지 않는 사용자입니다.") }
					}
			}
		}
	}

	given("PATCH /point/{id}/charge 요청") {
		`when` ("요청에 성공한다면") {
			then("200 OK와 포인트 현황을 반환한다") {
				// given
				val userId = 1L
				val chargeAmount = 10_000L
				val userPoint = UserPoint.create(id = userId, point = chargeAmount)

				every { pointService.chargePoint(userId, chargeAmount) } returns userPoint

				// when & then
				mockMvc.patch("/point/$userId/charge") {
					contentType = MediaType.APPLICATION_JSON
					content = "$chargeAmount"
				}
					.andExpect {
						status { isOk() }
						jsonPath("$.id") { value(userId) }
						jsonPath("$.point") { value(chargeAmount) }
					}

			}
		}
		// Request 요청 매핑 오류
		`when`("요청이 잘못돼서 예외가 발생했다면") {
			then("400 BAD_REQUEST를 반환한다") {
				// given
				val invalidChargeAmount = "invalid"

				// when & then
				mockMvc.patch("/point/$userId/charge") {
					contentType = MediaType.APPLICATION_JSON
					content = invalidChargeAmount
				}
					.andExpect {
						status { isBadRequest() }
					}
			}
		}
		// 서비스에서 예외 전파
		`when` ("유저 정보가 없다면") {
			then("404 NOT_FOUND와 에러 메시지를 반환한다") {
				// given
				val nonExistentUserId = 999L
				val chargeAmount = 10_000L
				every { pointService.chargePoint(nonExistentUserId, chargeAmount) } throws UserNotFoundException(nonExistentUserId)

				// when & then
				mockMvc.patch("/point/$nonExistentUserId/charge") {
					contentType = MediaType.APPLICATION_JSON
					content = "$chargeAmount"
				}
					.andExpect {
						status { isNotFound() }
						jsonPath("$.code") { value("404") }
						jsonPath("$.message") { value("${nonExistentUserId}는 존재하지 않는 사용자입니다.") }
					}
			}
		}
		`when` ("0원 이하의 금액을 충전하면") {
			then("400 BAD_REQUEST를 반환한다") {
				// given
				val invalidChargeAmount = -100L
				every { pointService.chargePoint(userId, invalidChargeAmount) } throws IllegalArgumentException("Charge amount must be greater than 0")

				// when & then
				mockMvc.patch("/point/$userId/charge") {
					contentType = MediaType.APPLICATION_JSON
					content = invalidChargeAmount
				}
					.andExpect {
						status { isBadRequest() }
					}
			}
		}
		`when` ("충전 시 잔고가 50만원을 초과하면") {
			then("400 BAD_REQUEST를 반환한다") {
				// given
				val excessiveChargeAmount = 600_000L
				every { pointService.chargePoint(userId, excessiveChargeAmount) } throws IllegalArgumentException("Charge amount exceeds limit")

				// when & then
				mockMvc.patch("/point/$userId/charge") {
					contentType = MediaType.APPLICATION_JSON
					content = "$excessiveChargeAmount"
				}
					.andExpect {
						status { isBadRequest() }
					}
			}
		}
	}

	given("PATCH /point/{id}/use 요청") {
		`when` ("요청에 성공한다면") {
			then("200 OK와 포인트 현황을 반환한다") {
				// given
				val userId = 1L
				val useAmount = 5_000L
				val userPoint = UserPoint.create(id = userId, point = 5_000L)

				every { pointService.usePoint(userId, useAmount) } returns userPoint

				// when & then
				mockMvc.patch("/point/$userId/use") {
					contentType = MediaType.APPLICATION_JSON
					content = "$useAmount"
				}
					.andExpect {
						status { isOk() }
						jsonPath("$.id") { value(userId) }
						jsonPath("$.point") { value(5000L) }
					}

			}
		}
		// Request 요청 매핑 오류
		`when`("요청이 잘못돼서 예외가 발생했다면") {
			then("400 BAD_REQUEST와 에러 메시지를 반환한다") {
				// given
				val invalidUseAmount = "invalid"

				// when & then
				mockMvc.patch("/point/$userId/use") {
					contentType = MediaType.APPLICATION_JSON
					content = invalidUseAmount
				}
					.andExpect {
						status { isBadRequest() }
					}
			}
		}
		// 서비스에서 예외 전파
		`when` ("유저 정보가 없다면") {
			then("404 NOT_FOUND와 에러 메시지를 반환한다") {
				// given
				val nonExistentUserId = 999L
				val useAmount = 5_000L

				every { pointService.usePoint(nonExistentUserId, useAmount) } throws UserNotFoundException(nonExistentUserId)

				// when & then
				mockMvc.patch("/point/$nonExistentUserId/use") {
					contentType = MediaType.APPLICATION_JSON
					content = "$useAmount"
				}
					.andExpect {
						status { isNotFound() }
						content { json("""{"code":"404","message":"${nonExistentUserId}는 존재하지 않는 사용자입니다."}""") }
					}
			}
		}
		`when` ("0원 이하의 금액을 사용하면") {
			then("400 BAD_REQUEST와 에러 메시지를 반환한다") {
				// given
				val invalidUseAmount = -100L
				every { pointService.usePoint(userId, invalidUseAmount) } throws IllegalArgumentException("Use amount must be greater than 0")

				// when & then
				mockMvc.patch("/point/$userId/use") {
					contentType = MediaType.APPLICATION_JSON
					content = "$invalidUseAmount"
				}
					.andExpect {
						status { isBadRequest() }
					}
			}
		}
		`when` ("잔고 이상의 금액을 초과하면") {
			then("400 BAD_REQUEST와 에러 메시지를 반환한다") {
				// given
				val excessiveUseAmount = 10_000L
				every { pointService.usePoint(userId, excessiveUseAmount) } throws IllegalArgumentException("Insufficient balance")

				// when & then
				mockMvc.patch("/point/$userId/use") {
					contentType = MediaType.APPLICATION_JSON
					content = "$excessiveUseAmount"
				}
					.andExpect {
						status { isBadRequest() }
					}
			}
		}
	}


	given("GET /point/{id}/histories 요청") {
		`when` ("요청에 성공한다면") {
			then("200 OK와 포인트 현황을 반환한다") {
				// given
				val userId = 1L
				val histories = listOf(
					PointHistory.create(id = 1L, userId = userId, type = TransactionType.CHARGE, amount = 10_000L),
					PointHistory.create(id = 2L, userId = userId, type = TransactionType.USE, amount = 5_000L)
				)

				every { pointService.getUserPointHistory(userId) } returns histories

				// when & then
				mockMvc.get("/point/$userId/histories")
					.andExpect {
						status { isOk() }
						jsonPath("$[0].userId") { value(userId) }
						jsonPath("$[0].type") { value("CHARGE") }
						jsonPath("$[0].amount") { value(10000) }
						jsonPath("$[1].userId") { value(userId) }
						jsonPath("$[1].type") { value("USE") }
						jsonPath("$[1].amount") { value(5000) }
					}

			}
		}
		// Request 요청 매핑 오류
		`when`("요청이 잘못돼서 예외가 발생했다면") {
			then("400 BAD_REQUEST와 에러 메시지를 반환한다") {
				// given
				val invalidUserId = "invalid"

				// when & then
				mockMvc.get("/point/$invalidUserId/histories")
					.andExpect {
						status { isBadRequest() }
					}
			}
		}
		// 서비스에서 예외 전파
		`when` ("유저 정보가 없다면") {
			then("404 NOT_FOUND와 에러 메시지를 반환한다") {
				// given
				val nonExistentUserId = 999L

				every { pointService.getUserPointHistory(nonExistentUserId) } throws UserNotFoundException(nonExistentUserId)

				// when & then
				mockMvc.get("/point/$nonExistentUserId/histories")
					.andExpect {
						status { isNotFound() }
						content { json("""{"code":"404","message":"${nonExistentUserId}는 존재하지 않는 사용자입니다."}""") }
					}
			}
		}
	}

})