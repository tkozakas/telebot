package com.telebot.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "subreddits")
open class Subreddit(
    @Id
    @GeneratedValue
    @Column(name = "subreddit_id", nullable = false)
    open var id: UUID? = null,

    @Column(name = "chat_id", nullable = false)
    open var chatId: Long,

    @Column(name = "subreddit_name", nullable = false)
    open var subredditName: String
)
