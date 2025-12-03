package org.example.devmarketbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.example.devmarketbackend.dto.request.ItemCreateRequest
import org.example.devmarketbackend.dto.response.ItemResponseDto
import org.example.devmarketbackend.global.api.ApiResponse
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.api.SuccessCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.service.CategoryService
import org.example.devmarketbackend.service.ItemService
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/items")
class ItemController(
    private val itemService: ItemService,
    private val categoryService: CategoryService,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    //모든 상품 조회
    @GetMapping
    @Operation(summary = "모든 상품 조회", description = "모든 상품을 목록으로 조회합니다.")
    fun getAllItems(): ApiResponse<*> {
        val items = itemService.getAllItems()
        return if (items.isEmpty()) {
            ApiResponse.onSuccess(SuccessCode.CATEGORY_ITEMS_EMPTY, emptyList<ItemResponseDto>())
        } else {
            ApiResponse.onSuccess(SuccessCode.ITEM_GET_SUCCESS, items)
        }
    }

    //개별 상품 조회
    @GetMapping("/{itemId}")
    @Operation(summary = "개별상품조회", description = "아이템을 개별 조회합니다.")
    fun getItem(@PathVariable itemId: Long): ApiResponse<*> {
        log.info("[STEP 1 개별 상품 조회 요청 수신..]")
        return try {
            val item = itemService.getItemById(itemId)
            log.info("[STEP 2] 개별 상품 조회 성공")
            ApiResponse.onSuccess(SuccessCode.ITEM_GET_SUCCESS, item)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 개별 상품 조회중 예외 발생: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("[ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    //상품 생성
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "상품 생성", description = "요청한 상품을 생성합니다.")
    fun createItem(
        @Parameter(description = "상품 정보(JSON)", required = true)
        @RequestPart("ItemInfo") itemInfoJson: String,

        @Parameter(description = "상품 이미지 파일", required = true)
        @RequestPart("Photo") file: MultipartFile
    ): ApiResponse<*> {
        log.info("[STEP 1] 상품 생성 요청 수신..")
        return try {
            val request = objectMapper.readValue(itemInfoJson, ItemCreateRequest::class.java)
            val newItem = itemService.createItem(request, file)
            log.info("[STEP 2] 상품 생성 성공")
            ApiResponse.onSuccess(SuccessCode.OK, newItem)
        } catch (e: GeneralException) {
            log.error("< ❌[ERROR] 상품 생성 중 예외 발생 {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    //상품 수정
    @PutMapping(value = ["/{itemId}"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "상품 수정", description = "요청 상품 정보를 수정합니다.")
    fun updateItem(
        @PathVariable itemId: Long,

        @Parameter(description = "상품 정보(JSON)", required = true)
        @RequestPart("ItemInfo") itemInfoJson: String,  // JSON 문자열로 받기

        @Parameter(description = "상품 이미지 파일", required = true)
        @RequestPart("Photo") file: MultipartFile       // 파일
    ): ApiResponse<*> {
        log.info("[STEP 1] 상품 정보 수정 요청 수신..")
        return try {
            val request = objectMapper.readValue(itemInfoJson, ItemCreateRequest::class.java)
            val fixedItem = itemService.fixItem(itemId, request, file)
            log.info("[STEP 2] 상품 정보 수정 성공")
            ApiResponse.onSuccess(SuccessCode.OK, fixedItem)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 개별 주문 조회 중 예외 발생: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    //상품 삭제
    @DeleteMapping("/{itemId}")
    @Operation(summary = "상품 삭제", description = "해당 상품을 삭제합니다.")
    fun deleteItem(
        @PathVariable itemId: Long
    ): ApiResponse<*> {
        log.info("[STEP 1] 상품 삭제 요청 수신..")
        return try {
            itemService.deleteItem(itemId)
            log.info("[STEP 2] ")
            ApiResponse.onSuccess(SuccessCode.OK, null)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 개별 주문 조회 중 예외 발생: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }
}

