package org.example.devmarketbackend.repository

import org.example.devmarketbackend.domain.Cart
import org.example.devmarketbackend.domain.CartItem
import org.example.devmarketbackend.domain.Item
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CartItemRepository : JpaRepository<CartItem, Long> {
    fun findByCart(cart: Cart): List<CartItem>
    fun findByCartAndItem(cart: Cart, item: Item): Optional<CartItem>
    fun deleteAllByCart(cart: Cart)
}

