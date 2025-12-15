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
import org.example.devmarketbackend.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val cartItemService: CartItemService,
    private val itemRepository: ItemRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun getCart(user: User): CartResponse {
        val managedUser = resolveUser(user)
        val cart = getOrCreateCart(managedUser)
        return CartResponse.from(cart)
    }

    @Transactional
    fun addItemToCart(user: User, itemId: Long, quantity: Int): CartResponse {
        if (quantity <= 0) {
            throw GeneralException.of(ErrorCode.CART_ITEM_INVALID)
        }
        val managedUser = resolveUser(user)
        val item = findItemOrThrow(itemId)
        val cart = getOrCreateCart(managedUser)
        cart.addItem(item, quantity)
        // 명시적으로 저장해 변경 사항을 즉시 반영
        cartRepository.save(cart)
        return CartResponse.from(cart)
    }

    @Transactional
    fun updateCartItemQuantity(user: User, cartItemId: Long, quantity: Int): CartResponse {
        if (quantity <= 0) {
            throw GeneralException.of(ErrorCode.CART_ITEM_INVALID)
        }
        val managedUser = resolveUser(user)
        val cartItem = cartItemService.findById(cartItemId)
        verifyOwnership(cartItem, managedUser)
        cartItem.changeQuantity(quantity)
        val cart = cartItem.cart ?: throw GeneralException.of(ErrorCode.CART_NOT_FOUND)
        cartRepository.save(cart)
        return CartResponse.from(cart)
    }

    @Transactional
    fun removeCartItem(user: User, cartItemId: Long): CartResponse {
        val managedUser = resolveUser(user)
        val cartItem = cartItemService.findById(cartItemId)
        verifyOwnership(cartItem, managedUser)
        val cart = cartItem.cart ?: throw GeneralException.of(ErrorCode.CART_NOT_FOUND)
        cart.removeItem(cartItem)
        cartRepository.save(cart)
        return CartResponse.from(cart)
    }

    @Transactional
    fun clearCart(user: User) {
        val managedUser = resolveUser(user)
        val cart = findCartOrThrow(managedUser)
        cart.clear()
        cartRepository.save(cart)
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
    
    private fun resolveUser(user: User): User {
        val userId = user.id
        return if (userId != null) {
            userRepository.findById(userId)
                .orElseThrow { GeneralException.of(ErrorCode.USER_NOT_FOUND) }
        } else {
            throw GeneralException.of(ErrorCode.USER_NOT_FOUND)
        }
    }

    private fun findItemOrThrow(itemId: Long): Item {
        return itemRepository.findById(itemId)
            .orElseThrow { GeneralException.of(ErrorCode.ITEM_NOT_FOUND) }
    }
}

