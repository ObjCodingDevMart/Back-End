package org.example.devmarketbackend.login.auth.jwt

import jakarta.persistence.*
import org.example.devmarketbackend.domain.User

@Entity
class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ 자체 PK 사용 (AUTO_INCREMENT)
    var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true) // ✅ FK + UNIQUE 제약(사용자당 1개)
    var user: User? = null


    var refreshToken: String? = null

    /**
     * 만료 시각(예: epoch millis)
     */
    var ttl: Long? = null

    /** 새 토큰으로 교체할 때 사용 */
    fun updateRefreshToken(refreshToken: String, ttl: Long) {
        this.refreshToken = refreshToken
        this.ttl = ttl
    }

    /** 만료 시각 갱신 */
    fun updateTtl(ttl: Long) {
        this.ttl = ttl
    }

    // (선택) 가독성 향상을 위한 헬퍼 메서드 예시
    // fun isExpired(): Boolean = ttl != null && System.currentTimeMillis() >= ttl
}

