package org.example.devmarketbackend.service

import jakarta.transaction.Transactional
import org.example.devmarketbackend.dto.request.CategoryCreateRequest
import org.example.devmarketbackend.dto.response.CategoryResponse
import org.example.devmarketbackend.dto.response.ItemResponseDto
import org.example.devmarketbackend.domain.Category
import org.example.devmarketbackend.domain.Item
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.repository.CategoryRepository
import org.example.devmarketbackend.repository.ItemRepository
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val itemRepository: ItemRepository
) {
    // 모든 카테고리 조회
    @Transactional
    fun getAllCategories(): List<CategoryResponse> {
        return categoryRepository.findAll().map { CategoryResponse.from(it) }
    }

    //개별 카테고리 조회
    @Transactional
    fun getCategoryById(categoryId: Long): CategoryResponse {
        return categoryRepository.findById(categoryId)
            .map { CategoryResponse.from(it) }
            .orElseThrow { GeneralException.of(ErrorCode.CATEGORY_NOT_FOUND) }
    }

    //카테고리 수정
    @Transactional
    fun categoryFix(request: CategoryCreateRequest, categoryId: Long): CategoryResponse {
        val category = categoryRepository.findById(categoryId)
            .orElseThrow { GeneralException.of(ErrorCode.CATEGORY_NOT_FOUND) }
        category.updateCategory(request.categoryName)
        return CategoryResponse.from(category)
    }

    //카테고리 추가
    @Transactional
    fun categoryCreate(request: CategoryCreateRequest): CategoryResponse {
        val category = Category(request.categoryName)
        categoryRepository.save(category)
        return CategoryResponse.from(category)
    }

    //카테고리 삭제
    @Transactional
    fun deleteCategory(categoryId: Long) {
        if (!categoryRepository.existsById(categoryId)) {
            throw GeneralException.of(ErrorCode.CATEGORY_NOT_FOUND)
        }
        categoryRepository.deleteById(categoryId)
    }

    //카테고리별 상품조회
    @Transactional
    fun getItemsByCategory(categoryId: Long): List<ItemResponseDto> {
        if (categoryId == 3L) {
            val newItems = itemRepository.findAllByIsNew(true)
            return newItems.map { ItemResponseDto.from(it) }
        }
        val category = categoryRepository.findById(categoryId)
            .orElseThrow { GeneralException.of(ErrorCode.CATEGORY_NOT_FOUND) }
        return category.items.map { ItemResponseDto.from(it) }
    }
}
//stream을 통해 item 리스트를 iterator로 뜯어낸 뒤 map을 통해 각 객체 item response로 변환, .collect를 통해 다시 리스트로 변환

