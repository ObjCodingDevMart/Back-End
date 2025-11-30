package org.example.devmarketbackend.dto.request

data class OrderCreateRequest(
    val itemId: Long,
    val quantity: Int,
    val mileageToUse: Int
)

