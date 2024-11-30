package com.telebot.model

import jakarta.persistence.*

@Entity
@Table(name = "sentence")
open class Sentence {
    @Id
    @GeneratedValue
    @Column(name = "sentence_id", nullable = false)
    open var id: Long? = null

    @Column(name = "group_id")
    open var groupId: Long? = null

    @Column(name = "order_number", nullable = false)
    open var orderNumber: Int? = null

    @Column(name = "text", length = 1000)
    open var text: String? = null
}
