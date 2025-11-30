package org.example.devmarketbackend.global.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.example.devmarketbackend.global.api.ApiResponse
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.api.ReasonDto
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

// 전역 예외 처리
@RestControllerAdvice(annotations = [RestController::class])
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    //ConstraintViolationException
    @ExceptionHandler
    fun validation(e: ConstraintViolationException, request: WebRequest): ResponseEntity<Any> {
        val errorMessage = e.constraintViolations.stream()
            .map { it.message }
            .findFirst()
            .orElse("ConstraintViolationException 처리 중 에러 발생")
        return handleExceptionInternalConstraint(e, ErrorCode.BAD_REQUEST, HttpHeaders.EMPTY, request)
    }

    //GeneralException
    @ExceptionHandler(value = [GeneralException::class])
    fun onThrowException(
        generalException: GeneralException,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val reason = generalException.reason
        return handleExceptionInternal(generalException, reason, null, request)
    }

    // MethodArgumentNotValidException
    override fun handleMethodArgumentNotValid(
        e: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        val errors = LinkedHashMap<String, String>()
        e.bindingResult.fieldErrors.forEach { fieldError ->
            val fieldName = fieldError.field
            val errorMessage = fieldError.defaultMessage ?: ""
            errors[fieldName] = errorMessage
        }
        return handleExceptionInternalArgs(e, HttpHeaders.EMPTY, ErrorCode.BAD_REQUEST, request, errors)
    }

    // Exception
    @ExceptionHandler
    fun exception(e: Exception, request: WebRequest): ResponseEntity<Any> {
        e.printStackTrace()
        return handleExceptionInternalFalse(
            e,
            ErrorCode.INTERNAL_SERVER_ERROR,
            HttpHeaders.EMPTY,
            ErrorCode.INTERNAL_SERVER_ERROR.httpStatus,
            request,
            e.message
        )
    }

    // 공통 에러 응답
    private fun handleExceptionInternal(
        e: Exception,
        reason: ReasonDto,
        headers: HttpHeaders?,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val body = ApiResponse.onFailure<Any>(reason, null)
        val webRequest = ServletWebRequest(request)
        return super.handleExceptionInternal(e, body, headers ?: HttpHeaders.EMPTY, reason.httpStatus, webRequest) ?: ResponseEntity.status(reason.httpStatus).body(body)
    }

    private fun handleExceptionInternalFalse(
        e: Exception,
        errorCode: ErrorCode,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest,
        errorPoint: String?
    ): ResponseEntity<Any> {
        val body = ApiResponse.onFailure<Any>(errorCode, errorPoint)
        return super.handleExceptionInternal(e, body, headers, status, request) ?: ResponseEntity.status(status).body(body)
    }

    private fun handleExceptionInternalArgs(
        e: Exception,
        headers: HttpHeaders,
        errorCode: ErrorCode,
        request: WebRequest,
        errorArgs: Map<String, String>
    ): ResponseEntity<Any> {
        val body = ApiResponse.onFailure<Any>(errorCode, errorArgs)
        return super.handleExceptionInternal(e, body, headers, errorCode.httpStatus, request) ?: ResponseEntity.status(errorCode.httpStatus).body(body)
    }

    private fun handleExceptionInternalConstraint(
        e: Exception,
        errorCode: ErrorCode,
        headers: HttpHeaders,
        request: WebRequest
    ): ResponseEntity<Any> {
        val body = ApiResponse.onFailure<Any>(errorCode, null)
        return super.handleExceptionInternal(e, body, headers, errorCode.httpStatus, request) ?: ResponseEntity.status(errorCode.httpStatus).body(body)
    }
}

