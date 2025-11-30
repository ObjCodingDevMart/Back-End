package org.example.devmarketbackend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.devmarketbackend.domain.User
import org.example.devmarketbackend.dto.request.OrderCreateRequest
import org.example.devmarketbackend.dto.response.OrderResponseDto
import org.example.devmarketbackend.global.api.ApiResponse
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.api.SuccessCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.login.auth.jwt.CustomUserDetails
import org.example.devmarketbackend.login.service.UserService
import org.example.devmarketbackend.service.OrderService
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/orders")
@Tag(name = "주문생성", description = "로그인한 사용자의 주문을 생성합니다.")
class OrderController(
    private val orderService: OrderService,
    private val userService: UserService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // 주문 생성
    @PostMapping
    @Operation(summary = "주문 생성", description = "로그인한 사용자의 주문을 생성합니다.")
    fun createOrder(
        @AuthenticationPrincipal customUserDetails: CustomUserDetails,
        @RequestBody request: OrderCreateRequest
    ): ApiResponse<*> {
        val user = userService.findByProviderId(customUserDetails.providerId)
            .orElseThrow { GeneralException.of(ErrorCode.USER_NOT_FOUND) }

        log.info("[STEP 1] 주문 생성 요청 수신...")
        return try {
            val newOrder = orderService.createOrder(request, user)
            log.info("[STEP 2] 주문 생성 성공")
            ApiResponse.onSuccess(SuccessCode.ORDER_CREATE_SUCCESS, newOrder)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 주문 생성 중 예외 발생: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    //개별 주문 조회
    @GetMapping("/{orderId}")
    @Operation(summary = "주문 개별 조회", description = "로그인한 사용자의 주문을 개별 조회합니다.")
    fun deleteOrderById(@PathVariable orderId: Long): ApiResponse<*> {
        log.info("[STEP 1] 개별 주문 조회 요청 수신...")

        return try {
            val order = orderService.getOrderById(orderId)
            log.info("[STEP 2] 개별 주문 조회 성공")
            ApiResponse.onSuccess(SuccessCode.ORDER_GET_SUCCESS, order)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 개별 주문 조회 중 예외 발생: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    //모든 주문 목록 조회
    @GetMapping
    @Operation(summary = "모든 주문 조회", description = "로그인한 사용자의 모든 주문을 목록으로 조회합니다.")
    fun getAllOrders(
        @AuthenticationPrincipal customUserDetails: CustomUserDetails
    ): ApiResponse<*> {
        val user = userService.findByProviderId(customUserDetails.providerId)
            .orElseThrow { GeneralException.of(ErrorCode.USER_NOT_FOUND) }
        val orders = orderService.getAllOrders(user)
        /*if (orders.isEmpty()) {
            return ApiResponse.onFailure(
                    ErrorCode.ORDER_NOT_FOUND,
                    "등록된 주문이 없습니다.");}
        return ApiResponse.onSuccess(SuccessCode.ORDER_LIST_SUCCESS,orders);*/

        // 주문이 없더라도 성공 응답 + 빈 리스트 반환
        return if (orders.isEmpty()) {
            ApiResponse.onSuccess(SuccessCode.ORDER_LIST_EMPTY, emptyList<OrderResponseDto>())
        } else {
            ApiResponse.onSuccess(SuccessCode.ORDER_LIST_SUCCESS, orders)
        }
    }

    //주문 취소
    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "주문 취소", description = "로그인한 사용자의 주문을 취소합니다.")
    fun cancelOrder(@PathVariable orderId: Long): ApiResponse<*> {
        log.info("[STEP 1] 주문 취소 요청 수신")

        return try {
            orderService.cancelOrder(orderId) // ❌ boolean X → ✅ void로 바뀐 메서드
            log.info("[STEP 2] 주문 취소 성공")
            ApiResponse.onSuccess(SuccessCode.ORDER_CANCEL_SUCCESS, "주문이 성공적으로 취소되었습니다.")
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 주문 취소 중 예외 발생: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }
}

