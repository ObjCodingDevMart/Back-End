package org.example.devmarketbackend.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class Address(
    @Column(nullable = true)
    var zipcode: String? = null,

    @Column(nullable = true)
    var address: String? = null,

    @Column(name = "address_detail", nullable = true)
    var addressDetail: String? = null
)




