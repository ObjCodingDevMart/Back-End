package org.example.devmarketbackend.login.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.example.devmarketbackend.global.api.ApiResponse
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.api.SuccessCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.login.auth.dto.JwtDto
import org.example.devmarketbackend.login.service.UserService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "회원", description = "회원 관련 API (토큰 재발급, 로그아웃) 입니다.")
@RestController
@RequestMapping("/users")
class UserController(
    // 서비스 계층: 재발급/로그아웃 로직을 모두 위임
    private val userService: UserService
) {

    // ───────────────────────────────────────────────────────────────────
    //  토큰 재발급 API
    //  - Access 토큰이 만료된 경우, DB에 저장된 Refresh 토큰을 검증하여
    //    새로운 Access/Refresh 토큰을 발급한다.
    //  - Access 토큰은 요청 헤더 Authorization에서 읽고, providerId(subject)를 추출한다.
    // ───────────────────────────────────────────────────────────────────
    @Operation(summary = "토큰 재발급", description = "Refresh Token을 이용해 새로운 Access Token을 발급하는 API입니다.")
    @ApiResponses(
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    )
    @PostMapping("/reissue")
    fun reissue(request: HttpServletRequest): ApiResponse<JwtDto> {
        println("[STEP 1] 토큰 재발급 요청 수신")

        return try {
            // 서비스에 위임: 헤더에서 Access 토큰 파싱 → Refresh 검증 → 새 JWT 발급
            val jwt = userService.reissue(request)
            println("[STEP 2] 토큰 재발급 성공 - 새로운 Access/Refresh 반환")
            ApiResponse.onSuccess(SuccessCode.USER_REISSUE_SUCCESS, jwt)

        } catch (e: GeneralException) {
            // 표준 에러코드 기반 예외는 그대로 전파 (ControllerAdvice에서 공통 처리 가능)
            println("[ERROR] 토큰 재발급 중 예외: ${e.reason.message}")
            throw e

        } catch (e: Exception) {
            // 알 수 없는 예외는 내부 서버 에러로 래핑
            println("[ERROR] 예상치 못한 예외: ${e.message}")
            e.printStackTrace()
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    // ───────────────────────────────────────────────────────────────────
    //  로그아웃 API
    //  - 현재 사용자의 Access 토큰에서 providerId(subject)를 읽어
    //    해당 사용자의 Refresh 토큰 레코드를 DB에서 삭제한다(세션 종료 효과).
    //  - Access 토큰 파싱은 서비스에서 수행한다.
    // ───────────────────────────────────────────────────────────────────
    @Operation(summary = "로그아웃", description = "사용자를 로그아웃 처리하는 API입니다.")
    @ApiResponses(
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    )
    @DeleteMapping("/logout")
    fun logout(request: HttpServletRequest): ApiResponse<Void?> {
        // 서비스에 위임: 헤더에서 Access 토큰 → subject 추출 → Refresh 삭제
        userService.logout(request)
        return ApiResponse.onSuccess(SuccessCode.USER_LOGOUT_SUCCESS, null)
    }
}

