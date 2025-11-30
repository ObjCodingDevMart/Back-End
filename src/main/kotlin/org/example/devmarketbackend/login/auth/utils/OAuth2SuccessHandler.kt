package org.example.devmarketbackend.login.auth.utils

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.devmarketbackend.domain.Address
import org.example.devmarketbackend.domain.User
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.login.auth.dto.JwtDto
import org.example.devmarketbackend.login.auth.jwt.CustomUserDetails
import org.example.devmarketbackend.login.auth.service.JpaUserDetailsManager
import org.example.devmarketbackend.login.service.UserService
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class OAuth2SuccessHandler(
    private val jpaUserDetailsManager: JpaUserDetailsManager, // Security 사용자 저장/조회 담당
    private val userService: UserService                     // JWT 발급 및 RefreshToken 저장 로직
) : AuthenticationSuccessHandler {

    companion object {
        private val ALLOWED_ORIGINS = listOf(
            "https://coda-likelion.netlify.app",
            "http://localhost:3000"
        )
        private const val DEFAULT_FRONT_ORIGIN = "https://coda-likelion.netlify.app"
    }

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        try {
            // 1️⃣ providerId, nickname 추출
            val oAuth2User = authentication.principal as DefaultOAuth2User
            val providerId = oAuth2User.attributes.getOrDefault("provider_id", oAuth2User.attributes["id"]).toString()
            val nickname = oAuth2User.attributes["nickname"] as? String
            println("// [OAuth2Success] providerId=$providerId, nickname=$nickname")

            // 2️⃣ 신규 회원 등록 여부 확인 후 없으면 생성
            if (!jpaUserDetailsManager.userExists(providerId)) {
                val newUser = User()
                newUser.providerId = providerId
                newUser.usernickname = nickname ?: ""
                newUser.deletable = true

                // 예시 주소 설정 (테스트용)
                newUser.address = Address("10540", "경기도 고양시 덕양구 항공대학로 76", "한국항공대학교")
                println("// 신규 회원 address 확인: ${newUser.address?.address}")

                val userDetails = CustomUserDetails(newUser)
                jpaUserDetailsManager.createUser(userDetails)
                println("// 신규 회원 등록 완료 (provider_id=$providerId)")
            } else {
                println("// 기존 회원 로그인 (provider_id=$providerId)")
            }

            // 3️⃣ JWT 발급 및 RefreshToken 저장
            val jwt = userService.jwtMakeSave(providerId)
            println("// JWT 발급 완료 (provider_id=$providerId)")

            // 4️⃣ 세션에서 redirect origin 회수 후 제거
            var frontendRedirectOrigin = request.session.getAttribute("FRONT_REDIRECT_URI") as? String
            request.session.removeAttribute("FRONT_REDIRECT_URI")

            // 5️⃣ 화이트리스트 재검증
            if (frontendRedirectOrigin == null || !ALLOWED_ORIGINS.contains(frontendRedirectOrigin)) {
                frontendRedirectOrigin = DEFAULT_FRONT_ORIGIN
            }

            // 6️⃣ 최종 리다이렉트 URL 생성 (accessToken 포함)
            val redirectUrl = UriComponentsBuilder
                .fromUriString(frontendRedirectOrigin)
                .queryParam("accessToken", URLEncoder.encode(jwt.accessToken, StandardCharsets.UTF_8))
                .build(true)
                .toUriString()

            println("// [OAuth2Success] Redirecting to $redirectUrl")
            response.sendRedirect(redirectUrl)

        } catch (e: GeneralException) {
            println("// [OAuth2Success] GeneralException: ${e.reason.message}")
            throw e
        } catch (e: Exception) {
            println("// [OAuth2Success] Unexpected Error: ${e.message}")
            e.printStackTrace()
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }
}

