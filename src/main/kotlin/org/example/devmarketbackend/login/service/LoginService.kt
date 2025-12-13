package org.example.devmarketbackend.login.service

import io.jsonwebtoken.JwtException
import org.example.devmarketbackend.domain.User
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.domain.Address
import org.example.devmarketbackend.login.auth.jwt.JwtTokenProvider
import org.example.devmarketbackend.login.auth.jwt.RefreshToken
import org.example.devmarketbackend.login.auth.jwt.RefreshTokenRepository
import org.example.devmarketbackend.login.dto.request.MobileLoginRequest
import org.example.devmarketbackend.login.dto.request.MobileTokenRefreshRequest
import org.example.devmarketbackend.login.dto.response.AuthTokenResponse
import org.example.devmarketbackend.login.kakao.KakaoClient
import org.example.devmarketbackend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

/**
 * 카카오 로그인을 위한 인증/토큰 서비스
 */
@Service
class LoginService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val kakaoClient: KakaoClient
) {

    /**
     * 모바일 앱에서 카카오 액세스 토큰을 받아 로그인 처리
     */
    @Transactional
    fun loginWithKakao(request: MobileLoginRequest): AuthTokenResponse {
        val resolved = resolveUserInfo(request)
        return loginWithUserInfo(resolved)
    }

    /**
     * 웹 OAuth2 플로우에서 이미 파싱된 사용자 정보로 로그인 처리
     */
    @Transactional
    fun loginWithOAuth2UserInfo(
        providerId: String,
        nickname: String,
        profileUrl: String?,
        email: String?
    ): AuthTokenResponse {
        val resolved = ResolvedUserInfo(
            providerId = providerId,
            nickname = nickname,
            profileUrl = profileUrl,
            email = email
        )
        return loginWithUserInfo(resolved)
    }

    /**
     * 사용자 정보로 로그인 처리하는 공통 메서드
     */
    private fun loginWithUserInfo(resolved: ResolvedUserInfo): AuthTokenResponse {
        val user = userRepository.findByProviderId(resolved.providerId)
            .orElseGet {
                userRepository.save(
                    User().apply {
                        providerId = resolved.providerId
                        usernickname = resolved.nickname
                        userProfileUrl = resolved.profileUrl
                        email = resolved.email
                        updateAddress(Address())
                    }
                )
            }

        user.apply {
            usernickname = resolved.nickname
            userProfileUrl = resolved.profileUrl
            email = resolved.email
        }

        val accessToken = jwtTokenProvider.generateAccessToken(user)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.providerId)

        val refreshEntity = user.auth ?: RefreshToken().apply { this.user = user }
        refreshEntity.refreshToken = refreshToken
        refreshEntity.expiredAt = jwtTokenProvider.getExpiration(refreshToken).toLocalDateTime()
        refreshTokenRepository.save(refreshEntity)
        user.auth = refreshEntity

        return AuthTokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = jwtTokenProvider.accessTokenValidityMs,
            refreshTokenExpiresIn = jwtTokenProvider.refreshTokenValidityMs
        )
    }

    @Transactional
    fun reissue(request: MobileTokenRefreshRequest): AuthTokenResponse {
        val refreshTokenEntity = refreshTokenRepository.findByRefreshToken(request.refreshToken)
            .orElseThrow { GeneralException.of(ErrorCode.WRONG_REFRESH_TOKEN) }

        if (!jwtTokenProvider.validateToken(request.refreshToken)) {
            throw GeneralException.of(ErrorCode.TOKEN_EXPIRED)
        }

        val providerId = try {
            jwtTokenProvider.getSubject(request.accessToken, allowExpired = true)
        } catch (_: JwtException) {
            throw GeneralException.of(ErrorCode.TOKEN_INVALID)
        }

        val user = userRepository.findByProviderId(providerId)
            .orElseThrow { GeneralException.of(ErrorCode.USER_NOT_FOUND) }

        if (refreshTokenEntity.user.id != user.id) {
            throw GeneralException.of(ErrorCode.TOKEN_INVALID)
        }

        val newAccessToken = jwtTokenProvider.generateAccessToken(user)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(providerId)

        refreshTokenEntity.refreshToken = newRefreshToken
        refreshTokenEntity.expiredAt = jwtTokenProvider.getExpiration(newRefreshToken).toLocalDateTime()
        refreshTokenRepository.save(refreshTokenEntity)

        return AuthTokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            accessTokenExpiresIn = jwtTokenProvider.accessTokenValidityMs,
            refreshTokenExpiresIn = jwtTokenProvider.refreshTokenValidityMs
        )
    }

    private fun resolveUserInfo(request: MobileLoginRequest): ResolvedUserInfo {
        val kakaoUser = kakaoClient.fetchUserInfo(request.kakaoAccessToken)
        
        return ResolvedUserInfo(
            providerId = kakaoUser.id.toString(),
            nickname = kakaoUser.properties?.nickname ?: "KakaoUser",
            profileUrl = kakaoUser.properties?.profileImageUrl,
            email = kakaoUser.kakaoAccount?.email
        )
    }

    private data class ResolvedUserInfo(
        val providerId: String,
        val nickname: String,
        val profileUrl: String?,
        val email: String?
    )

    private fun Date.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault())
}

