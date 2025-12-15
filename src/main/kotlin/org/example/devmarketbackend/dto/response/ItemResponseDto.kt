package org.example.devmarketbackend.dto.response

import org.example.devmarketbackend.domain.Item

data class ItemResponseDto(
    val itemId: Long?,
    val itemName: String,
    val price: Int,
    val imagePath: String?,
    val brand: String,
    val isNew: Boolean,
    val categories: List<String>,
    val productDetailImgUrl: String? = null,
    val productDetailContent: String? = null
) {
    companion object {
        fun from(item: Item): ItemResponseDto {
            return ItemResponseDto(
                item.id,
                item.itemname,
                item.price,
                item.imagePath,
                item.brand,
                item.isNew,
                item.categories.map { it.categoryName },
                item.itemDetail?.productDetailImgUrl,
                item.itemDetail?.productDetailContent
            )
        }
    }
}
//아이템 카테고리 이름만 반환 할 수 있도록 작성

