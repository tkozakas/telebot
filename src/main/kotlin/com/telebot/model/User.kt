package com.telebot.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
open class User(

    @Id
    @Column(name = "user_id", nullable = false)
    open var userId: Long,

    @Column(name = "username")
    open var username: String? = null,

    @Column(name = "is_winner")
    open var isWinner: Boolean? = null,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    open var stats: MutableSet<Stat> = mutableSetOf(),

    @ManyToMany(mappedBy = "users", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    open var chats: MutableSet<Chat> = mutableSetOf()

)
