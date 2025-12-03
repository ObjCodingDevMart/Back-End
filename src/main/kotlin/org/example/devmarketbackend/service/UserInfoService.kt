package org.example.devmarketbackend.service

import org.example.devmarketbackend.dto.request.UserInfoFixRequest
import org.example.devmarketbackend.dto.response.UserInfoResponse
import org.example.devmarketbackend.dto.response.UserMilegeResponse
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException
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
        val managed = resolveUser(user)
        return UserInfoResponse.from(managed)
    }

    //내정보 수정
    @Transactional
    fun fixUserInfo(user: User, request: UserInfoFixRequest): UserInfoResponse {
        val managed = resolveUser(user)
        request.usernickname?.let { managed.usernickname = it }
        request.phoneNumber?.let { managed.phoneNumber = it }
        return UserInfoResponse.from(managed)
    }

    //내 마일리지 조회
    @Transactional
    fun getUserMileges(user: User): UserMilegeResponse {
        val managed = resolveUser(user)
        return UserMilegeResponse.from(managed)
    }

    private fun resolveUser(user: User): User {
        val userId = user.id
        return if (userId != null) {
            userRepository.findById(userId)
                .orElseThrow { GeneralException.of(ErrorCode.USER_NOT_FOUND) }
        } else {
            throw GeneralException.of(ErrorCode.USER_NOT_FOUND)
        }
    }
}

