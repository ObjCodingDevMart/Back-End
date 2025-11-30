package org.example.devmarketbackend.domain

import jakarta.persistence.Embeddable

/**
 * 간단한 주소 정보 Embeddable 타입
 */
@Embeddable
data class Address(
    var zipcode: String? = null,
    var address: String? = null,
    var addressDetail: String? = null
)

