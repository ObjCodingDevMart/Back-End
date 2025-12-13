package org.example.devmarketbackend.login.dto.request

import jakarta.validation.constraints.NotBlank

/**
 * 안드로이드 앱에서 전달하는 카카오 로그인 요청 데이터
 */
data class MobileLoginRequest(
    /**
     * 프론트에서 카카오 SDK로 발급받은 액세스 토큰.
     * 백엔드가 카카오 `/user/me`로 직접 유저 정보를 조회합니다.
     */
    @field:NotBlank(message = "카카오 액세스 토큰은 필수입니다.")
    val kakaoAccessToken: String
)

