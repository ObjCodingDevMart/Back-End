package org.example.devmarketbackend.login.service

import io.jsonwebtoken.Claims
import jakarta.servlet.http.HttpServletRequest
import org.example.devmarketbackend.domain.User
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.login.auth.dto.JwtDto
import org.example.devmarketbackend.login.auth.jwt.RefreshToken
import org.example.devmarketbackend.login.auth.jwt.TokenProvider
import org.example.devmarketbackend.login.auth.repository.RefreshTokenRepository
import org.example.devmarketbackend.login.auth.service.JpaUserDetailsManager
import org.example.devmarketbackend.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * UserService
 * - 사용자 조회/검증, JWT 생성/재발급, RefreshToken 저장/삭제를 담당하는 도메인 서비스
 * - 인증 키로 providerId(소셜 고유 ID)를 사용한다.
 */
@Service
class UserService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val tokenProvider: TokenProvider,
    private val manager: JpaUserDetailsManager
) {

    // ============================================================
    //  1) 회원 관련 서비스
    // ============================================================

    /** provider_id 존재 여부 빠른 확인 */
    fun checkMemberByProviderId(providerId: String): Boolean {
        return userRepository.existsByProviderId(providerId)
    }

    /** provider_id로 회원 찾기 (없을 수 있으므로 Optional) */
    fun findByProviderId(providerId: String): java.util.Optional<User> {
        return userRepository.findByProviderId(providerId)
    }

    /** provider_id로 회원 강제 조회 (없으면 USER_NOT_FOUND 예외) */
    fun getAuthenticatedUser(providerId: String): User {
        return userRepository.findByProviderId(providerId)
            .orElseThrow { GeneralException.of(ErrorCode.USER_NOT_FOUND) }
    }

    // ============================================================
    //  2) Refresh Token 관련 서비스
    // ============================================================

    /**
     * Refresh Token 저장 또는 갱신
     * - providerId로 User를 조회한 뒤, 사용자당 1개의 RefreshToken 행을 upsert한다.
     */
    @Transactional
    fun saveRefreshToken(providerId: String, refreshToken: String, ttl: Long) {
        // 1. 사용자 조회
        val user = userRepository.findByProviderId(providerId)
            .orElseThrow { GeneralException.of(ErrorCode.USER_NOT_FOUND) }

        // 2. 기존 토큰이 있으면 교체, 없으면 생성
        val token = refreshTokenRepository.findByUser(user)
            .map { existingToken ->
                existingToken.updateRefreshToken(refreshToken, ttl)
                existingToken
            }
            .orElseGet {
                println("새 RefreshToken 생성 시도 (user_id=${user.id})")
                RefreshToken().apply {
                    this.user = user                                   // User와 1:1
                    this.refreshToken = refreshToken                   // 실제 RT 문자열
                    this.ttl = ttl                                     // 만료 시각(파라미터로 받은 값 사용)
                }
            }

        // 3. 저장
        refreshTokenRepository.save(token)
        println("RefreshToken 저장 완료 (user_id=${user.id})")
    }

    // ============================================================
    //  3) JWT 생성 및 저장
    // ============================================================

    /**
     * Access/Refresh 토큰 동시 생성 + Refresh 저장
     * - providerId로 UserDetails를 로드 → TokenProvider로 JWT 생성 → RefreshToken upsert
     */
    @Transactional
    fun jwtMakeSave(providerId: String): JwtDto {
        println("UserDetailsManager 타입: ${manager.javaClass.name}")

        // 1. providerId 기반 사용자 로드 (Security용 UserDetails)
        val details = manager.loadUserByUsername(providerId)

        // 2. JWT 생성 (Access에 권한 포함, Refresh에는 권한 미포함)
        val jwt = tokenProvider.generateTokens(details)

        // 3. RefreshToken 저장/갱신
        saveRefreshToken(providerId, jwt.refreshToken ?: "", jwt.ttl ?: 0L)
        return jwt
    }

    // ============================================================
    //  4) Refresh Token 기반 Access Token 재발급
    // ============================================================

    /**
     * Access 만료 상황에서의 재발급 엔드포인트용 로직
     * - 헤더 Authorization에서 Access 토큰을 받아 subject(providerId)를 추출한다.
     * - DB에 저장된 RefreshToken이 유효하면 새 Access/Refresh를 재발급한다(회전).
     */
    @Transactional
    fun reissue(request: HttpServletRequest): JwtDto {
        println("[STEP 1] Access Token 재발급 요청 시작")

        // 1. Authorization 헤더에서 Bearer 토큰 추출
        var accessToken = request.getHeader("Authorization")
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7)
        }

        // 2. (만료 허용) Claims 파싱으로 providerId(subject) 추출
        val claims = try {
            tokenProvider.parseClaimsAllowExpired(accessToken ?: "")
        } catch (e: Exception) {
            println("[ERROR] Access Token Claims 추출 실패: ${e.message}")
            throw GeneralException.of(ErrorCode.TOKEN_INVALID)
        }
        val providerId = claims.subject
        println("[STEP 2] Access Token에서 추출한 providerId: $providerId")

        if (providerId.isNullOrEmpty()) {
            throw GeneralException.of(ErrorCode.TOKEN_INVALID)
        }

        // 3. providerId로 사용자 조회
        val user = findByProviderId(providerId)
            .orElseGet {
                println("[ERROR] providerId=$providerId 사용자 없음")
                throw GeneralException.of(ErrorCode.USER_NOT_FOUND)
            }
        println("[STEP 3] User 조회 성공 (user_id=${user.id}, providerId=${user.providerId})")

        // 4. DB에 저장된 Refresh Token 조회
        val refreshTokenEntity = refreshTokenRepository.findByUser(user)
            .orElseThrow { GeneralException.of(ErrorCode.WRONG_REFRESH_TOKEN) }

        // 5. Refresh Token 유효성 검증 (서명/만료 등)
        if (!tokenProvider.validateToken(refreshTokenEntity.refreshToken ?: "")) {
            // 만료/위조 등으로 무효 → DB에서 삭제 후 만료 에러
            refreshTokenRepository.deleteByUser(user)
            println("[ERROR] Refresh Token 만료 또는 무효 - 삭제 완료 (user_id=${user.id})")
            throw GeneralException.of(ErrorCode.TOKEN_EXPIRED)
        }
        // (선택 강화) 주체 일치성 확인:
        // Claims rtClaims = tokenProvider.parseClaims(refreshTokenEntity.getRefreshToken());
        // if (!providerId.equals(rtClaims.getSubject())) { ... }

        // 6. 새 Access/Refresh 발급 (회전)
        val userDetails = manager.loadUserByUsername(providerId)
        val newJwt = tokenProvider.generateTokens(userDetails)
        println("[STEP 4] 새로운 Access/Refresh Token 발급 완료")

        // 7. Refresh 토큰 교체 저장
        refreshTokenEntity.updateRefreshToken(newJwt.refreshToken ?: "", newJwt.ttl ?: 0L)
        refreshTokenRepository.save(refreshTokenEntity)

        return newJwt
    }

    // ============================================================
    //  5) 로그아웃 서비스
    // ============================================================

    /**
     * 로그아웃
     * - 현재 Access Token의 subject(providerId)를 추출하여 해당 사용자의 RefreshToken 레코드를 삭제한다.
     */
    @Transactional
    fun logout(request: HttpServletRequest) {
        // 1. Authorization 헤더에서 Access 토큰 추출
        var accessToken = request.getHeader("Authorization")
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7)
        }

        // 2. Access 토큰 파싱 (여기서는 만료 허용 없이 처리)
        val claims = tokenProvider.parseClaims(accessToken ?: "")
        val providerId = claims.subject
        if (providerId.isNullOrEmpty()) {
            throw GeneralException.of(ErrorCode.TOKEN_INVALID)
        }

        // 3. providerId로 사용자 조회
        val user = findByProviderId(providerId)
            .orElseThrow { GeneralException.of(ErrorCode.USER_NOT_FOUND) }

        // 4. Refresh Token 삭제 (세션 종료 효과)
        refreshTokenRepository.deleteByUser(user)
        refreshTokenRepository.flush() // 즉시 반영이 꼭 필요하지 않다면 생략 가능
    }
}

