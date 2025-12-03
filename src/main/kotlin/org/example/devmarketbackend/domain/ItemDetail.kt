package org.example.devmarketbackend.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded

@Embeddable
class ItemDetail {
    @Column(name="product_detail_img_url")
    var productDetailImgUrl: String? = null

    @Column(name="product_detail_content")
    var productDetailContent: String? = null

}