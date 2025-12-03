package org.example.devmarketbackend.dto.request

data class CartItemUpdateRequest(
    val cartItemId: Long,
    val quantity: Int
)

