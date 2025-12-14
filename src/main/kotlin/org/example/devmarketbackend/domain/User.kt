package org.example.devmarketbackend.domain

import jakarta.persistence.*
import org.example.devmarketbackend.domain.entity.BaseEntity
import org.example.devmarketbackend.login.auth.jwt.RefreshToken

@Entity
@Table(name = "users")
class User : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    var id: Long? = null

    // 카카오 고유 ID
    @Column(nullable = false, unique = true)
    var providerId: String = ""

    // 카카오 닉네임 (중복 허용)
    @Column(nullable = false)
    var usernickname: String = ""

    // 휴대폰 번호 (선택 사항, 기본값 null)
    @Column(nullable = true)
    var phoneNumber: String? = null

    // 계정 삭제 가능 여부 (기본값 true)
    @Column(nullable = false)
    var deletable: Boolean = true

    // 마일리지 (기본값 0, 비즈니스 메서드로만 관리)
    // 주의: 직접 할당하지 말고 useMileage(), addMileage() 메서드를 사용하세요
    @Column(nullable = false)
    var maxMileage: Int = 0

    // 최근 총 구매액 (기본값 0, 비즈니스 메서드로만 관리)
    // 주의: 직접 할당하지 말고 updateRecentTotal() 메서드를 사용하세요
    @Column(nullable = false)
    var recentTotal: Int = 0

    @Column(name = "user_profile_url", nullable = true)
    var userProfileUrl: String? = null

    // Refresh Token 관계 설정 (1:1)
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var auth: RefreshToken? = null

    // 장바구니 (사용자당 하나, 삭제 시 물품도 지워짐)
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var cart: Cart? = null

    // 주소 정보 (임베디드 타입)
    @Embedded
    var address: Address? = null

    @Column(nullable = true)
    var email: String? = null

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var Reviews: MutableList<Review> = ArrayList()

    @OneToMany(mappedBy = "user",cascade = [CascadeType.ALL], orphanRemoval = true)
    var likeBookmark: MutableList<LikeBookmark> = ArrayList()


    // 주문 정보 (1:N 관계)
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var orders: MutableList<Order> = ArrayList()

    fun updateAddress(address: Address) {
        this.address = address
    }

    // 주문 추가 메서드
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