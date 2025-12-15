package org.example.devmarketbackend.login.controller

import jakarta.validation.Valid
import org.example.devmarketbackend.global.api.ApiResponse
import org.example.devmarketbackend.global.api.SuccessCode
import org.example.devmarketbackend.login.dto.request.MobileLoginRequest
import org.example.devmarketbackend.login.dto.request.MobileTokenRefreshRequest
import org.example.devmarketbackend.login.dto.response.AuthTokenResponse
import org.example.devmarketbackend.login.service.LoginService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 모바일 클라이언트를 위한 카카오 로그인 관련 컨트롤러
 */
@RestController
@RequestMapping("/token")
class LoginController(
    private val loginService: LoginService
) {
    private val log = org.slf4j.LoggerFactory.getLogger(javaClass)

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: MobileLoginRequest): ApiResponse<AuthTokenResponse> {
        log.info("[STEP 1] 로그인 요청 수신")
        return try {
            val tokens = loginService.loginWithKakao(request)
            log.info("[STEP 2] 로그인 성공")
            ApiResponse.onSuccess(SuccessCode.USER_LOGIN_SUCCESS, tokens)
        } catch (e: org.example.devmarketbackend.global.exception.GeneralException) {
            log.error("❌ [ERROR] 로그인 실패: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw org.example.devmarketbackend.global.exception.GeneralException.of(org.example.devmarketbackend.global.api.ErrorCode.USER_NOT_AUTHENTICATED)
        }
    }

    @PostMapping("/reissue")
    fun reissue(@RequestBody @Valid request: MobileTokenRefreshRequest): ApiResponse<AuthTokenResponse> {
        log.info("[STEP 1] 토큰 재발급 요청 수신")
        return try {
            val tokens = loginService.reissue(request)
            log.info("[STEP 2] 토큰 재발급 성공")
            ApiResponse.onSuccess(SuccessCode.USER_REISSUE_SUCCESS, tokens)
        } catch (e: org.example.devmarketbackend.global.exception.GeneralException) {
            log.error("❌ [ERROR] 토큰 재발급 실패: {}", e.reason.message)
            throw GeneralException.of(org.example.devmarketbackend.global.api.ErrorCode.USER_NOT_AUTHENTICATED)
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw org.example.devmarketbackend.global.exception.GeneralException.of(org.example.devmarketbackend.global.api.ErrorCode.USER_NOT_AUTHENTICATED)
        }
    }
}

