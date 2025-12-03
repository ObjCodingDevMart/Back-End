package org.example.devmarketbackend.login.auth.controller

import io.swagger.v3.oas.annotations.Operation
import org.example.devmarketbackend.dto.request.MobileLoginRequest
import org.example.devmarketbackend.dto.request.MobileTokenRefreshRequest
import org.example.devmarketbackend.global.api.ApiResponse
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.api.SuccessCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.login.auth.dto.JwtDto
import org.example.devmarketbackend.login.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mobile/auth")
class MobileAuthController(
    private val userService: UserService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/login")
    @Operation(summary = "모바일 로그인", description = "모바일에서 providerId로 Login 처리를 하고 JWT를 반환합니다.")
    fun mobileLogin(@RequestBody request: MobileLoginRequest): ApiResponse<JwtDto> {
        log.info("[STEP 1] 모바일 로그인 요청 received")
        if (request.providerId.isBlank()) {
            log.warn("[STEP 1] providerId 누락")
            throw GeneralException.of(ErrorCode.TOKEN_INVALID)
        }
        return try {
            val jwt = userService.jwtMakeSave(request.providerId)
            log.info("[STEP 2] 모바일 로그인 토큰 발급 성공")
            ApiResponse.onSuccess(SuccessCode.USER_LOGIN_SUCCESS, jwt)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 모바일 로그인 실패: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "모바일 토큰 재발급", description = "Access/Refresh 토큰을 보내면 새 토큰을 발급합니다.")
    fun refreshToken(@RequestBody request: MobileTokenRefreshRequest): ApiResponse<JwtDto> {
        log.info("[STEP 1] 모바일 토큰 재발급 요청")
        if (request.accessToken.isBlank() || request.refreshToken.isBlank()) {
            log.warn("[STEP 1] 토큰 값이 비어있습니다.")
            throw GeneralException.of(ErrorCode.TOKEN_INVALID)
        }
        return try {
            val jwt = userService.reissue(request.accessToken, request.refreshToken)
            log.info("[STEP 2] 모바일 토큰 재발급 성공")
            ApiResponse.onSuccess(SuccessCode.USER_REISSUE_SUCCESS, jwt)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 모바일 토큰 재발급 실패: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }
}

