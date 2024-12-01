package com.telebot.service

import com.telebot.model.Subreddit
import com.telebot.repository.SubredditRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class SubredditService(
    private val subredditRepository: SubredditRepository
) {
    fun findByChatId(chatId: Long): List<Subreddit> {
        return subredditRepository.findByChatId(chatId)
    }

    fun addSubreddit(chatId: Long, subreddit: String) {
        subredditRepository.save(
            Subreddit(
                chatId = chatId,
                subredditName = subreddit
            )
        )
    }

    @Transactional
    fun removeSubreddit(chatId: Long, it: String) {
        subredditRepository.deleteByChatIdAndSubredditName(chatId, it)
    }
}
