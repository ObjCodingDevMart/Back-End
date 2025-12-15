package org.example.devmarketbackend.service

import org.springframework.transaction.annotation.Transactional
import org.example.devmarketbackend.dto.request.OrderCreateRequest
import org.example.devmarketbackend.dto.response.OrderResponseDto
import org.example.devmarketbackend.domain.Item
import org.example.devmarketbackend.domain.Order
import org.example.devmarketbackend.domain.User
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.constant.OrderStatus
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.repository.ItemRepository
import org.example.devmarketbackend.repository.OrderRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val userRepository: org.example.devmarketbackend.repository.UserRepository,
    private val itemRepository: ItemRepository
) {
    //마일리지 적용 후 가격에 대한 로직
    private fun calculateFinalPrice(totalPrice: Int, mileageToUse: Int): Int {
        // 사용 가능한 최대 마일리지
        val availableMileage = minOf(mileageToUse, totalPrice)
        // 최종 결제 금액
        val finalPrice = totalPrice - availableMileage
        return maxOf(finalPrice, 0)  // 최소 결제 금액 0원 보장
    }

    // 주문 생성
    @Transactional
    fun createOrder(request: OrderCreateRequest, user: User): OrderResponseDto {
        // User를 managed 상태로 조회
        val managedUser = resolveUser(user)
        
        // 상품 조회
        val item = itemRepository.findById(request.itemId)
            .orElseThrow { GeneralException.of(ErrorCode.ITEM_NOT_FOUND) }

        // 총 주문 금액 계산
        val totalPrice = item.price * request.quantity
        // 마일리지 유효성 검사
        val mileageToUse = request.mileageToUse
        if (mileageToUse > managedUser.maxMileage) {
            throw GeneralException.of(ErrorCode.INVALID_MILEAGE)
        }

        // 최종 금액 계산
        val finalPrice = calculateFinalPrice(totalPrice, mileageToUse) // 최종 결제 금액

        //주문 생성과 동시에 배송 중으로 설정
        val order = Order(managedUser, item, request.quantity)
        order.totalPrice = totalPrice
        order.finalPrice = finalPrice
        order.status = OrderStatus.PROCESSING
        //사용자 마일리지 처리
        managedUser.useMileage(mileageToUse)
        managedUser.addMileage((finalPrice * 0.1).toInt())//결제 금액의 10% 마일리지 적립
        //최근 결제 금액 업데이트
        managedUser.updateRecentTotal(finalPrice)
        //주문 저장 (User 변경사항은 @Transactional에 의해 자동 저장됨)
        orderRepository.save(order)

        return OrderResponseDto.from(order)
    }
    
    private fun resolveUser(user: User): User {
        val userId = user.id
        return if (userId != null) {
            userRepository.findById(userId)
                .orElseThrow { GeneralException.of(ErrorCode.USER_NOT_FOUND) }
        } else {
            throw GeneralException.of(ErrorCode.USER_NOT_FOUND)
        }
    }

    //개별 주문 조회
    @Transactional
    fun getOrderById(orderId: Long): OrderResponseDto {
        return orderRepository.findById(orderId)
            .map { OrderResponseDto.from(it) }
            .orElseThrow { GeneralException.of(ErrorCode.ORDER_NOT_FOUND) }
    }

    //사용자의 모든 주문 조회
    @Transactional
    fun getAllOrders(user: User): List<OrderResponseDto> {
        //프록시 객체 -> DTO로 변환 후 반환
        return user.orders.map { OrderResponseDto.from(it) }
    }

    //삭제가 아니라 주문 상태만 변경
    //배송 완료된 상품, 주문 취소된 상품은 주문 취소 불가능
    @Transactional
    fun cancelOrder(orderId: Long) {
        val order = orderRepository.findById(orderId)
            .orElseThrow { GeneralException.of(ErrorCode.ORDER_NOT_FOUND) }

        if (order.status == OrderStatus.COMPLETE || order.status == OrderStatus.CANCEL) {
            throw GeneralException.of(ErrorCode.ORDER_CANCEL_FAILED)
        }

        val user = order.user ?: throw GeneralException.of(ErrorCode.USER_NOT_FOUND)
        // User를 managed 상태로 조회
        val managedUser = resolveUser(user)
        
        // 회수해야할 마일리지보다 가지고 있는 마일리지가 적을 경우
        if (managedUser.maxMileage < (order.finalPrice * 0.1).toInt()) {
            throw GeneralException.of(ErrorCode.INVALID_MILEAGE)
        }
        //주문 상태 변경
        order.status = OrderStatus.CANCEL
        // 결제 시에 적립되었던 마일리지 차감 ( 결제 금액의 10%)
        managedUser.useMileage((order.finalPrice * 0.1).toInt())

        //마일리지 환불
        managedUser.addMileage(order.totalPrice - order.finalPrice)

        // 주문 취소 시, 해당 주문의 총 결제 금액 차감
        managedUser.updateRecentTotal(-order.totalPrice)
    }

    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    @Transactional
    fun updateOrderStatus() {
        // PROCESSING 상태면서 1 시간 이전에 생성된 주문 찾는 메서드
        val orders = orderRepository.findByStatusAndCreatedAtBefore(
            OrderStatus.PROCESSING,
            LocalDateTime.now().minusMinutes(1)
        )

        // 주문 상태를 'COMPLETE' 로 변경
        orders.forEach { it.status = OrderStatus.COMPLETE }
    }
}

