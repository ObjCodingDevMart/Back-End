package org.example.devmarketbackend.dto.response

import org.example.devmarketbackend.domain.User

data class UserMilegeResponse(
    val maxMileage: Int
) {
    companion object {
        fun from(user: User): UserMilegeResponse {
            return UserMilegeResponse(
                user.maxMileage
            )
        }
    }
}
//유저 마일리지와 최근 사용 금액 반환

