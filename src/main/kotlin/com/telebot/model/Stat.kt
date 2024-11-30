package com.telebot.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "stats")
open class Stat {
    @Id
    @Column(name = "stats_id", nullable = false)
    open var id: Long? = null

    @Column(name = "chat_id")
    open var chatId: Long? = null

    @Column(name = "user_id")
    open var userId: Long? = null

    @Column(name = "is_winner")
    open var isWinner: Boolean? = null

    @Column(name = "score")
    open var score: Long? = null

    @Column(name = "year")
    open var year: Int? = null
}
