package org.example.devmarketbackend.dto.response

import org.example.devmarketbackend.domain.Address

data class AddressResponse(
    val zipcode: String,
    val address: String,
    val addressDetail: String
) {
    companion object {
        fun from(address: Address?): AddressResponse {
            return AddressResponse(
                address?.zipcode ?: "",
                address?.address ?: "",
                address?.addressDetail ?: ""
            )
        }
    }
}
//주소 객체와 dto를 매핑

