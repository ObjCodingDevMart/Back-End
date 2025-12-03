package org.example.devmarketbackend.controller

import io.swagger.v3.oas.annotations.Operation
import org.example.devmarketbackend.dto.request.ReviewCreateRequest
import org.example.devmarketbackend.global.api.ApiResponse
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.api.SuccessCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.login.auth.jwt.CustomUserDetails
import org.example.devmarketbackend.login.service.UserService
import org.example.devmarketbackend.service.ReviewService
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/reviews")
class ReviewController(
    private val reviewService: ReviewService,
    private val userService: UserService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping
    @Operation(summary = "리뷰 등록", description = "로그인된 사용자가 상품에 대한 리뷰를 등록합니다.")
    fun createReview(
        @AuthenticationPrincipal customUserDetails: CustomUserDetails,
        @RequestBody request: ReviewCreateRequest
    ): ApiResponse<*> {
        log.info("[STEP 1] 리뷰 등록 요청: itemId={}", request.itemId)
        return try {
            val user = userService.getAuthenticatedUser(customUserDetails.providerId)
            val response = reviewService.createReview(user, request)
            log.info("[STEP 2] 리뷰 등록 성공")
            ApiResponse.onSuccess(SuccessCode.REVIEW_CREATE_SUCCESS, response)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 리뷰 등록 실패: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/items/{itemId}")
    @Operation(summary = "상품 리뷰 목록", description = "상품별 리뷰 목록을 조회합니다.")
    fun getItemReviews(@PathVariable itemId: Long): ApiResponse<*> {
        log.info("[STEP 1] 상품 리뷰 조회 요청: itemId={}", itemId)
        return try {
            val response = reviewService.getReviewsByItem(itemId)
            log.info("[STEP 2] 상품 리뷰 조회 성공")
            ApiResponse.onSuccess(SuccessCode.REVIEW_ITEM_LIST_SUCCESS, response)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 상품 리뷰 조회 실패: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/me")
    @Operation(summary = "내 리뷰 목록", description = "현재 사용자가 쓴 리뷰 목록을 조회합니다.")
    fun getMyReviews(
        @AuthenticationPrincipal customUserDetails: CustomUserDetails
    ): ApiResponse<*> {
        log.info("[STEP 1] 내 리뷰 목록 조회 요청")
        return try {
            val user = userService.getAuthenticatedUser(customUserDetails.providerId)
            val response = reviewService.getReviewsByUser(user)
            log.info("[STEP 2] 내 리뷰 목록 조회 성공")
            ApiResponse.onSuccess(SuccessCode.REVIEW_USER_LIST_SUCCESS, response)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 내 리뷰 목록 조회 실패: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }
}

