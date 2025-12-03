package org.example.devmarketbackend.global.api

// 응답 인터페이스
interface BaseCode {
    // ReasonDTO -> 응답 코드, 메시지, 상태 정보
    fun getReason(): ReasonDto
}

