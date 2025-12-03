package org.example.devmarketbackend.login.auth.jwt

import jakarta.persistence.*
import org.example.devmarketbackend.domain.User
import java.time.LocalDateTime

/**
 * Refresh Token 정보를 저장하는 엔티티
 */
@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    @Column(nullable = false, unique = true)
    var refreshToken: String = "",

    @Column(nullable = false)
    var expiredAt: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    lateinit var user: User
}

