package com.telebot.repository

import com.telebot.model.Chat
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRepository : JpaRepository<Chat, Long> {
    fun findByTelegramChatId(chatId: Long): Chat?
}
