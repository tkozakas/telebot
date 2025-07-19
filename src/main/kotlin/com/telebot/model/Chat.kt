package com.telebot.model

import jakarta.persistence.*

@Entity
@Table(name = "chats")
open class Chat(

    @Id
    @Column(name = "chat_id", nullable = false)
    open var chatId: Long,

    @Column(name = "chat_name", length = 255)
    open var chatName: String? = null,

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "chat_users",
        joinColumns = [JoinColumn(name = "chat_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    open var users: MutableSet<User> = mutableSetOf()
)
