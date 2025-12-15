package org.example.devmarketbackend.service

import org.springframework.transaction.annotation.Transactional
import org.example.devmarketbackend.dto.request.AddressRequest
import org.example.devmarketbackend.dto.response.AddressResponse
import org.example.devmarketbackend.domain.Address
import org.example.devmarketbackend.domain.User
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.springframework.stereotype.Service

@Service
class UserAddressService(
    private val userRepository: org.example.devmarketbackend.repository.UserRepository
) {
    //내 주소 조회
    @Transactional
    fun getAddress(user: User): AddressResponse {
        val managedUser = resolveUser(user)
        return AddressResponse.from(managedUser.address)
    }

    //내 주소 수정
    @Transactional
    fun fixAddress(user: User, request: AddressRequest): AddressResponse {
        val managedUser = resolveUser(user)
        val address = Address(
            request.zipcode,
            request.address,
            request.addressDetail
        )
        managedUser.updateAddress(address)
        return AddressResponse.from(managedUser.address)
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
//final을 통해 레포 객체 생성이 여러번 되지 않고 하나의 객체로 사용할 수 있게 작성

