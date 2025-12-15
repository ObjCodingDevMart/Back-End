package org.example.devmarketbackend.dto.response

import org.example.devmarketbackend.domain.Review
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException

data class ReviewResponse(
    val reviewId: Long?,
    val nickname: String?,
    val rating: Int?,
    val content: String?,
    val imgUrl: String?,
    val imgKey: String?,
    val item: ItemResponseDto
) {
    companion object {
        fun from(review: Review): ReviewResponse {
            val item = review.item ?: throw GeneralException.of(ErrorCode.ITEM_NOT_FOUND)
            return ReviewResponse(
                review.id,
                review.user?.usernickname,
                review.rating,
                review.content,
                review.imgUrl,
                review.imgKey,
                ItemResponseDto.from(item)
            )
        }
    }
}

