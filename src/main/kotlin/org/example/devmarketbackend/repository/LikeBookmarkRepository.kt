package org.example.devmarketbackend.repository

import org.example.devmarketbackend.domain.Item
import org.example.devmarketbackend.domain.LikeBookmark
import org.example.devmarketbackend.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface LikeBookmarkRepository : JpaRepository<LikeBookmark, Long> {
    fun findAllByUser(user: User): List<LikeBookmark>
    fun findByUserAndItem(user: User, item: Item): Optional<LikeBookmark>
}

