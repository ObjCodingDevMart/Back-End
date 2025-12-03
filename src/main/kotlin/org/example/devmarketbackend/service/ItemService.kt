package org.example.devmarketbackend.service

import jakarta.transaction.Transactional
import org.example.devmarketbackend.dto.request.ItemCreateRequest
import org.example.devmarketbackend.dto.response.ItemResponseDto
import org.example.devmarketbackend.dto.response.S3Item
import org.example.devmarketbackend.domain.Item
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.repository.CategoryRepository
import org.example.devmarketbackend.repository.ItemRepository
import org.example.devmarketbackend.s3.S3Service
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ItemService(
    private val itemRepository: ItemRepository,
    private val categoryRepository: CategoryRepository,
    private val s3Service: S3Service
) {
    // 개별 상품 조회
    @Transactional
    fun getItemById(itemId: Long): ItemResponseDto {
        return itemRepository.findById(itemId)
            .map { ItemResponseDto.from(it) }
            .orElseThrow { GeneralException.of(ErrorCode.ITEM_NOT_FOUND) }
    }

    //모든 상품 조회
    @Transactional
    fun getAllItems(): List<ItemResponseDto> {
        return itemRepository.findAll().map { ItemResponseDto.from(it) }
    }

    //상품 생성
    @Transactional
    fun createItem(request: ItemCreateRequest, file: MultipartFile): ItemResponseDto {
        val s3Result = s3Service.uploadFileForItem(file)
        val item = Item(
            request.itemname,
            request.price,
            s3Result.url,
            s3Result.itemName,
            request.brand,
            request.isNew
        )
        val categories = categoryRepository.findAllById(request.categoryIds)
        // ManyToMany 관계: Category가 주인(owner)이므로 Category의 items에 추가
        categories.forEach { category ->
            category.items.add(item)
        }
        itemRepository.save(item)

        return ItemResponseDto.from(item)
    }

    //item 삭제
    @Transactional
    fun deleteItem(itemId: Long) {
        val item = itemRepository.findById(itemId)
            .orElseThrow { GeneralException.of(ErrorCode.ITEM_NOT_FOUND) }
        item.s3ImgKey?.let { s3Service.deleteFile(it) }
        itemRepository.deleteById(itemId)
    }

    //item 수정
    @Transactional
    fun fixItem(itemId: Long, request: ItemCreateRequest, file: MultipartFile): ItemResponseDto {
        val item = itemRepository.findById(itemId)
            .orElseThrow { GeneralException.of(ErrorCode.ITEM_NOT_FOUND) }
        item.s3ImgKey?.let { s3Service.deleteFile(it) }
        val s3Result = s3Service.uploadFileForItem(file)
        item.itemname = request.itemname
        item.price = request.price
        item.brand = request.brand
        item.imagePath = s3Result.url
        item.s3ImgKey = s3Result.itemName
        item.isNew = request.isNew
        
        // 기존 카테고리 관계 제거
        item.categories.forEach { category ->
            category.items.remove(item)
        }
        item.categories.clear()
        
        // 새로운 카테고리 관계 추가
        val categories = categoryRepository.findAllById(request.categoryIds)
        categories.forEach { category ->
            category.items.add(item)
        }

        return ItemResponseDto.from(item)
    }
}

