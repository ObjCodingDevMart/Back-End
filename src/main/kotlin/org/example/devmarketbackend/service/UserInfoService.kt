package org.example.devmarketbackend.service

import org.example.devmarketbackend.dto.request.UserInfoFixRequest
import org.example.devmarketbackend.dto.response.UserInfoResponse
import org.example.devmarketbackend.dto.response.UserMilegeResponse
import org.example.devmarketbackend.domain.User
import org.example.devmarketbackend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserInfoService(
    private val userRepository: UserRepository
) {
    //내정보 조회
    @Transactional
    fun getUserInfo(user: User): UserInfoResponse {
        return UserInfoResponse.from(user)
    }

    //내정보 수정
    @Transactional
    fun fixUserInfo(user: User, request: UserInfoFixRequest): UserInfoResponse {
        request.usernickname?.let { user.usernickname = it }
        request.phoneNumber?.let { user.phoneNumber = it }
        return UserInfoResponse.from(user)
    }

    //내 마일리지 조회
    @Transactional
    fun getUserMileges(user: User): UserMilegeResponse {
        return UserMilegeResponse.from(user)
    }
}

