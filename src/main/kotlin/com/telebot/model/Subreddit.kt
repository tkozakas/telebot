package com.telebot.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "subreddits")
open class Subreddit {
    @Id
    @Column(name = "subreddit_id", nullable = false)
    open var id: UUID? = null

    @ManyToOne
    @JoinColumn(name = "chat_id")
    open var chat: Chat? = null

    @Column(name = "subreddit_name", nullable = false)
    open var subredditName: String? = null
}
