package org.example.devmarketbackend.repository

import org.example.devmarketbackend.domain.Cart
import org.example.devmarketbackend.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CartRepository : JpaRepository<Cart, Long> {
    fun findByUser(user: User): Optional<Cart>
}

