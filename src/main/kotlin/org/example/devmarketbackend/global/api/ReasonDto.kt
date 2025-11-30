package org.example.devmarketbackend.global.api

import org.springframework.http.HttpStatus

// API 응답 상세 정보
data class ReasonDto(
    val httpStatus: HttpStatus, // HTTP 상태 코드
    val code: String, // 응답 코드
    val message: String // 응답 메시지
) : BaseCode {
    override fun getReason(): ReasonDto {
        return this
    }
}

