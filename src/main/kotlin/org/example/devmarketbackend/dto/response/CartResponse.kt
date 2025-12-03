package org.example.devmarketbackend.dto.response

import org.example.devmarketbackend.domain.Cart

data class CartResponse(
    val cartId: Long?,
    val items: List<CartItemResponse>,
    val totalAmount: Int
) {
    companion object {
        fun from(cart: Cart): CartResponse {
            return CartResponse(
                cart.id,
                cart.items.map { CartItemResponse.from(it) },
                cart.totalAmount()
            )
        }
    }
}

