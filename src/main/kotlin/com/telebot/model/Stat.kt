package com.telebot.model

import jakarta.persistence.*

@Entity
@Table(
    name = "stats",
    uniqueConstraints = [UniqueConstraint(columnNames = ["username", "year"])]
)
open class Stat {
    @Id
    @GeneratedValue
    @Column(name = "stats_id", nullable = false)
    open var id: Long? = null

    @Column(name = "chat_id")
    open var chatId: Long? = null

    @Column(name = "user_id")
    open var userId: Long? = null

    @Column(name = "username")
    open var username: String? = null

    @Column(name = "is_winner")
    open var isWinner: Boolean? = null

    @Column(name = "score")
    open var score: Long? = null

    @Column(name = "year")
    open var year: Int? = null
}
