package org.example.devmarketbackend.login.auth.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.login.auth.dto.JwtDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

/**
 * TokenProvider
 * - JWT(Access/Refresh) 생성·검증·파싱을 담당
 *
 * 토큰 설계:
 * - subject(sub): providerId(카카오 고유 ID)를 저장
 * - iat(발급시각), exp(만료시각) 기본 포함
 * - authorities(문자열): Access Token에만 포함, Refresh Token에는 포함하지 않음
 *
 * 서명 알고리즘:
 * - HS256 (대칭키) 사용
 * - secretKey는 32바이트(256비트) 이상이 권장됨 (jjwt Keys.hmacShaKeyFor 요구사항)
 *   예: 환경변수에 충분히 긴 랜덤 바이트를 Base64로 넣어 사용
 */
@Component
class TokenProvider(
    @Value("\${JWT_SECRET}") secretKey: String,
    @Value("\${JWT_EXPIRATION}") private val accessTokenExpiration: Long,
    @Value("\${JWT_REFRESH_EXPIRATION}") private val refreshTokenExpiration: Long
) {
    private val secretKey: Key

    init {
        // jjwt가 내부적으로 키 길이를 검사하므로, 충분히 긴 바이트 배열이어야 함
        this.secretKey = Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    /**
     * AccessToken 및 RefreshToken 동시 생성
     * - UserDetails에서 providerId(username)와 권한을 읽어 AccessToken에만 권한을 claim으로 넣는다.
     */
    fun generateTokens(userDetails: UserDetails): JwtDto {
        println("JWT 생성 시작: 사용자 ${userDetails.username}")

        // username == providerId (우리 서비스 정책)
        val userId = userDetails.username

        // 권한 목록을 "ROLE_USER,ROLE_ADMIN" 형태의 문자열로 직렬화 (AccessToken에만 저장)
        val authorities = userDetails.authorities.joinToString(",") { it.authority ?: "" }

        // Access Token 생성 (권한 포함)
        val accessToken = createToken(userId, authorities, accessTokenExpiration)

        // Refresh Token 생성 (권한 불포함: 재발급 용도로만 사용)
        val refreshToken = createToken(userId, null, refreshTokenExpiration)

        println("Access/Refresh 토큰 생성 완료 (userId: $userId)")
        return JwtDto(accessToken, refreshToken, System.currentTimeMillis() + refreshTokenExpiration)
    }

    /**
     * 공통 JWT 생성 로직
     *
     * @param providerId     사용자 식별자(= provider_id) → JWT의 subject로 저장
     * @param authorities    권한 문자열 (Access 전용, 예: "ROLE_USER,ROLE_ADMIN")
     * @param expirationTime 만료 시간(ms)
     */
    private fun createToken(providerId: String, authorities: String?, expirationTime: Long): String {
        val jwtBuilder = Jwts.builder()
            .setSubject(providerId)                                   // sub
            .setIssuedAt(Date())                                  // iat
            .setExpiration(Date(System.currentTimeMillis() + expirationTime)) // exp
            .signWith(secretKey, SignatureAlgorithm.HS256)           // 서명

        // Access Token에만 권한을 실어 보낸다.
        if (authorities != null) {
            jwtBuilder.claim("authorities", authorities)
        }

        return jwtBuilder.compact()
    }

    /**
     * 토큰 유효성 검증
     * - 서명/구조/만료 등을 검증하고, 예외가 없으면 유효한 것으로 간주
     */
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token) // 파싱 성공 == 유효
            true
        } catch (e: JwtException) {
            // SignatureException, MalformedJwtException, ExpiredJwtException 등 모두 포함
            println("JWT 검증 실패: ${e.javaClass.simpleName}")
            false
        }
    }

    /**
     * 토큰에서 Claims 추출
     * - 만료(ExpiredJwtException) 시 예외를 상위로 던져 호출부에서 별도 처리
     * - 그 외 파싱 실패는 TOKEN_INVALID로 래핑
     */
    fun parseClaims(token: String): Claims {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: ExpiredJwtException) {
            println("토큰 만료: ${e.javaClass.simpleName}")
            throw e // 재발급 플로우 등 호출부에서 만료를 구분 처리
        } catch (e: JwtException) {
            println("JWT 파싱 실패: ${e.javaClass.simpleName}")
            throw GeneralException.of(ErrorCode.TOKEN_INVALID)
        }
    }

    /**
     * Claims → 권한 정보 복원
     * - "ROLE_USER,ROLE_ADMIN" 문자열을 SimpleGrantedAuthority 리스트로 변환
     * - 권한이 없으면 기본 ROLE_USER 부여 (게스트 최소 권한)
     */
    fun getAuthFromClaims(claims: Claims): Collection<GrantedAuthority> {
        val authoritiesString = claims.get("authorities", String::class.java)
        if (authoritiesString.isNullOrEmpty()) {
            println("권한 정보 없음 - 기본 ROLE_USER 부여")
            return Collections.singletonList(SimpleGrantedAuthority("ROLE_USER"))
        }
        return authoritiesString.split(",")
            .map { SimpleGrantedAuthority(it) }
    }

    /**
     * 만료 허용 파싱
     * - 만료된 토큰이라도 Claims만 뽑아 써야 하는 경우(예: Refresh로 재발급) 사용
     */
    fun parseClaimsAllowExpired(token: String): Claims {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: ExpiredJwtException) {
            // 만료됐지만 payload(Claims)는 안전하게 획득 가능
            e.claims
        }
    }
}

