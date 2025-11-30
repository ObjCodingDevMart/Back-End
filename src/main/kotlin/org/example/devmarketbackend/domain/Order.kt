package org.example.devmarketbackend.domain

import jakarta.persistence.*
import org.example.devmarketbackend.domain.entity.BaseEntity

@Entity
@Table(name = "orders")
class Order : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    var id: Long? = null
        private set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null
        internal set
}

