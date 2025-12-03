package org.example.devmarketbackend.controller

import io.swagger.v3.oas.annotations.Operation
import org.example.devmarketbackend.domain.User
import org.example.devmarketbackend.dto.request.CartItemAddRequest
import org.example.devmarketbackend.dto.request.CartItemRemoveRequest
import org.example.devmarketbackend.dto.request.CartItemUpdateRequest
import org.example.devmarketbackend.global.api.ApiResponse
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.api.SuccessCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.service.CartService
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/cart")
class CartController(
    private val cartService: CartService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private fun resolveUser(user: User?): User {
        return user ?: throw GeneralException.of(ErrorCode.USER_NOT_AUTHENTICATED)
    }

    @GetMapping
    @Operation(summary = "장바구니 조회", description = "로그인된 사용자의 장바구니를 조회합니다.")
    fun getCart(
        @AuthenticationPrincipal user: User?
    ): ApiResponse<*> {
        log.info("[STEP 1] 장바구니 조회 요청 수신")
        return try {
            val currentUser = resolveUser(user)
            val response = cartService.getCart(currentUser)
            log.info("[STEP 2] 장바구니 조회 성공")
            ApiResponse.onSuccess(SuccessCode.CART_GET_SUCCESS, response)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 장바구니 조회 실패: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/items")
    @Operation(summary = "장바구니 상품 추가", description = "상품을 장바구니에 추가합니다.")
    fun addCartItem(
        @AuthenticationPrincipal user: User?,
        @RequestBody request: CartItemAddRequest
    ): ApiResponse<*> {
        log.info("[STEP 1] 장바구니 상품 추가 요청: itemId={}, quantity={}", request.itemId, request.quantity)
        return try {
            val currentUser = resolveUser(user)
            val response = cartService.addItemToCart(currentUser, request.itemId, request.quantity)
            log.info("[STEP 2] 장바구니 상품 추가 성공")
            ApiResponse.onSuccess(SuccessCode.CART_ITEM_ADD_SUCCESS, response)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 장바구니 상품 추가 실패: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    @PutMapping("/items")
    @Operation(summary = "장바구니 상품 수량 변경", description = "장바구니 항목 수량을 수정합니다.")
    fun updateCartItem(
        @AuthenticationPrincipal user: User?,
        @RequestBody request: CartItemUpdateRequest
    ): ApiResponse<*> {
        log.info("[STEP 1] 장바구니 숫자 변경 요청: {}", request.cartItemId)
        return try {
            val currentUser = resolveUser(user)
            val response = cartService.updateCartItemQuantity(currentUser, request.cartItemId, request.quantity)
            log.info("[STEP 2] 장바구니 수량 수정 성공")
            ApiResponse.onSuccess(SuccessCode.CART_ITEM_UPDATE_SUCCESS, response)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 장바구니 수량 수정 실패: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    @DeleteMapping("/items")
    @Operation(summary = "장바구니 상품 삭제", description = "지정한 장바구니 항목을 삭제합니다.")
    fun removeCartItem(
        @AuthenticationPrincipal user: User?,
        @RequestBody request: CartItemRemoveRequest
    ): ApiResponse<*> {
        log.info("[STEP 1] 장바구니 항목 삭제 요청: {}", request.cartItemId)
        return try {
            val currentUser = resolveUser(user)
            val response = cartService.removeCartItem(currentUser, request.cartItemId)
            log.info("[STEP 2] 장바구니 항목 삭제 성공")
            ApiResponse.onSuccess(SuccessCode.CART_ITEM_REMOVE_SUCCESS, response)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 장바구니 항목 삭제 실패: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    @DeleteMapping
    @Operation(summary = "장바구니 초기화", description = "모든 장바구니 항목을 제거합니다.")
    fun clearCart(
        @AuthenticationPrincipal user: User?
    ): ApiResponse<*> {
        log.info("[STEP 1] 장바구니 초기화 요청 수신")
        return try {
            val currentUser = resolveUser(user)
            cartService.clearCart(currentUser)
            log.info("[STEP 2] 장바구니 초기화 성공")
            ApiResponse.onSuccess(SuccessCode.CART_CLEAR_SUCCESS, null)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 장바구니 초기화 실패: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }
}

