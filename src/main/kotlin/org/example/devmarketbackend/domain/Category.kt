package org.example.devmarketbackend.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.example.devmarketbackend.domain.entity.BaseEntity

@Entity
@Table(name = "categorys")
class Category protected constructor() : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    var id: Long? = null

    @Column(nullable = false)
    var categoryName: String = ""

    // 아이템과의 관계 설정
    @ManyToMany
    @JsonIgnore
    @JoinTable(
        name = "category_item",
        joinColumns = [JoinColumn(name = "category_id")],
        inverseJoinColumns = [JoinColumn(name = "item_id")]
    )
    var items: MutableList<Item> = ArrayList()

    // 생성자
    constructor(categoryName: String) : this() {
        this.categoryName = categoryName
    }

    // 카테고리명 업데이트
    fun updateCategory(categoryName: String) {
        this.categoryName = categoryName
    }
}
// many to many를 활용한 다대다 매핑

