package com.telebot.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "subreddits")
open class Subreddit {
    @Id
    @Column(name = "subreddit_id", nullable = false)
    open var id: UUID? = null

    @Column(name = "chat_id")
    open var chatId: Long? = null

    @Column(name = "subreddit_name")
    open var subredditName: String? = null
}
