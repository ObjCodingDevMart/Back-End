package org.example.devmarketbackend.domain

import jakarta.persistence.*
import org.example.devmarketbackend.domain.entity.BaseEntity

@Entity
@Table(name = "cart_items")
class CartItem protected constructor() : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    var id: Long? = null


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    var cart: Cart? = null
        internal set

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    var item: Item? = null

    @Column(nullable = false)
    var quantity: Int = 0


    @Column(nullable = false)
    var unitPrice: Int = 0


    fun increaseQuantity(delta: Int) {
        require(delta > 0) { "증가분은 양수여야 합니다." }
        quantity += delta
    }

    fun changeQuantity(newQuantity: Int) {
        require(newQuantity > 0) { "수량은 1 이상이어야 합니다." }
        quantity = newQuantity
    }

    fun totalPrice(): Int {
        return unitPrice * quantity
    }

    fun detach() {
        cart = null
        item = null
    }

    companion object {
        fun create(cart: Cart, item: Item, quantity: Int): CartItem {
            require(quantity > 0) { "담을 수량은 1 이상이어야 합니다." }
            val cartItem = CartItem()
            cartItem.cart = cart
            cartItem.item = item
            cartItem.quantity = quantity
            cartItem.unitPrice = item.price
            return cartItem
        }
    }
}


