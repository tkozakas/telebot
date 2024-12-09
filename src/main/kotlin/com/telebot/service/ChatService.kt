package com.telebot.service

import com.telebot.model.Chat
import com.telebot.repository.ChatRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    @Value("\${ktgram.username}") private val botUsername: String
) {

    fun saveChat(chatId: Long, chatName: String?): Chat {
        val chat = chatRepository.findByTelegramChatId(chatId) ?: Chat(telegramChatId = chatId)
        chat.chatName = chatName?.takeIf(String::isNotBlank) ?: botUsername
        return chatRepository.save(chat)
    }

    fun save(chat: Chat) = chatRepository.save(chat)

    fun findAll(): List<Chat> = chatRepository.findAll()

    fun saveAll(chats: List<Chat>): List<Chat> = chatRepository.saveAll(chats)

    fun findOrCreate(chatId: Long, chatName: String?): Chat {
        return chatRepository.findByTelegramChatId(chatId) ?: saveChat(chatId, chatName)
    }

}
