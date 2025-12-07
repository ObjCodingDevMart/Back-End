package org.example.devmarketbackend.domain

import jakarta.persistence.*
import org.example.devmarketbackend.domain.entity.BaseEntity

@Entity
@Table(name = "item")
class Item protected constructor() : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var itemname: String = ""

    @Column(nullable = false)
    var price: Int = 0

    @Column(nullable = true)
    var imagePath: String? = null

    @Column(nullable = false)
    var brand: String = ""

    @Column(nullable = false)
    var isNew: Boolean = false

    @Column(nullable = true)
    var s3ImgKey: String? = null

    // 카테고리와 연관관계 설정
    @ManyToMany(mappedBy = "items")
    val categories: MutableList<Category> = ArrayList()

    @OneToMany(mappedBy = "item", cascade = [CascadeType.ALL], orphanRemoval = true)
    val reviews: MutableList<Review> = ArrayList()

    // 생성자
    constructor(
        itemname: String,
        price: Int,
        thumbnailImg: String,
        imgKey: String,
        brand: String,
        isNew: Boolean
    ) : this() {
        this.itemname = itemname
        this.price = price
        this.imagePath = thumbnailImg
        this.s3ImgKey = imgKey
        this.brand = brand
        this.isNew = isNew
    }
}
// many to many를 활용하여 카테고리와 매핑 다대다 관계 설정
// base entity를 활용하여 upload at update at 컬럼 추가

