package org.example.devmarketbackend.login.kakao

import com.fasterxml.jackson.annotation.JsonProperty
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

private const val USER_INFO_URL = "https://kapi.kakao.com/v2/user/me"

@Component
class KakaoClient(
    private val webClient: WebClient
) {

    fun fetchUserInfo(accessToken: String): KakaoUserInfo {
        return try {
            webClient.get()
                .uri(USER_INFO_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                .retrieve()
                .bodyToMono(KakaoUserInfo::class.java)
                .block() ?: throw GeneralException.of(ErrorCode.OAUTH2_PROCESS_FAILED)
        } catch (ex: WebClientResponseException) {
            throw GeneralException.of(ErrorCode.OAUTH2_PROCESS_FAILED)
        }
    }
}

data class KakaoUserInfo(
    val id: Long,
    val properties: KakaoUserProperties?,
    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount?
)

data class KakaoUserProperties(
    val nickname: String?,
    @JsonProperty("profile_image")
    val profileImageUrl: String?
)

data class KakaoAccount(
    val email: String?
)

