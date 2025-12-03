package org.example.devmarketbackend.dto.request

data class ReviewCreateRequest(
    val itemId: Long,
    val rating: Int,
    val content: String,
    val imgUrl: String? = null,
    val imgKey: String? = null
)

