package org.example.devmarketbackend.dto.response

import org.example.devmarketbackend.domain.Category

data class CategoryResponse(
    val categoryId: Long?,
    val categoryName: String
) {
    companion object {
        fun from(category: Category): CategoryResponse {
            return CategoryResponse(
                category.id,
                category.categoryName
            )
        }
    }
}

