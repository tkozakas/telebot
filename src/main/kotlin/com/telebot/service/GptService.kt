package com.telebot.service

import com.telebot.client.GptClient
import com.telebot.dto.GptRequestDTO
import com.telebot.enums.SubCommand
import com.telebot.model.UpdateContext
import com.telebot.properties.GptProperties
import com.telebot.util.TelegramMediaSender
import com.telebot.util.TelegramMessageSender
import eu.vendeli.tgbot.types.internal.InputFile
import org.springframework.stereotype.Service
import kotlin.io.path.createTempFile
import kotlin.io.path.readBytes
import kotlin.io.path.writeText

@Service
class GptService(
    private val gptClient: GptClient,
    private val gptMessageStorageService: GptMessageStorageService,
    private val gptProperties: GptProperties,
    private val telegramMessageSender: TelegramMessageSender,
    private val telegramMediaSender: TelegramMediaSender
) : CommandService {

    companion object {
        private const val INVALID_PROMPT = "Please provide a valid argument or prompt after the /gpt command."
        private const val NO_RESPONSE = "GPT did not provide a response. Please try again."
        private const val CHAT_HISTORY_CLEARED = "Chat history cleared."
        private const val CHAT_HISTORY_EMPTY = "Chat history is empty."
    }

    override suspend fun handle(update: UpdateContext) {
        when (update.subCommand?.lowercase()) {
            SubCommand.MEMORY.name.lowercase() -> handleSendChatHistory(update)
            SubCommand.FORGET.name.lowercase() -> handleClearChatHistory(update)
            else -> handlePrompt(update)
        }
    }

    private suspend fun handleSendChatHistory(update: UpdateContext) {
        val messages = gptMessageStorageService.getMessages(update.chat.chatId)
        if (messages.isEmpty()) {
            telegramMessageSender.send(update.bot, update.chat.chatId, CHAT_HISTORY_EMPTY)
            return
        }
        val historyFile = gptMessageStorageService.getMessages(update.chat.chatId).takeIf { it.isNotEmpty() }?.let {
            InputFile(createTempFile(prefix = "chat_history_", suffix = ".txt").apply {
                writeText(it.joinToString("\n") { msg -> "${msg.role}: ${msg.content}" })
            }.readBytes())
        } ?: return
        telegramMediaSender.sendDocument(
            update.bot,
            update.chat.chatId,
            "Chat History",
            historyFile
        )
    }

    private suspend fun handleClearChatHistory(update: UpdateContext) {
        gptMessageStorageService.clearMessages(update.chat.chatId)
        telegramMessageSender.send(update.bot, update.chat.chatId, CHAT_HISTORY_CLEARED)
    }

    private suspend fun handlePrompt(update: UpdateContext) {
        val prompt = update.args.drop(1).joinToString(" ")

        if (prompt.isBlank()) {
            telegramMessageSender.send(update.bot, update.chat.chatId, INVALID_PROMPT)
            return
        }

        val requestPromptDTO = GptRequestDTO.Message(role = "user", content = prompt)
        gptMessageStorageService.addMessage(update.chat.chatId, requestPromptDTO)
        val gptHistory = gptMessageStorageService.getMessages(update.chat.chatId)
        val gptResponse = gptClient.getChatCompletion(
            "Bearer ${gptProperties.token}",
            GptRequestDTO(gptHistory, gptProperties)
        )
        val botMessage = gptResponse?.choices?.firstOrNull()?.message?.let {
            GptRequestDTO.Message(role = "assistant", content = it.content)
        }
        if (botMessage?.content.isNullOrBlank()) {
            telegramMessageSender.send(update.bot, update.chat.chatId, NO_RESPONSE)
            return
        }
        telegramMessageSender.send(update.bot, update.chat.chatId, botMessage.content)
    }
}
