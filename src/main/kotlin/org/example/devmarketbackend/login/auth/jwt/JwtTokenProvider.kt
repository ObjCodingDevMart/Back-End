package org.example.devmarketbackend.login.auth.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.example.devmarketbackend.domain.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.Date

/**
 * JWT 토큰을 생성하고 검증하는 유틸
 */
@Component
class JwtTokenProvider(
    @Value("\${JWT_SECRET}")
    private val jwtSecret: String,

    @Value("\${JWT_EXPIRATION}")
    private val accessTokenExpirationMs: Long,

    @Value("\${JWT_REFRESH_EXPIRATION}")
    private val refreshTokenExpirationMs: Long
) {
    private lateinit var key: Key

    @PostConstruct
    fun init() {
        val secretBytes = jwtSecret.toByteArray(StandardCharsets.UTF_8)
        key = Keys.hmacShaKeyFor(secretBytes)
    }

    val accessTokenValidityMs: Long
        get() = accessTokenExpirationMs

    val refreshTokenValidityMs: Long
        get() = refreshTokenExpirationMs

    fun generateAccessToken(user: User): String {
        val now = Date()
        val expiry = Date(now.time + accessTokenExpirationMs)
        return Jwts.builder()
            .setSubject(user.providerId)
            .claim("id", user.id)
            .claim("nickname", user.usernickname)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun generateRefreshToken(subject: String): String {
        val now = Date()
        return Jwts.builder()
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + refreshTokenExpirationMs))
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            parser.parseClaimsJws(token)
            true
        } catch (e: JwtException) {
            false
        }
    }

    fun getSubject(token: String, allowExpired: Boolean = false): String {
        val claims = parseClaims(token, allowExpired)
        return claims.subject ?: throw JwtException("Subject is missing")
    }

    fun getExpiration(token: String, allowExpired: Boolean = false): Date {
        return parseClaims(token, allowExpired).expiration
    }

    private val parser by lazy {
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
    }

    private fun parseClaims(token: String, allowExpired: Boolean): Claims {
        return try {
            parser.parseClaimsJws(token).body
        } catch (ex: ExpiredJwtException) {
            if (allowExpired) {
                ex.claims
            } else {
                throw ex
            }
        }
    }
}

