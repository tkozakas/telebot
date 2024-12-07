package com.telebot.service

import com.telebot.client.GptClient
import com.telebot.dto.GptRequestDTO
import com.telebot.dto.GptResponseDTO
import com.telebot.enums.SubCommand
import com.telebot.handler.TelegramBotActions
import com.telebot.model.Chat
import com.telebot.properties.GptProperties
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.writeText

@Service
class GptService(
    private val gptClient: GptClient,
    private val gptMessageStorageService: GptMessageStorageService,
    private val gptProperties: GptProperties
) {
    companion object Constants {
        const val INVALID_PROMPT = "Please provide a valid argument or prompt after the /gpt command."
        const val NO_RESPONSE = "GPT did not provide a response. Please try again."
        const val CHAT_HISTORY_CLEARED = "Chat history cleared."
        const val CHAT_HISTORY_EMPTY = "Chat history is empty."
    }

    suspend fun handleGptCommand(
        chat: Chat,
        username: String,
        args: List<String>,
        subCommand: String?,
        bot: TelegramBotActions
    ) {
        val chatId = chat.telegramChatId ?: return
        when (subCommand) {
            SubCommand.MEMORY.name.lowercase() -> getChatHistory(chatId)?.let { file ->
                bot.sendDocument(file.toFile())
            } ?: bot.sendMessage(CHAT_HISTORY_EMPTY)

            SubCommand.FORGET.name.lowercase() -> {
                clearChatHistory(chatId)
                bot.sendMessage(CHAT_HISTORY_CLEARED)
            }

            else -> args.drop(1).joinToString(" ")
                .takeIf { it.isNotBlank() }
                ?.let { prompt -> processPrompt(chatId, username, prompt, true)?.let { bot.sendMessage(it) } }
                ?: bot.sendMessage(
                    if (args.size <= 1) INVALID_PROMPT else NO_RESPONSE
                )
        }
    }

    fun processPrompt(chatId: Long, username: String, prompt: String, useMemory: Boolean): String? {
        val userMessage = GptRequestDTO.Message(role = "user", content = "$username: $prompt")
        useMemory.let { gptMessageStorageService.addMessage(chatId, userMessage) }
        val messages = gptMessageStorageService.getMessages(chatId)
        val gptRequest = GptRequestDTO(messages = messages, gptProperties = gptProperties)
        val gptResponse = gptClient.getChatCompletion("Bearer " + gptProperties.token, gptRequest)
        val botMessage = extractBotMessage(gptResponse)
        if (botMessage != null) {
            useMemory.let { gptMessageStorageService.addMessage(chatId, botMessage) }
            return botMessage.content
        }
        return null
    }

    private fun clearChatHistory(chatId: Long) {
        gptMessageStorageService.clearMessages(chatId)
    }

    private fun getChatHistory(chatId: Long): Path? {
        val messages = gptMessageStorageService.getMessages(chatId)
        return if (messages.isNotEmpty()) {
            kotlin.io.path.createTempFile(prefix = "chat_history_", suffix = ".txt").apply {
                writeText(messages.joinToString("\n") { "${it.role}: ${it.content}" })
            }
        } else null
    }

    private fun extractBotMessage(gptResponse: GptResponseDTO?): GptRequestDTO.Message? {
        return gptResponse
            ?.choices
            ?.firstOrNull()
            ?.message
            ?.let { GptRequestDTO.Message(role = "assistant", content = it.content) }
    }

}
