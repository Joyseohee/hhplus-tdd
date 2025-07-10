package io.hhplus.tdd

import io.hhplus.tdd.point.PointServiceException
import io.hhplus.tdd.point.PointTransactionFailedException
import io.hhplus.tdd.point.UserNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.lang.reflect.InvocationTargetException

data class ErrorResponse(val code: String, val message: String?)

@RestControllerAdvice
class ApiControllerAdvice : ResponseEntityExceptionHandler() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(PointServiceException::class)
    fun handlePointServiceException(e: PointServiceException): ResponseEntity<ErrorResponse> {
        val status = when (e) {
            is UserNotFoundException -> HttpStatus.NOT_FOUND
            is PointTransactionFailedException -> HttpStatus.INTERNAL_SERVER_ERROR
        }

        return ResponseEntity(
            ErrorResponse("${status.value()}", e.message),
            status,
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("400", e.message),
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("400", "요청 파라미터 타입이 올바르지 않습니다."),
            HttpStatus.BAD_REQUEST
        )
    }


    @ExceptionHandler(InvocationTargetException::class)
    fun handleInvocationTargetException(e: InvocationTargetException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("400", "요청 본문이 올바르지 않습니다."),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse("500", "에러가 발생했습니다."),
            HttpStatus.INTERNAL_SERVER_ERROR,
        )
    }
}