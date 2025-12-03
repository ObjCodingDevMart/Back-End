package org.example.devmarketbackend.login.auth.jwt

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {

    fun findByRefreshToken(refreshToken: String): Optional<RefreshToken>

    fun deleteByRefreshToken(refreshToken: String)
}

