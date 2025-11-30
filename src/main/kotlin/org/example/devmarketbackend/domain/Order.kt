package org.example.devmarketbackend.domain

import jakarta.persistence.*
import org.example.devmarketbackend.domain.entity.BaseEntity
import org.example.devmarketbackend.global.constant.OrderStatus

@Entity
@Table(name = "orders") // 예약어 회피
class Order private constructor() : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    var id: Long? = null

    @Column(nullable = false)
    var quantity: Int = 0

    @Column(nullable = false)
    var totalPrice: Int = 0 // 기존 주문 내역을 유지하기 위해

    @Column(nullable = false)
    var finalPrice: Int = 0

    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.PROCESSING

    // Item, User와 연관관계 설정
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    var item: Item? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    var user: User? = null
        internal set

    // 생성자 -> 객체 생성될 때 자동으로 실행! 즉 초기 설정을 할 때 사용
    constructor(user: User, item: Item, quantity: Int) : this() {
        this.user = user
        this.item = item
        this.quantity = quantity
        this.status = OrderStatus.PROCESSING
    }

    companion object {
        // 팩토리 메서드
        fun create(user: User, item: Item, quantity: Int, totalPrice: Int, finalPrice: Int): Order {
            val order = Order(user, item, quantity)
            order.totalPrice = totalPrice
            order.finalPrice = finalPrice
            return order
        }
    }

    // 주문 상태 업데이트
    fun updateStatus(status: OrderStatus) {
        this.status = status
    }
}

