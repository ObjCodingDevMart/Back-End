package org.example.devmarketbackend.login.dto.request

import jakarta.validation.constraints.NotBlank

/**
 * 모바일 클라이언트가 재발급을 요청할 때 사용하는 DTO
 */
data class MobileTokenRefreshRequest(
    @field:NotBlank
    val accessToken: String,

    @field:NotBlank
    val refreshToken: String
)

