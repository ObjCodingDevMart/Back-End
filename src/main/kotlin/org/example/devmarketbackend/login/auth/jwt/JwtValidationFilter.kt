package org.example.devmarketbackend.login.auth.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.devmarketbackend.global.api.ApiResponse
import org.example.devmarketbackend.global.api.ErrorCode
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT 유효성 검사 필터 (provider_id 기반)
 *
 * - 모든 요청마다 1회 실행(OncePerRequestFilter)
 * - Authorization 헤더의 Bearer 토큰을 꺼내 유효성 검증
 * - 유효하면 SecurityContext에 인증(Authentication)을 주입
 * - 불필요한 중복 파싱/중복 인증 주입을 피하기 위해
 *   이미 인증된 요청은 바로 체인 진행
 */
@Component
class JwtValidationFilter(
    private val tokenProvider: TokenProvider
) : OncePerRequestFilter() {

    /**
     * 특정 URL은 본 필터를 건너뛴다.
     * - /users/reissue : Refresh 토큰 재발급 엔드포인트
     *   (설계상 AuthCreationFilter에서 ROLE_ANONYMOUS를 주입해 처리)
     */
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.servletPath == "/users/reissue"
    }

    /**
     * 요청 처리 진입점
     * 1) 이미 인증된 요청이면 패스
     * 2) Authorization 헤더에서 Bearer 토큰 추출
     * 3) 토큰 파싱/검증 → providerId와 권한 복원
     * 4) SecurityContext에 Authentication 주입 후 체인 진행
     * 5) 예외 발생 시 표준 오류 응답(JSON) 반환
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        // [사전 차단] 이미 인증된 요청이면 추가 작업 없이 다음 필터로 진행
        //  - 익명 토큰(AnonymousAuthenticationToken)은 제외
        val existing = SecurityContextHolder.getContext().authentication
        if (existing != null && existing.isAuthenticated && existing !is AnonymousAuthenticationToken) {
            chain.doFilter(request, response)
            return
        }

        // Authorization 헤더 검사 (ex: "Bearer eyJhbGciOi...")
        //  - 헤더가 없거나 Bearer 스킴이 아니면 다음 필터로 넘김(비인증 상태 허용)
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response)
            return
        }

        // "Bearer " 이후의 실제 토큰 문자열 추출
        val token = authHeader.substring(7)

        try {
            // [핵심] 토큰을 한 번만 파싱하여 끝까지 사용 (중복 비용/오류 방지)
            val claims = tokenProvider.parseClaims(token)

            // sub(subject) == providerId (우리 정책)
            val providerId = claims.subject
            if (providerId.isNullOrEmpty()) {
                sendErrorResponse(response, ErrorCode.TOKEN_INVALID)
                return
            }

            // 권한 복원 (없으면 ROLE_USER 기본 부여)
            val authorities = tokenProvider.getAuthFromClaims(claims)

            // Spring Security 표준 인증 토큰 구성
            val userDetails = CustomUserDetails(
                providerId = providerId,
                usernickname = null,
                userId = null,
                address = null
            )
            val authToken = UsernamePasswordAuthenticationToken(userDetails, null, authorities)

            // SecurityContext에 인증 주입 (요청 수명 동안 유효)
            SecurityContextHolder.getContext().authentication = authToken

            // 민감 식별자 마스킹 로그 (운영 로그에 원문 노출 금지)
            val masked = if (providerId.length > 4) providerId.substring(0, 4) + "***" else "***"
            println("JWT 인증 성공 - subject(masked)=$masked")

            // 다음 필터/컨트롤러로 진행
            chain.doFilter(request, response)

        } catch (e: io.jsonwebtoken.security.SecurityException) {
            // 서명 위조/구조 이상
            println("잘못된 서명")
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID)
        } catch (e: MalformedJwtException) {
            println("잘못된 서명")
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID)
        } catch (e: ExpiredJwtException) {
            // 만료(재로그인 또는 재발급 필요)
            println("토큰 만료")
            sendErrorResponse(response, ErrorCode.TOKEN_EXPIRED)
        } catch (e: UnsupportedJwtException) {
            // 지원하지 않는 형식
            println("지원되지 않는 토큰")
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID)
        } catch (e: IllegalArgumentException) {
            // 널/공백 등 잘못된 입력
            println("유효하지 않은 요청")
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID)
        } catch (e: Exception) {
            // 그 외 예기치 못한 오류
            println("JWT 처리 중 알 수 없는 예외: ${e.message}")
            e.printStackTrace()
            sendErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    /**
     * 표준 에러 응답(JSON) 반환 유틸
     * - 상태 코드는 401(UNAUTHORIZED)로 통일
     *   (필요 시 INVALID=401, EXPIRED=401, 기타=500 등으로 세분화 가능)
     */
    private fun sendErrorResponse(response: HttpServletResponse, errorCode: ErrorCode) {
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.writer.write(
            ObjectMapper().writeValueAsString(ApiResponse.onFailure<Any>(errorCode, null))
        )
    }
}

