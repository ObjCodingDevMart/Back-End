package org.example.devmarketbackend.dto.response

import org.example.devmarketbackend.domain.CartItem
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException

data class CartItemResponse(
    val cartItemId: Long?,
    val item: ItemResponseDto,
    val quantity: Int,
    val unitPrice: Int,
    val totalPrice: Int
) {
    companion object {
        fun from(cartItem: CartItem): CartItemResponse {
            val item = cartItem.item ?: throw GeneralException.of(ErrorCode.ITEM_NOT_FOUND)
            return CartItemResponse(
                cartItem.id,
                ItemResponseDto.from(item),
                cartItem.quantity,
                cartItem.unitPrice,
                cartItem.totalPrice()
            )
        }
    }
}

