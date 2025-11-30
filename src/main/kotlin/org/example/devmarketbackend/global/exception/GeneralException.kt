package org.example.devmarketbackend.global.exception

import org.example.devmarketbackend.global.api.BaseCode
import org.example.devmarketbackend.global.api.ReasonDto

// 공통 예외 처리
class GeneralException(
    private val code: BaseCode
) : RuntimeException() {

    //예외 생성
    companion object {
        fun of(code: BaseCode): GeneralException {
            return GeneralException(code)
        }
    }

    //예외 상세 정보
    val reason: ReasonDto
        get() = this.code.getReason()
}

