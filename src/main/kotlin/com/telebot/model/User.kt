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

    @ManyToMany(mappedBy = "users")
    open var chats: MutableSet<Chat> = mutableSetOf()
)

