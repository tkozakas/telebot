package com.telebot.service

import com.telebot.client.GptClient
import com.telebot.dto.GptRequestDTO
import com.telebot.enums.SubCommand
import com.telebot.handler.TelegramBotActions
import com.telebot.model.Chat
import com.telebot.model.UpdateContext
import com.telebot.properties.GptProperties
import org.springframework.stereotype.Service
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

@Service
class GptService(
    private val gptClient: GptClient,
    private val gptMessageStorageService: GptMessageStorageService,
    private val gptProperties: GptProperties
) : CommandService {

    companion object {
        private const val INVALID_PROMPT = "Please provide a valid argument or prompt after the /gpt command."
        private const val NO_RESPONSE = "GPT did not provide a response. Please try again."
        private const val CHAT_HISTORY_CLEARED = "Chat history cleared."
        private const val CHAT_HISTORY_EMPTY = "Chat history is empty."
    }

    override suspend fun handle(chat: Chat, update: UpdateContext) {
        val chatId = chat.telegramChatId ?: return
        when (update.subCommand) {
            SubCommand.MEMORY.name.lowercase() -> sendChatHistory(chatId, update.bot)
            SubCommand.FORGET.name.lowercase() -> clearChatHistory(chatId, update.bot)
            else -> processChat(chatId, update.args.joinToString(" "), update.bot)
        }
    }

    private suspend fun sendChatHistory(chatId: Long, bot: TelegramBotActions) {
        val historyFile = generateChatHistory(chatId)
        bot.sendDocument(historyFile?.toFile() ?: run { bot.sendMessage(CHAT_HISTORY_EMPTY); return })
    }

    private suspend fun clearChatHistory(chatId: Long, bot: TelegramBotActions) {
        gptMessageStorageService.clearMessages(chatId)
        bot.sendMessage(CHAT_HISTORY_CLEARED)
    }

    private suspend fun processChat(chatId: Long, prompt: String, bot: TelegramBotActions) {
        if (prompt.isBlank()) {
            bot.sendMessage(INVALID_PROMPT)
            return
        }
        val response = processPrompt(chatId, prompt)
        bot.sendMessage(response ?: NO_RESPONSE, parseMode = response?.let { "Markdown" })
    }

    private fun processPrompt(chatId: Long, prompt: String): String? {
        val userMessage = GptRequestDTO.Message(role = "user", content = prompt)
        gptMessageStorageService.addMessage(chatId, userMessage)

        val messages = gptMessageStorageService.getMessages(chatId)
        val gptResponse =
            gptClient.getChatCompletion("Bearer ${gptProperties.token}", GptRequestDTO(messages, gptProperties))
        val botMessage = gptResponse?.choices?.firstOrNull()?.message?.let {
            GptRequestDTO.Message(role = "assistant", content = it.content)
        }
        botMessage?.let { gptMessageStorageService.addMessage(chatId, it) }
        return botMessage?.content
    }

    private fun generateChatHistory(chatId: Long) =
        gptMessageStorageService.getMessages(chatId).takeIf { it.isNotEmpty() }?.let {
            createTempFile(prefix = "chat_history_", suffix = ".txt").apply {
                writeText(it.joinToString("\n") { msg -> "${msg.role}: ${msg.content}" })
            }
        }
}
