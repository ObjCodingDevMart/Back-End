package org.example.devmarketbackend.repository

import org.example.devmarketbackend.domain.Item
import org.example.devmarketbackend.domain.Review
import org.example.devmarketbackend.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReviewRepository : JpaRepository<Review, Long> {
    fun findAllByItem(item: Item): List<Review>
    fun findAllByUser(user: User): List<Review>
}

