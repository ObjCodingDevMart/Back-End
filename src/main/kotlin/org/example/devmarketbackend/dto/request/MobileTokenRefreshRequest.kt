package org.example.devmarketbackend.dto.request

data class MobileTokenRefreshRequest(
    val accessToken: String,
    val refreshToken: String
)

