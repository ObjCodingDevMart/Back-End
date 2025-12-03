package org.example.devmarketbackend.dto.response

import org.example.devmarketbackend.domain.LikeBookmark
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException

data class LikeBookmarkResponse(
    val bookmarkId: Long?,
    val item: ItemResponseDto
) {
    companion object {
        fun from(bookmark: LikeBookmark): LikeBookmarkResponse {
            val item = bookmark.item ?: throw GeneralException.of(ErrorCode.ITEM_NOT_FOUND)
            return LikeBookmarkResponse(bookmark.id, ItemResponseDto.from(item))
        }
    }
}

