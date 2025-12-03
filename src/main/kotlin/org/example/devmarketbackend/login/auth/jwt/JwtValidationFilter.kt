package org.example.devmarketbackend.login.auth.jwt

import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.repository.UserRepository
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT 토큰을 꺼내 인증 컨텍스트에 반영하는 필터
 */
@Component
class JwtValidationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val accessToken = resolveToken(request)
            if (!accessToken.isNullOrBlank() && jwtTokenProvider.validateToken(accessToken)) {
                val providerId = jwtTokenProvider.getSubject(accessToken)
                val user = userRepository.findByProviderId(providerId)
                if (user.isPresent) {
                    val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
                    val authentication = UsernamePasswordAuthenticationToken(user.get(), null, authorities)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        } catch (ex: JwtException) {
            throw GeneralException.of(ErrorCode.TOKEN_INVALID)
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearer = request.getHeader("Authorization") ?: return null
        return if (bearer.startsWith("Bearer ")) bearer.substring(7) else null
    }
}

