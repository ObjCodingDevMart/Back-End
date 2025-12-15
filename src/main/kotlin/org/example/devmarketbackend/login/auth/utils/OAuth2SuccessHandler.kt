package org.example.devmarketbackend.login.auth.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.devmarketbackend.global.api.ApiResponse
import org.example.devmarketbackend.global.api.SuccessCode
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.login.service.LoginService
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.stereotype.Component
import java.io.IOException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * OAuth2 로그인 성공 시 실행되어 토큰을 내려주는 핸들러
 */
@Component
class OAuth2SuccessHandler(
    private val loginService: LoginService,
    private val objectMapper: ObjectMapper
) : AuthenticationSuccessHandler {

    @Throws(IOException::class)
    override fun onAuthenticationSuccess(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication) {
        try {
            val oAuth2User = authentication.principal as? DefaultOAuth2User
                ?: throw GeneralException.of(ErrorCode.USER_NOT_AUTHENTICATED)

            val providerId = oAuth2User.attributes["id"]?.toString()
                ?: throw GeneralException.of(ErrorCode.USER_NOT_AUTHENTICATED)

            val properties = oAuth2User.attributes["properties"] as? Map<*, *> ?: emptyMap<Any, Any>()
            val kakaoAccount = oAuth2User.attributes["kakao_account"] as? Map<*, *>

            val nickname = properties["nickname"]?.toString() ?: "KakaoUser"
            val profileUrl = properties["profile_image"]?.toString()
            val email = kakaoAccount?.get("email")?.toString()

            val tokens = loginService.loginWithOAuth2UserInfo(
                providerId = providerId,
                nickname = nickname,
                profileUrl = profileUrl,
                email = email
            )

            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write(
                objectMapper.writeValueAsString(
                    ApiResponse.onSuccess(SuccessCode.USER_LOGIN_SUCCESS, tokens)
                )
            )
        } catch (e: GeneralException) {
            response.status = ErrorCode.USER_NOT_AUTHENTICATED.httpStatus.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write(
                objectMapper.writeValueAsString(
                    ApiResponse.onFailure<Any>(ErrorCode.USER_NOT_AUTHENTICATED, null)
                )
            )
        } catch (e: Exception) {
            response.status = ErrorCode.USER_NOT_AUTHENTICATED.httpStatus.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write(
                objectMapper.writeValueAsString(
                    ApiResponse.onFailure<Any>(ErrorCode.USER_NOT_AUTHENTICATED, null)
                )
            )
        }
    }
}

