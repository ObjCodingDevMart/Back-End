package org.example.devmarketbackend.domain

import jakarta.persistence.*
import org.example.devmarketbackend.domain.entity.BaseEntity
import org.example.devmarketbackend.login.auth.jwt.RefreshToken
import java.util.ArrayList

@Entity
@Table(name = "users")
class User : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    var id: Long? = null
        private set

    @Column(nullable = false, unique = true)
    var providerId: String? = null

    @Column(nullable = false)
    var usernickname: String? = null

    @Column
    var phoneNumber: String? = null

    @Column(nullable = false)
    var deletable: Boolean = true

    @Column(nullable = false)
    var maxMileage: Int = 0
        private set

    @Column(nullable = false)
    var recentTotal: Int = 0
        private set

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var auth: RefreshToken? = null

    @Embedded
    var address: Address? = null

    @Column
    var email: String? = null

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val orders: MutableList<Order> = ArrayList()

    fun updateAddress(address: Address) {
        this.address = address
    }

    fun addOrder(order: Order) {
        orders.add(order)
        order.user = this
    }

    fun useMileage(mileage: Int) {
        require(mileage >= 0) { "사용할 마일리지는 0보다 커야 합니다." }
        require(maxMileage >= mileage) { "마일리지가 부족합니다." }
        maxMileage -= mileage
    }

    fun addMileage(mileage: Int) {
        require(mileage >= 0) { "적립할 마일리지는 0보다 커야 합니다." }
        maxMileage += mileage
    }

    fun updateRecentTotal(amount: Int) {
        val newTotal = recentTotal + amount
        require(newTotal >= 0) { "총 결제 금액은 음수가 될 수 없습니다." }
        recentTotal = newTotal
    }
}