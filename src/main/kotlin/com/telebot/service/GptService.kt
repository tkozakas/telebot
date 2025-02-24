package com.telebot.service

import com.telebot.client.GptClient
import com.telebot.dto.GptRequestDTO
import com.telebot.enums.SubCommand
import com.telebot.model.UpdateContext
import com.telebot.properties.GptProperties
import eu.vendeli.tgbot.api.media.sendDocument
import eu.vendeli.tgbot.api.message.sendMessage
import eu.vendeli.tgbot.types.internal.ImplicitFile
import eu.vendeli.tgbot.types.internal.InputFile
import org.springframework.stereotype.Service
import kotlin.io.path.createTempFile
import kotlin.io.path.readBytes
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

    override suspend fun handle(update: UpdateContext) {
        val prompt = update.args.drop(1).joinToString(" ")

        when (update.subCommand?.lowercase()) {
            SubCommand.MEMORY.name.lowercase() -> sendChatHistory(update)
            SubCommand.FORGET.name.lowercase() -> clearChatHistory(update)
            else -> processChat(update, prompt)
        }
    }

    private suspend fun sendChatHistory(update: UpdateContext) {
        val historyFile = generateChatHistory(update.telegramChatId)
        if (historyFile == null) {
            sendMessage { CHAT_HISTORY_EMPTY }.send(update.telegramChatId, update.bot)
        } else {
            sendDocument(ImplicitFile.InpFile(historyFile))
                .caption { "Chat history" }
                .send(update.telegramChatId, update.bot)
        }
    }

    private suspend fun clearChatHistory(update: UpdateContext) {
        gptMessageStorageService.clearMessages(update.telegramChatId)
        sendMessage { CHAT_HISTORY_CLEARED }.send(update.telegramChatId, update.bot)
    }

    private suspend fun processChat(update: UpdateContext, prompt: String) {
        if (prompt.isBlank()) {
            sendMessage { INVALID_PROMPT }.send(update.telegramChatId, update.bot)
            return
        }
        val response = processPrompt(update.telegramChatId, prompt)
        sendMessage { response ?: NO_RESPONSE }.send(update.telegramChatId, update.bot)
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
            InputFile(createTempFile(prefix = "chat_history_", suffix = ".txt").apply {
                writeText(it.joinToString("\n") { msg -> "${msg.role}: ${msg.content}" })
            }.readBytes())
        }
}
