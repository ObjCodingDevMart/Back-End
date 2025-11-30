package org.example.devmarketbackend.repository

import org.example.devmarketbackend.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {

    // providerId(카카오 고유 ID) 기반 조회 (feature/4)
    fun findByProviderId(providerId: String): Optional<User>

    fun existsByProviderId(providerId: String): Boolean

}

