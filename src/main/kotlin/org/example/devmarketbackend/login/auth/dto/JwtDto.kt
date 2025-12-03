package org.example.devmarketbackend.login.auth.dto

data class JwtDto(
    var accessToken: String? = null,
    var refreshToken: String? = null,
    var ttl: Long? = null
)

