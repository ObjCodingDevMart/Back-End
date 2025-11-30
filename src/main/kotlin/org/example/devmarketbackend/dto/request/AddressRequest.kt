package org.example.devmarketbackend.dto.request

data class AddressRequest(
    val zipcode: String,
    val address: String,
    val addressDetail: String
)

