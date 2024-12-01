package com.telebot.repository

import com.telebot.model.Subreddit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying

interface SubredditRepository : JpaRepository<Subreddit, Long> {
    fun findByChatId(chatId: Long): List<Subreddit>

    @Modifying
    fun deleteByChatIdAndSubredditName(chatId: Long, subredditName: String)
}

