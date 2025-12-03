package org.example.devmarketbackend.login.dto.response

/**
 * 인증 토큰을 클라이언트로 전달할 때 쓰는 응답 모델
 */
data class AuthTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long
)

