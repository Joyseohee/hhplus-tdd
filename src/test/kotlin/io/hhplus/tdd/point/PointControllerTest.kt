package io.hhplus.tdd.point

import io.hhplus.tdd.ApiControllerAdvice
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearMocks
import io.mockk.mockk
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
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
			}
		}
		// Request 요청 매핑 오류
		`when`("요청이 잘못돼서 예외가 발생했다면") {
			then("400 BAD_REQUEST와 에러 메시지를 반환한다") {
			}
		}
		// 서비스에서 예외 전파
		`when` ("유저 정보가 없다면") {
			then("404 NOT_FOUND와 에러 메시지를 반환한다") {
			}
		}
	}

	given("PATCH /point/{id}/charge 요청") {
		`when` ("요청에 성공한다면") {
			then("200 OK와 포인트 현황을 반환한다") {

			}
		}
		// Request 요청 매핑 오류
		`when`("요청이 잘못돼서 예외가 발생했다면") {
			then("400 BAD_REQUEST를 반환한다") {

			}
		}
		// 서비스에서 예외 전파
		`when` ("유저 정보가 없다면") {
			then("404 NOT_FOUND와 에러 메시지를 반환한다") {

			}
		}
		`when` ("0원 이하의 금액을 충전하면") {
			then("400 BAD_REQUEST를 반환한다") {

			}
		}
		`when` ("충전 시 잔고가 50만원을 초과하면") {
			then("400 BAD_REQUEST를 반환한다") {

			}
		}
	}

	given("PATCH /point/{id}/use 요청") {
		`when` ("요청에 성공한다면") {
			then("200 OK와 포인트 현황을 반환한다") {

			}
		}
		// Request 요청 매핑 오류
		`when`("요청이 잘못돼서 예외가 발생했다면") {
			then("400 BAD_REQUEST와 에러 메시지를 반환한다") {

			}
		}
		// 서비스에서 예외 전파
		`when` ("유저 정보가 없다면") {
			then("404 NOT_FOUND와 에러 메시지를 반환한다") {

			}
		}
		`when` ("0원 이하의 금액을 사용하면") {
			then("400 BAD_REQUEST와 에러 메시지를 반환한다") {

			}
		}
		`when` ("잔고 이상의 금액을 초과하면") {
			then("400 BAD_REQUEST와 에러 메시지를 반환한다") {

			}
		}
	}


	given("GET /point/{id}/histories 요청") {
		`when` ("요청에 성공한다면") {
			then("200 OK와 포인트 현황을 반환한다") {

			}
		}
		// Request 요청 매핑 오류
		`when`("요청이 잘못돼서 예외가 발생했다면") {
			then("400 BAD_REQUEST와 에러 메시지를 반환한다") {

			}
		}
		// 서비스에서 예외 전파
		`when` ("유저 정보가 없다면") {
			then("404 NOT_FOUND와 에러 메시지를 반환한다") {

			}
		}
	}

})