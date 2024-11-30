package com.telebot.model

import jakarta.persistence.*

@Entity
@Table(name = "facts")
open class Fact {
    @Id
    @GeneratedValue
    @Column(name = "fact_id", nullable = false)
    open var id: Long? = null

    @Column(name = "comment", length = 3500)
    open var comment: String? = null
}
