package org.example.devmarketbackend.service

import org.springframework.transaction.annotation.Transactional
import org.example.devmarketbackend.domain.Cart
import org.example.devmarketbackend.domain.CartItem
import org.example.devmarketbackend.domain.User
import org.example.devmarketbackend.domain.Item
import org.example.devmarketbackend.dto.response.CartResponse
import org.example.devmarketbackend.global.api.ErrorCode
import org.example.devmarketbackend.global.exception.GeneralException
import org.example.devmarketbackend.repository.CartRepository
import org.example.devmarketbackend.repository.ItemRepository
import org.springframework.stereotype.Service

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val cartItemService: CartItemService,
    private val itemRepository: ItemRepository
) {

    @Transactional
    fun getCart(user: User): CartResponse {
        val cart = getOrCreateCart(user)
        return CartResponse.from(cart)
    }

    @Transactional
    fun addItemToCart(user: User, itemId: Long, quantity: Int): CartResponse {
        if (quantity <= 0) {
            throw GeneralException.of(ErrorCode.CART_ITEM_INVALID)
        }
        val item = findItemOrThrow(itemId)
        val cart = getOrCreateCart(user)
        cart.addItem(item, quantity)
        return CartResponse.from(cart)
    }

    @Transactional
    fun updateCartItemQuantity(user: User, cartItemId: Long, quantity: Int): CartResponse {
        if (quantity <= 0) {
            throw GeneralException.of(ErrorCode.CART_ITEM_INVALID)
        }
        val cartItem = cartItemService.findById(cartItemId)
        verifyOwnership(cartItem, user)
        cartItem.changeQuantity(quantity)
        val cart = cartItem.cart ?: throw GeneralException.of(ErrorCode.CART_NOT_FOUND)
        return CartResponse.from(cart)
    }

    @Transactional
    fun removeCartItem(user: User, cartItemId: Long): CartResponse {
        val cartItem = cartItemService.findById(cartItemId)
        verifyOwnership(cartItem, user)
        val cart = cartItem.cart ?: throw GeneralException.of(ErrorCode.CART_NOT_FOUND)
        cart.removeItem(cartItem)
        return CartResponse.from(cart)
    }

    @Transactional
    fun clearCart(user: User) {
        val cart = findCartOrThrow(user)
        cart.clear()
    }

    private fun verifyOwnership(cartItem: CartItem, user: User) {
        val ownerId = cartItem.cart?.user?.id
        if (ownerId != user.id) {
            throw GeneralException.of(ErrorCode.CART_ITEM_INVALID)
        }
    }

    private fun getOrCreateCart(user: User): Cart {
        val existing = cartRepository.findByUser(user)
        return if (existing.isPresent) {
            existing.get()
        } else {
            cartRepository.save(Cart.create(user))
        }
    }

    private fun findCartOrThrow(user: User): Cart {
        return cartRepository.findByUser(user)
            .orElseThrow { GeneralException.of(ErrorCode.CART_NOT_FOUND) }
    }

    private fun findItemOrThrow(itemId: Long): Item {
        return itemRepository.findById(itemId)
            .orElseThrow { GeneralException.of(ErrorCode.ITEM_NOT_FOUND) }
    }
}

