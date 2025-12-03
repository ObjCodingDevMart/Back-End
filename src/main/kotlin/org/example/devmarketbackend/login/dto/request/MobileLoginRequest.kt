package org.example.devmarketbackend.login.dto.request

/**
 * 안드로이드 앱에서 전달하는 카카오 로그인 요청 데이터
 */
data class MobileLoginRequest(
    /**
     * 프론트에서 카카오 SDK로 발급받은 액세스 토큰.
     * 입력되면 백엔드가 카카오 `/user/me`로 직접 유저 정보를 조회합니다.
     */
    val kakaoAccessToken: String? = null,

    /**
     * 기존처럼 직접 `providerId`를 포함하는 방식도 지원합니다.
     */
    val providerId: String? = null,

    val userNickname: String? = null,
    val userProfileUrl: String? = null,
    val email: String? = null
)

