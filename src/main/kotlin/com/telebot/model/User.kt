package com.telebot.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
open class User(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id", nullable = false)
    open var id: Long? = null,

    @Column(name = "telegram_user_id", nullable = false)
    open var telegramUserId: Long,

    @Column(name = "username")
    open var telegramUsername: String? = null,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    open val stats: MutableList<Stat> = mutableListOf(),

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chat_id", nullable = false)
    open var chat: Chat
)

