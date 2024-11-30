package com.telebot.service

import com.telebot.dto.GptRequestDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GptMessageService {
    @Value("\${gpt.initial-prompt}")
    private val initialPrompt: String? = null

    private val messageStore = mutableMapOf<Long, MutableList<GptRequestDTO.Message>>()

    fun addMessage(chatId: Long, message: GptRequestDTO.Message) {
        val messages = messageStore.computeIfAbsent(chatId) { mutableListOf() }
        when {
            messages.isEmpty() && !initialPrompt.isNullOrBlank() -> {
                messages.add(GptRequestDTO.Message(role = "system", content = initialPrompt))
            }
        }
        messages.add(message)
    }

    fun getMessages(chatId: Long): List<GptRequestDTO.Message> {
        return messageStore[chatId] ?: emptyList()
    }

    fun clearMessages(chatId: Long) {
        messageStore.remove(chatId)
    }
}

