package com.telebot.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "subreddits")
open class Subreddit(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "subreddit_id", nullable = false)
    open var subredditId: UUID? = null,

    @Column(name = "subreddit_name", nullable = false)
    open var subredditName: String? = null,

    @ManyToOne
    @JoinColumn(name = "chat_id")
    open var chat: Chat? = null

)
