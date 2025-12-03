package org.example.devmarketbackend.repository

import org.example.devmarketbackend.domain.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    //카테고리 이름 기반 카테고리 탐색
    fun findByCategoryName(categoryName: String): Optional<Category>
}
//이름 검색기능 추가

