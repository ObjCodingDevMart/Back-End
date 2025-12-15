package org.example.devmarketbackend.service

import org.springframework.transaction.annotation.Transactional
import org.example.devmarketbackend.domain.Item
import org.example.devmarketbackend.domain.LikeBookmark
import org.example.devmarketbackend.domain.User
import org.example.devmarketbackend.dto.response.LikeBookmarkResponse
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.repository.ItemRepository
import org.example.devmarketbackend.repository.LikeBookmarkRepository
import org.example.devmarketbackend.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class LikeBookmarkService(
    private val likeBookmarkRepository: LikeBookmarkRepository,
    private val itemRepository: ItemRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun addBookmark(user: User, itemId: Long): LikeBookmarkResponse {
        val managedUser = resolveUser(user)
        val item = findItemOrThrow(itemId)
        likeBookmarkRepository.findByUserAndItem(managedUser, item).ifPresent {
            throw GeneralException.of(ErrorCode.BOOKMARK_ALREADY_EXISTS)
        }
        val bookmark = LikeBookmark.create(managedUser, item)
        likeBookmarkRepository.save(bookmark)
        return LikeBookmarkResponse.from(bookmark)
    }

    @Transactional
    fun removeBookmark(user: User, itemId: Long) {
        val managedUser = resolveUser(user)
        val item = findItemOrThrow(itemId)
        val deleted = likeBookmarkRepository.deleteByUserAndItem(managedUser, item)
        if (deleted == 0L) {
            throw GeneralException.of(ErrorCode.BOOKMARK_NOT_FOUND)
        }
    }

    @Transactional
    fun getBookmarks(user: User): List<LikeBookmarkResponse> {
        val managedUser = resolveUser(user)
        return likeBookmarkRepository.findAllByUser(managedUser)
            .map { LikeBookmarkResponse.from(it) }
    }

    private fun findItemOrThrow(itemId: Long): Item {
        return itemRepository.findById(itemId)
            .orElseThrow { GeneralException.of(ErrorCode.ITEM_NOT_FOUND) }
    }
    
    private fun resolveUser(user: User): User {
        val userId = user.id
        return if (userId != null) {
            userRepository.findById(userId)
                .orElseThrow { GeneralException.of(ErrorCode.USER_NOT_FOUND) }
        } else {
            throw GeneralException.of(ErrorCode.USER_NOT_FOUND)
        }
    }
}

