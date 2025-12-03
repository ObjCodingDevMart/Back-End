package org.example.devmarketbackend.controller

import io.swagger.v3.oas.annotations.Operation
import org.example.devmarketbackend.domain.User
import org.example.devmarketbackend.dto.request.AddressRequest
import org.example.devmarketbackend.dto.response.AddressResponse
import org.example.devmarketbackend.global.api.ApiResponse
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.api.SuccessCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.service.UserAddressService
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users/me/address")
class UserAddressController(
    private val userAddressService: UserAddressService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private fun resolveUser(user: User?): User {
        return user ?: throw GeneralException.of(ErrorCode.USER_NOT_AUTHENTICATED)
    }

    //내 주소 조회
    @GetMapping
    @Operation(summary = "내 주소 조회", description = "내 주소를 조회합니다.")
    fun getAddress(
        @AuthenticationPrincipal user: User?
    ): ApiResponse<*> {
        log.info("[STEP 1] 내 주소 조회 요청 수신 ..")
        return try {
            val currentUser = resolveUser(user)
            val myAddress = userAddressService.getAddress(currentUser)
            log.info("[STEP 2] 내 주소 조회 성공")
            ApiResponse.onSuccess(SuccessCode.ADDRESS_GET_SUCCESS, myAddress)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 내 주소 조회 중 예외 발생: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    //내 주소 수정
    @PutMapping
    @Operation(summary = "내 주소 수정", description = "내 주소를 수정 합니다.")
    fun updateAddress(
        @AuthenticationPrincipal user: User?,
        @RequestBody request: AddressRequest
    ): ApiResponse<*> {
        log.info("[STEP 1] 내 주소 수정 요청 수신 ..")
        return try {
            val currentUser = resolveUser(user)
            val myAddress = userAddressService.fixAddress(currentUser, request)
            log.info("[STEP 2] 내 주소 수정 성공")
            ApiResponse.onSuccess(SuccessCode.ADDRESS_SAVE_SUCCESS, myAddress)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 내 주소 수정 중 예외 발생: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }
}
//try catch를 활용하여 일관적인 오류 및 성공시 apiresponse 형태로 api구현
// 유저 서비스에서 가져온 유저 객체를 활용하여 주소 정보 조회

