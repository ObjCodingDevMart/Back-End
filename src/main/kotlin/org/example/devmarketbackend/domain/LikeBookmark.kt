package org.example.devmarketbackend.domain

import org.example.devmarketbackend.domain.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(name = "user_likes")
class LikeBookmark protected constructor():BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="like_id")
    var id:Long? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="user_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var user: User? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="item_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var item: Item? = null

    companion object {
        fun create(user: User, item: Item): LikeBookmark {
            val bookmark = LikeBookmark()
            bookmark.user = user
            bookmark.item = item
            return bookmark
        }
    }
}