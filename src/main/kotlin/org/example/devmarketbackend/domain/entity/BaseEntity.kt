package org.example.devmarketbackend.domain.entity


import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import lombok.Getter
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.jpa.domain.support.AuditingEntityListener

import java.time.LocalDateTime
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
@Getter
abstract class BaseEntity {


    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    @Column(insertable = false)
    val updatedAt: LocalDateTime= LocalDateTime.now()




}