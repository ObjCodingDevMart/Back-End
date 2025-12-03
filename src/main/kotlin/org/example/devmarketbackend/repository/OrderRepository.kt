package org.example.devmarketbackend.repository

import org.example.devmarketbackend.domain.Order
import org.example.devmarketbackend.global.constant.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findByStatusAndCreatedAtBefore(status: OrderStatus, dateTime: LocalDateTime): List<Order>
}

