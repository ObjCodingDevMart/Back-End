package org.example.devmarketbackend.domain

import io.swagger.v3.oas.models.media.Content
import jakarta.persistence.*
import org.example.devmarketbackend.domain.entity.BaseEntity

@Entity
@Table(name = "reviews")
class Review protected constructor(): BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    var id:Long? = null

    @Column(name = "rating")
    var rating: Int? = null

    @Column(name = "content")
    var content: String? = null

    @Column(name="img_url")
    var imgUrl: String? = null

    @Column(name="img_key")
    var imgKey: String? = null

    @ManyToOne(fetch = FetchType.EAGER)
    var user:User? = null

    @ManyToOne(fetch = FetchType.EAGER)
    var item:Item? = null





    constructor(rating:Int, content: String,user: User,item: Item ,imgUrl: String = "", imgKey: String="")
            :this() {
        this.rating = rating
        this.content = content
        this.imgUrl = imgUrl
        this.imgKey = imgKey
        this.item=item
        this.user=user
    }
}