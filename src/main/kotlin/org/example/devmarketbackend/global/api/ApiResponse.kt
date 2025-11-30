package org.example.devmarketbackend.global.api

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder("isSuccess", "code", "message", "result")
class ApiResponse<T> private constructor(
    val isSuccess: Boolean, // 성공 여부
    val code: String, // 응답 코드
    val message: String, // 메시지
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val result: T? // 응답 데이터
) {
    companion object {
        // 성공
        fun <T> onSuccess(code: BaseCode, result: T?): ApiResponse<T> {
            return ApiResponse(
                true,
                code.getReason().code,
                code.getReason().message,
                result
            )
        }

        // 실패
        fun <T> onFailure(code: BaseCode, data: T?): ApiResponse<T> {
            return ApiResponse(
                false,
                code.getReason().code,
                code.getReason().message,
                data
            )
        }
    }
}

