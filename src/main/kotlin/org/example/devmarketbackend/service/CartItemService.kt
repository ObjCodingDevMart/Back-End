package org.example.devmarketbackend.service

import jakarta.transaction.Transactional
import org.example.devmarketbackend.domain.Cart
import org.example.devmarketbackend.domain.CartItem
import org.example.devmarketbackend.domain.Item
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.repository.CartItemRepository
import org.springframework.stereotype.Service

@Service
class CartItemService(
    private val cartItemRepository: CartItemRepository
) {

    @Transactional
    fun findById(cartItemId: Long): CartItem {
        return cartItemRepository.findById(cartItemId)
            .orElseThrow { GeneralException.of(ErrorCode.CART_ITEM_NOT_FOUND) }
    }

    @Transactional
    fun findByCartAndItem(cart: Cart, item: Item): CartItem? {
        return cartItemRepository.findByCartAndItem(cart, item).orElse(null)
    }
}

