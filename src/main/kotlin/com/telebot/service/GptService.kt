package com.telebot.service

import com.telebot.client.GptClient
import com.telebot.dto.GptRequestDTO
import com.telebot.dto.GptResponseDTO
import com.telebot.properties.GptProperties
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.writeText

@Service
class GptService(
    private val gptClient: GptClient,
    private val gptMessageService: GptMessageService,
    private val gptProperties: GptProperties
) {
    fun processPrompt(chatId: Long, username: String, prompt: String): String? {
        val userMessage = GptRequestDTO.Message(role = "user", content = "$username: $prompt")
        gptMessageService.addMessage(chatId, userMessage)
        val messages = gptMessageService.getMessages(chatId)
        val gptRequest = GptRequestDTO(messages = messages, gptProperties = gptProperties)
        val gptResponse = gptClient.getChatCompletion(gptRequest)
        val botMessage = extractBotMessage(gptResponse)
        if (botMessage != null) {
            gptMessageService.addMessage(chatId, botMessage)
            return botMessage.content
        }
        return null
    }

    fun clearChatHistory(chatId: Long) {
        gptMessageService.clearMessages(chatId)
    }

    fun getChatHistory(chatId: Long): Path {
        val messages = gptMessageService.getMessages(chatId)
        return kotlin.io.path.createTempFile(prefix = "chat_history_", suffix = ".txt").apply {
            writeText(messages.joinToString("\n") { "${it.role}: ${it.content}" })
        }
    }

    private fun extractBotMessage(gptResponse: GptResponseDTO?): GptRequestDTO.Message? {
        return gptResponse
            ?.choices
            ?.firstOrNull()
            ?.message
            ?.let { GptRequestDTO.Message(role = "assistant", content = it.content) }
    }

}
