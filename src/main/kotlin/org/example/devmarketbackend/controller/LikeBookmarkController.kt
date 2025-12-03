package org.example.devmarketbackend.controller

import io.swagger.v3.oas.annotations.Operation
import org.example.devmarketbackend.dto.request.LikeBookmarkRequest
import org.example.devmarketbackend.global.api.ApiResponse
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.api.SuccessCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.login.auth.jwt.CustomUserDetails
import org.example.devmarketbackend.login.service.UserService
import org.example.devmarketbackend.service.LikeBookmarkService
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/likes")
class LikeBookmarkController(
    private val likeBookmarkService: LikeBookmarkService,
    private val userService: UserService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping
    @Operation(summary = "즐겨찾기 등록", description = "현재 사용자의 즐겨찾기를 추가합니다.")
    fun addBookmark(
        @AuthenticationPrincipal customUserDetails: CustomUserDetails,
        @RequestBody request: LikeBookmarkRequest
    ): ApiResponse<*> {
        log.info("[STEP 1] 즐겨찾기 등록 요청: itemId={}", request.itemId)
        return try {
            val user = userService.getAuthenticatedUser(customUserDetails.providerId)
            val response = likeBookmarkService.addBookmark(user, request.itemId)
            log.info("[STEP 2] 즐겨찾기 등록 성공")
            ApiResponse.onSuccess(SuccessCode.LIKEBOOKMARK_ADD_SUCCESS, response)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 즐겨찾기 등록 실패: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    @DeleteMapping
    @Operation(summary = "즐겨찾기 삭제", description = "현재 사용자의 즐겨찾기에서 상품을 제거합니다.")
    fun removeBookmark(
        @AuthenticationPrincipal customUserDetails: CustomUserDetails,
        @RequestBody request: LikeBookmarkRequest
    ): ApiResponse<*> {
        log.info("[STEP 1] 즐겨찾기 삭제 요청: itemId={}", request.itemId)
        return try {
            val user = userService.getAuthenticatedUser(customUserDetails.providerId)
            likeBookmarkService.removeBookmark(user, request.itemId)
            log.info("[STEP 2] 즐겨찾기 삭제 성공")
            ApiResponse.onSuccess(SuccessCode.LIKEBOOKMARK_REMOVE_SUCCESS, null)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 즐겨찾기 삭제 실패: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping
    @Operation(summary = "즐겨찾기 목록 조회", description = "현재 사용자의 즐겨찾기 목록을 가져옵니다.")
    fun listBookmarks(
        @AuthenticationPrincipal customUserDetails: CustomUserDetails
    ): ApiResponse<*> {
        log.info("[STEP 1] 즐겨찾기 목록 조회 요청")
        return try {
            val user = userService.getAuthenticatedUser(customUserDetails.providerId)
            val response = likeBookmarkService.getBookmarks(user)
            log.info("[STEP 2] 즐겨찾기 목록 조회 성공")
            ApiResponse.onSuccess(SuccessCode.LIKEBOOKMARK_LIST_SUCCESS, response)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 즐겨찾기 조회 실패: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }
}

