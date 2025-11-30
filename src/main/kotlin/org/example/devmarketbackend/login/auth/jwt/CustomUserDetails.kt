package org.example.devmarketbackend.login.auth.jwt

import org.example.devmarketbackend.domain.Address
import org.example.devmarketbackend.domain.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.Collections

/**
 * Spring Security에서 인증 주체(principal)로 사용하는 사용자 정보 객체
 * - 우리 서비스는 "providerId(카카오 고유 ID)"를 username으로 사용
 * - 비밀번호 기반 로그인이 아니므로 password는 사용하지 않음
 */
class CustomUserDetails(
    val userId: Long? = null,         // 내부 DB의 User PK
    val providerId: String = "",      // 소셜 로그인 고유 식별자 (username으로 사용)
    val usernickname: String? = null, // 카카오 프로필 닉네임(표시용)
    val address: Address? = null,     // 예시: 프로필의 주소 정보
    private val _authorities: Collection<GrantedAuthority>? = null // 권한
) : UserDetails {

    // User 엔티티를 받아 기본 권한(ROLE_USER)을 가진 CustomUserDetails로 변환
    constructor(user: User) : this(
        userId = user.id,
        providerId = user.providerId,
        usernickname = user.usernickname,
        address = user.address,
        _authorities = Collections.singletonList(SimpleGrantedAuthority("ROLE_USER"))
    )

    // username, password, 권한을 직접 받는 생성자
    constructor(
        providerId: String,
        password: String,
        authorities: Collection<GrantedAuthority>
    ) : this(
        providerId = providerId,
        userId = null,
        usernickname = null,
        address = null,
        _authorities = authorities
    )

    companion object {
        /**
         * User 엔티티 → CustomUserDetails 변환
         * - 권한(authorities)은 지정하지 않음 → getAuthorities()에서 ROLE_USER로 보완
         */
        fun fromEntity(entity: User): CustomUserDetails {
            return CustomUserDetails(
                userId = entity.id,
                providerId = entity.providerId,
                usernickname = entity.usernickname,
                address = entity.address
            )
        }
    }

    /**
     * CustomUserDetails → User 엔티티 변환
     * - 신규 저장 시 최소 필드만 세팅 (비밀번호 없음)
     * - 필요 시 등급/상태/타 필드를 서비스 레이어에서 채워 넣음
     */
    fun toEntity(): User {
        val user = User()
        user.id = this.userId
        user.providerId = this.providerId
        user.usernickname = this.usernickname ?: ""
        user.address = this.address
        return user
    }

    // ──────────────────────────────── UserDetails 필수 구현 ───────────────────────────────

    override fun getUsername(): String {
        // username으로 providerId를 사용 (주요 인증 키)
        return this.providerId
    }

    /**
     * 권한 조회
     * - 명시된 권한이 있으면 그대로 사용
     * - 없으면 기본 ROLE_USER 권한을 부여
     */
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return if (this._authorities != null && this._authorities.isNotEmpty()) {
            this._authorities
        } else {
            Collections.singletonList(SimpleGrantedAuthority("ROLE_USER"))
        }
    }

    override fun getPassword(): String {
        // 소셜 로그인은 비밀번호를 사용하지 않음
        return ""
    }

    override fun isAccountNonExpired(): Boolean {
        // 계정 만료 정책 사용 시 실제 값으로 교체
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        // 잠금 정책 사용 시 실제 값으로 교체
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        // 자격 증명(비밀번호) 만료 정책 사용 시 실제 값으로 교체
        return true
    }

    override fun isEnabled(): Boolean {
        // 활성/비활성 정책 사용 시 실제 값으로 교체 (예: 탈퇴/정지 사용자)
        return true
    }
}

