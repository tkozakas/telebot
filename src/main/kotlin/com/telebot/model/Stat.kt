package com.telebot.model

import jakarta.persistence.*

@Entity
@Table(name = "stats")
open class Stat(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "stats_id", nullable = false)
    open var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "chat_id")
    open var chat: Chat? = null,

    @Column(name = "user_id", nullable = false)
    open var userId: Long? = null,

    @Column(name = "username")
    open var username: String? = null,

    @Column(name = "is_winner")
    open var isWinner: Boolean? = null,

    @Column(name = "score")
    open var score: Long? = null,

    @Column(name = "year")
    open var year: Int? = null,

)
