package com.telebot.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "sentence")
open class Sentence {
    @Id
    @Column(name = "sentence_id", nullable = false)
    open var id: UUID? = null

    @Column(name = "group_id")
    open var groupId: UUID? = null

    @Column(name = "order_number", nullable = false)
    open var orderNumber: Int? = null

    @Column(name = "text", length = 1000)
    open var text: String? = null
}
