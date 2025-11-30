package org.example.devmarketbackend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.devmarketbackend.dto.request.CategoryCreateRequest
import org.example.devmarketbackend.dto.response.CategoryResponse
import org.example.devmarketbackend.dto.response.ItemResponseDto
import org.example.devmarketbackend.global.api.ApiResponse
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.api.SuccessCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.service.CategoryService
import org.example.devmarketbackend.service.ItemService
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
@Tag(name = "카테고리", description = "카테고리 관련 API 입니다.")
class CategoryController(
    private val categoryService: CategoryService,
    private val itemService: ItemService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    //모든 카테고리 조회
    @GetMapping
    @Operation(summary = "모든 카테고리 조회", description = "모든 카테고리를 조회합니다.")
    fun getCategories(): ApiResponse<*> {
        val categories = categoryService.getAllCategories()
        return if (categories.isEmpty()) {
            ApiResponse.onSuccess(SuccessCode.CATEGORY_ITEMS_EMPTY, emptyList<CategoryResponse>())
        } else {
            ApiResponse.onSuccess(SuccessCode.CATEGORY_ITEMS_GET_SUCCESS, categories)
        }
    }

    //카테고리 생성
    @PostMapping
    @Operation(summary = "카테고리 생성", description = "카테고리를 생성합니다.")
    @PreAuthorize("hasRole('ADMIN')") // 권한 설정
    fun addCategory(
        @RequestBody categoryCreateRequest: CategoryCreateRequest
    ): ApiResponse<*> {
        log.info("[STEP 1] 카테고리 생성 요청 수신..")
        return try {
            val newCategory = categoryService.categoryCreate(categoryCreateRequest)
            log.info("[STEP 2] 카테고리 생성 ..")
            ApiResponse.onSuccess(SuccessCode.OK, newCategory)
        } catch (e: GeneralException) {
            log.error("< ❌[ERROR] 카테고리 생성 중 예외 발생 {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    //개별 카테고리 조회
    @GetMapping("/{categoryId}")
    @Operation(summary = "개별 카테고리 조회", description = "해당 카테고리를 조회합니다.")
    fun getCategory(@PathVariable categoryId: Long): ApiResponse<*> {
        log.info("[STEP 1] 개별 카테고리 조회 요청 수신..")
        return try {
            val category = categoryService.getCategoryById(categoryId)
            log.info("[STEP 2] 개별 카테고리 정보 조회 성공")
            ApiResponse.onSuccess(SuccessCode.OK, category)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 개별 카테고리 조회 중 예외 발생: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    //카테고리 수정 요청
    @PutMapping("/{categoryId}")
    @Operation(summary = "카테고리 수정", description = "카테고리를 수정합니다.")
    fun updateCategory(
        @PathVariable categoryId: Long,
        @RequestBody request: CategoryCreateRequest
    ): ApiResponse<*> {
        log.info("[STEP 1] 카테고리 수정 요청 수신..")
        return try {
            val fixCategory = categoryService.categoryFix(request, categoryId)
            log.info("[STEP 2] 카테고리 수정 성공")
            ApiResponse.onSuccess(SuccessCode.OK, fixCategory)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 카테고리 수정 중 예외 발생: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    //카테고리 삭제
    @DeleteMapping("/{categoryId}")
    @Operation(summary = "카테고리 삭제", description = "해당 카테고리를 삭제합니다.")
    fun deleteCategory(@PathVariable categoryId: Long): ApiResponse<*> {
        log.info("[STEP 1] 카테고리 삭제 요청 수신..")
        return try {
            itemService.deleteItem(categoryId)
            log.info("[STEP 2] 카테고리 삭제 성공")
            ApiResponse.onSuccess(SuccessCode.OK, null)
        } catch (e: GeneralException) {
            log.error("❌ [ERROR] 카테고리 삭제 중 예외 발생: {}", e.reason.message)
            throw e
        } catch (e: Exception) {
            log.error("❌ [ERROR] 알 수 없는 예외 발생: {}", e.message)
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    //카테고리별 상품 조회
    @GetMapping("/{categoryId}/items")
    @Operation(
        summary = "카테고리별 상품 조회",
        description = "해당 카테고리에 속해있는 모든 아이템을 조회합니다."
    )
    fun getItemsByCategory(@PathVariable categoryId: Long): ApiResponse<*> {
        log.info("[STEP 1] 카테고리 상품 조회 요청 수신..")
        val categoryItems = categoryService.getItemsByCategory(categoryId)
        return if (categoryItems.isEmpty()) {
            ApiResponse.onSuccess(SuccessCode.CATEGORY_ITEMS_EMPTY, emptyList<ItemResponseDto>())
        } else {
            ApiResponse.onSuccess(SuccessCode.CATEGORY_ITEMS_GET_SUCCESS, categoryItems)
        }
    }
}
//order 컨트롤러를 참고하여 order서비스 계층의 기능을 활용하여 CRUD기능을 구현
// try-catch를 통해 성공과 실패를 분리하여 예외를 처리 dto형태로 도출된 결과에 결과 코드를 붙여 api응답 형태로 반환
