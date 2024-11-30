package com.telebot.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "facts")
open class Fact(
) {
    @Id
    @Column(name = "fact_id", nullable = false)
    open var id: Long? = null

    @Column(name = "comment", length = 3500)
    open var comment: String? = null
}
