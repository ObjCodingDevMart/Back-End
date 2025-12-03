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

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: MobileLoginRequest): ApiResponse<AuthTokenResponse> {
        val tokens = loginService.loginWithKakao(request)
        return ApiResponse.onSuccess(SuccessCode.USER_LOGIN_SUCCESS, tokens)
    }

    @PostMapping("/reissue")
    fun reissue(@RequestBody @Valid request: MobileTokenRefreshRequest): ApiResponse<AuthTokenResponse> {
        val tokens = loginService.reissue(request)
        return ApiResponse.onSuccess(SuccessCode.USER_REISSUE_SUCCESS, tokens)
    }
}

