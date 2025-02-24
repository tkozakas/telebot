package com.telebot.model

import jakarta.persistence.*

@Entity
@Table(name = "stats")
open class Stat(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "stats_id", nullable = false)
    open var id: Long? = null,

    @Column(name = "score")
    open var score: Long? = null,

    @Column(name = "year")
    open var year: Int? = null,

    @Column(name = "is_winner")
    open var isWinner: Boolean? = false,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chat_id", nullable = false)
    open var chat: Chat,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    open var user: User

)
