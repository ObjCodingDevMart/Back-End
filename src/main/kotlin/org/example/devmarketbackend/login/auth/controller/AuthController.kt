package org.example.devmarketbackend.login.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import java.net.URI

@Tag(name = "인증", description = "OAuth2 로그인 시작 API")
@RestController
@RequestMapping("/oauth2")
class AuthController {

    companion object {
        private val ALLOWED_ORIGINS = setOf(
            "https://coda-likelion.netlify.app",
            "http://localhost:3000"
        )
        private const val DEFAULT_FRONT_ORIGIN = "https://coda-likelion.netlify.app"
    }

    @Operation(summary = "카카오 로그인 시작", description = "redirect_uri를 검증·저장 후 카카오 인가로 리다이렉트합니다.")
    @GetMapping("/start/kakao")
    fun startKakao(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @RequestParam(name = "redirect_uri", required = false) redirectUri: String?
    ) {
        // // 허용 Origin만 통과(https?://host[:port])
        val safe = pickSafeOrigin(redirectUri, ALLOWED_ORIGINS, DEFAULT_FRONT_ORIGIN)
        request.getSession(true).setAttribute("FRONT_REDIRECT_URI", safe) // // 성공 핸들러에서 회수
        response.sendRedirect("/oauth2/authorization/kakao")              // // 시큐리티 기본 인가 엔드포인트
    }

    private fun pickSafeOrigin(url: String?, allowed: Set<String>, fallback: String): String {
        return try {
            if (url.isNullOrBlank()) return fallback
            val u = URI.create(url)
            val origin = "${u.scheme}://${u.host}${if (u.port == -1) "" else ":${u.port}"}"
            if (allowed.contains(origin)) origin else fallback
        } catch (e: Exception) {
            fallback
        }
    }
}

