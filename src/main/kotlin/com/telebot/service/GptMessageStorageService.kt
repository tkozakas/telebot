package com.telebot.service

import com.telebot.dto.GptRequestDTO
import com.telebot.model.GptHistory
import com.telebot.repository.GptHistoryRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GptMessageStorageService(
    private val gptHistoryRepository: GptHistoryRepository
) {

    @Value("\${gpt.initial-prompt}")
    private lateinit var initialPrompt: String

    fun addMessage(chatId: Long, message: GptRequestDTO.Message) {
        val history = gptHistoryRepository.findById(chatId).orElseGet { GptHistory(chatId) }

        if (history.messages.isEmpty() && initialPrompt.isNotBlank()) {
            history.messages.add(GptRequestDTO.Message(role = "system", content = initialPrompt))
        }
        history.messages.add(message)
        gptHistoryRepository.save(history)
    }

    fun getMessages(chatId: Long): List<GptRequestDTO.Message> {
        return gptHistoryRepository.findById(chatId).map { it.messages }.orElse(null) ?: emptyList()
    }

    fun clearMessages(chatId: Long) {
        gptHistoryRepository.deleteById(chatId)
    }
}