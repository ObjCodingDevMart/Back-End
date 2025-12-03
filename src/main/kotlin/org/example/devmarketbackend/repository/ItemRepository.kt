package org.example.devmarketbackend.repository

import org.example.devmarketbackend.domain.Item
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ItemRepository : JpaRepository<Item, Long> {
    fun findAllByIsNew(isNew: Boolean): List<Item>
}
// optional을 통한 .orElseThrow 활용
//jpaRepository 상속하여 기능 활용

