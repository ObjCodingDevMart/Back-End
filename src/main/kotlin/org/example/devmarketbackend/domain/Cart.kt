package org.example.devmarketbackend.domain

import jakarta.persistence.*
import org.example.devmarketbackend.domain.entity.BaseEntity
import java.util.ArrayList

@Entity
@Table(name = "carts")
class Cart protected constructor() : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    var user: User? = null
        internal set

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<CartItem> = ArrayList()

    fun totalAmount(): Int {
        return items.sumOf { it.totalPrice() }
    }

    fun addItem(item: Item, quantity: Int) {
        require(quantity > 0) { "담을 수량은 1 이상이어야 합니다." }
        val existing = items.firstOrNull { it.item?.id == item.id }
        if (existing != null) {
            existing.increaseQuantity(quantity)
        } else {
            items.add(CartItem.create(this, item, quantity))
        }
    }

    fun removeItem(cartItem: CartItem) {
        if (items.remove(cartItem)) {
            cartItem.detach()
        }
    }

    fun clear() {
        items.forEach { it.detach() }
        items.clear()
    }

    companion object {
        fun create(user: User): Cart {
            val cart = Cart()
            cart.user = user
            user.cart = cart
            return cart
        }
    }
}


