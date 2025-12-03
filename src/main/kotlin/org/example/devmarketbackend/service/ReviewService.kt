package org.example.devmarketbackend.service

import jakarta.transaction.Transactional
import org.example.devmarketbackend.domain.Item
import org.example.devmarketbackend.domain.Review
import org.example.devmarketbackend.domain.User
import org.example.devmarketbackend.dto.request.ReviewCreateRequest
import org.example.devmarketbackend.dto.response.ReviewResponse
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.repository.ItemRepository
import org.example.devmarketbackend.repository.ReviewRepository
import org.springframework.stereotype.Service

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val itemRepository: ItemRepository
) {

    @Transactional
    fun createReview(user: User, request: ReviewCreateRequest): ReviewResponse {
        val item = findItemOrThrow(request.itemId)
        val review = Review(
            request.rating,
            request.content,
            user,
            item,
            request.imgUrl ?: "",
            request.imgKey ?: ""
        )
        reviewRepository.save(review)
        return ReviewResponse.from(review)
    }

    @Transactional
    fun getReviewsByItem(itemId: Long): List<ReviewResponse> {
        val item = findItemOrThrow(itemId)
        return reviewRepository.findAllByItem(item).map { ReviewResponse.from(it) }
    }

    @Transactional
    fun getReviewsByUser(user: User): List<ReviewResponse> {
        return reviewRepository.findAllByUser(user).map { ReviewResponse.from(it) }
    }

    private fun findItemOrThrow(itemId: Long): Item {
        return itemRepository.findById(itemId)
            .orElseThrow { GeneralException.of(ErrorCode.ITEM_NOT_FOUND) }
    }
}

