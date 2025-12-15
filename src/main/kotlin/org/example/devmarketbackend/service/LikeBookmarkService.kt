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
import org.springframework.stereotype.Service

@Service
class LikeBookmarkService(
    private val likeBookmarkRepository: LikeBookmarkRepository,
    private val itemRepository: ItemRepository
) {

    @Transactional
    fun addBookmark(user: User, itemId: Long): LikeBookmarkResponse {
        val item = findItemOrThrow(itemId)
        likeBookmarkRepository.findByUserAndItem(user, item).ifPresent {
            throw GeneralException.of(ErrorCode.BOOKMARK_ALREADY_EXISTS)
        }
        val bookmark = LikeBookmark.create(user, item)
        likeBookmarkRepository.save(bookmark)
        return LikeBookmarkResponse.from(bookmark)
    }

    @Transactional
    fun removeBookmark(user: User, itemId: Long) {
        val item = findItemOrThrow(itemId)
        val bookmark = likeBookmarkRepository.findByUserAndItem(user, item)
            .orElseThrow { GeneralException.of(ErrorCode.BOOKMARK_NOT_FOUND) }
        likeBookmarkRepository.delete(bookmark)
    }

    @Transactional
    fun getBookmarks(user: User): List<LikeBookmarkResponse> {
        return likeBookmarkRepository.findAllByUser(user)
            .map { LikeBookmarkResponse.from(it) }
    }

    private fun findItemOrThrow(itemId: Long): Item {
        return itemRepository.findById(itemId)
            .orElseThrow { GeneralException.of(ErrorCode.ITEM_NOT_FOUND) }
    }
}

