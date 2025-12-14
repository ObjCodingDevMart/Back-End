package org.example.devmarketbackend.dto.response

import org.example.devmarketbackend.domain.User
import org.example.devmarketbackend.global.constant.OrderStatus

data class UserInfoResponse(
    val usernickname: String?,
    val recentTotal: Int,
    val maxMilege: Int,
    val userLikeCnt: Int?,
    val orderStatusCounts: Map<OrderStatus, Int>
) {
    companion object {
        fun from(user: User): UserInfoResponse {
            val orderStatusCounts = user.orders
                .groupBy { it.status }
                .mapValues { it.value.size }
                .toMutableMap()
            
            orderStatusCounts.putIfAbsent(OrderStatus.PROCESSING, 0)
            orderStatusCounts.putIfAbsent(OrderStatus.COMPLETE, 0)
            orderStatusCounts.putIfAbsent(OrderStatus.CANCEL, 0)

            return UserInfoResponse(
                user.usernickname,
                user.recentTotal,
                user.maxMileage,
                user.likeBookmark.size,
                orderStatusCounts
            )
        }
    }
}
// 유저 id, 프로바이더 id 같은 민감한 정보는 가리고 닉네임,번호등 정보만 노출
//static메소드를 활용하여 클래스 명으로만 from을 통해 user객체와 매핑하여 response 반환

