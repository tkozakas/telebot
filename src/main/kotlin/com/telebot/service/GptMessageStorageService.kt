package com.telebot.service

import com.telebot.dto.GptRequestDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

// TODO redis cache
@Service
class GptMessageStorageService {

    @Value("\${gpt.initial-prompt}")
    private lateinit var initialPrompt: String

    private val messageStore = mutableMapOf<Long, MutableList<GptRequestDTO.Message>>()

    fun addMessage(chatId: Long, message: GptRequestDTO.Message) {
        val messages = messageStore.computeIfAbsent(chatId) { mutableListOf() }
        if (messages.isEmpty() && initialPrompt.isNotBlank()) {
            messages.add(GptRequestDTO.Message(role = "system", content = initialPrompt))
        }
        messages.add(message)
    }

    fun getMessages(chatId: Long): List<GptRequestDTO.Message> =
        messageStore[chatId]?.toList() ?: emptyList()

    fun clearMessages(chatId: Long) {
        messageStore.remove(chatId)
    }
}
