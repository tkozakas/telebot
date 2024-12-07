package com.telebot.service

import com.telebot.model.Chat
import com.telebot.repository.ChatRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    @Value("\${telegram-bot.username}") private val botUsername: String,
) {
    fun saveChat(chatId: Long, chatName: String?): Chat {
        val chat = chatRepository.findByTelegramChatId(chatId) ?: Chat().apply {
            telegramChatId = chatId
        }
        chat.chatName = chatName?.takeIf { it.isNotBlank() } ?: botUsername
        return chatRepository.save(chat)
    }
}

