package org.example.devmarketbackend.login.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "UserReqDto")
data class UserReqDto(
    @Schema(description = "이메일")
    val email: String? = null,

    @Schema(description = "id(username)")
    val username: String? = null,

    @Schema(description = "social type")
    val providerId: String? = null
)

