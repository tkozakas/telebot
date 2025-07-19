package com.telebot.repository

import com.telebot.model.Chat
import com.telebot.model.Subreddit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SubredditRepository : JpaRepository<Subreddit, Long> {
    @Query("""
        SELECT *
          FROM subreddits s
         WHERE s.chat_id = :chatId
            OR s.chat_id IS NULL
         ORDER BY random()
         LIMIT 1
       """,
        nativeQuery = true
    )
    fun findRandomByChatId(chatId: Long) : Subreddit?
    fun findByChat(chat: Chat) : List<Subreddit>
    fun deleteSubredditByChatAndSubredditName(chat: Chat, subredditName: String) : Int
}