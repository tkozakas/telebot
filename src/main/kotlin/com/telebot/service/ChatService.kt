package com.telebot.service

import com.telebot.model.Chat
import com.telebot.repository.ChatRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    @Value("\${ktgram.username}") private val botUsername: String
) {

    @Transactional
    fun findOrCreate(chatId: Long, chatName: String?): Chat {
        return chatRepository.findById(chatId).orElseGet {
            val newChat = Chat(chatId, chatName ?: botUsername)
            chatRepository.save(newChat)
        }
    }

}
