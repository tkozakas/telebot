package com.telebot.model

import jakarta.persistence.*


@Entity
@Table(
    name = "stats",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_user_chat_year", columnNames = ["user_id", "chat_id", "year"])
    ]
)
open class Stat(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "stat_id", nullable = false)
    open var statId: Long? = null,

    @Column(name = "score")
    open var score: Long,

    @Column(name = "year")
    open var year: Int,

    @Column(name = "is_winner")
    open var isWinner: Boolean? = false,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    open var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    open var chat: Chat
)
