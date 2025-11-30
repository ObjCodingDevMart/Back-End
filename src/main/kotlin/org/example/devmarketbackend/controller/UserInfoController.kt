package org.example.devmarketbackend.controller

import io.swagger.v3.oas.annotations.Operation
import org.example.devmarketbackend.domain.User
import org.example.devmarketbackend.dto.request.UserInfoFixRequest
import org.example.devmarketbackend.dto.response.UserInfoResponse
import org.example.devmarketbackend.dto.response.UserMilegeResponse
import org.example.devmarketbackend.global.api.ApiResponse
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.api.SuccessCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.login.auth.jwt.CustomUserDetails
import org.example.devmarketbackend.login.service.UserService
import org.example.devmarketbackend.service.UserInfoService
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user/me")
class UserInfoController(
    private val userService: UserService,
    private val userInfoService: UserInfoService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    //내 정보 조회
    @GetMapping
    @Operation(summary = "내 정보 조회", description = "내 정보를 조회합니다.")
    fun getUserInfo(
        @AuthenticationPrincipal customUserDetails: CustomUserDetails
    ): ApiResponse<*> {
        log.info("[STEP 1] 내 정보 조회 요청 수신")
        return try {
            val user = userService.findByProviderId(customUserDetails.providerId)
                .orElseThrow { GeneralException.of(ErrorCode.USER_NOT_FOUND) }

            val userInfoResponse = userInfoService.getUserInfo(user)
            log.info("[STEP 2] 내 정보 조회 성공")
            ApiResponse.onSuccess(SuccessCode.USER_INFO_GET_SUCCESS, userInfoResponse)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 내정보 조회 중 예외 발생: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    // 내 정보 수정
    @PutMapping
    @Operation(summary = "내 정보 수정", description = "내 정보를 수정합니다.")
    fun updateUserInfo(
        @AuthenticationPrincipal customUserDetails: CustomUserDetails,
        @RequestBody request: UserInfoFixRequest
    ): ApiResponse<*> {
        log.info("[STEP 1] 내 정보 수정 요청 수신..")
        return try {
            val user = userService.findByProviderId(customUserDetails.providerId)
                .orElseThrow { GeneralException.of(ErrorCode.USER_NOT_FOUND) }
            val fixedUser = userInfoService.fixUserInfo(user, request)
            log.info("[STEP 2] 내 정보 수정 성공 ..")
            ApiResponse.onSuccess(SuccessCode.OK, fixedUser)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 내 정보 수정 중 예외 발생: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    //내 마일리지 조회
    @GetMapping("/mileage")
    @Operation(summary = "내 마일리지 조회", description = "현제 나의 마일리지를 조회합니다.")
    fun getUserMileage(
        @AuthenticationPrincipal customUserDetails: CustomUserDetails
    ): ApiResponse<*> {
        log.info("[STEP 1] 내 마일리지 조회 요청 수신..")
        return try {
            val user = userService.findByProviderId(customUserDetails.providerId)
                .orElseThrow { GeneralException.of(ErrorCode.USER_NOT_FOUND) }
            val myMilage = userInfoService.getUserMileges(user)
            log.info("[STEP 2]  내 마일리지 조회 성공 ..")
            ApiResponse.onSuccess(SuccessCode.OK, myMilage)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 내 마일리지 조회 중 예외 발생: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }
}
// getmapping request 매핑을 통해 api 호출
// operation 을 통해 swagger 문서화

