package org.example.devmarketbackend.dto.response

import org.example.devmarketbackend.domain.Order
import org.example.devmarketbackend.global.constant.OrderStatus
import java.time.LocalDateTime

data class OrderResponseDto(
    val orderId: Long?,
    val usernickname: String?,
    val item_name: String?,
    val item_brand: String?,
    val item_url: String?,
    val quantity: Int,
    val totalPrice: Int,
    val finalPrice: Int,
    val mileageToUse: Int, //사용한 마일리지
    val status: OrderStatus?,
    val createdAt: LocalDateTime?
) {
    companion object {
        fun from(order: Order): OrderResponseDto {
            return OrderResponseDto(
                order.id,
                order.user?.usernickname,
                order.item?.itemname,
                order.item?.brand,
                order.item?.imagePath,
                order.quantity,
                order.totalPrice,
                order.finalPrice,
                order.totalPrice - order.finalPrice,
                order.status,
                order.createdAt
            )
        }
    }
}

