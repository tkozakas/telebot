package com.telebot.model

import jakarta.persistence.*

@Entity
@Table(name = "chats")
open class Chat(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "chat_id", nullable = false)
    open var id: Long? = null,

    @Column(name = "telegram_chat_id", nullable = false)
    open var telegramChatId: Long? = null,

    @Column(name = "chat_name", length = 255)
    open var telegramChatName: String? = null,

    @OneToMany(mappedBy = "chat", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    open var facts: MutableSet<Fact> = mutableSetOf(),

    @OneToMany(mappedBy = "chat", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    open var stickers: MutableSet<Sticker> = mutableSetOf(),

    @OneToMany(mappedBy = "chat", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    open var subreddits: MutableSet<Subreddit> = mutableSetOf(),

    @OneToMany(mappedBy = "chat", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    open var stats: MutableSet<Stat> = mutableSetOf(),

    @OneToMany(mappedBy = "chat", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    open var users: MutableSet<User> = mutableSetOf()

)
