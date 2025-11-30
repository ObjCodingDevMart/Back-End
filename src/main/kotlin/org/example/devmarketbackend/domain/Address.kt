package org.example.devmarketbackend.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class Address(
    @Column(nullable = false)
    var zipcode: String = "10540",
    
    @Column(nullable = false)
    var address: String = "경기도 고양시 덕양구 항공대학로 76",
    
    @Column(nullable = false)
    var addressDetail: String = "한국항공대학교"
)

