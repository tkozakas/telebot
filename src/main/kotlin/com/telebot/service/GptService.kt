package com.telebot.service

import com.telebot.client.GptClient
import com.telebot.model.UpdateContext
import com.telebot.properties.GptProperties
import org.springframework.stereotype.Service

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

//        when (update.subCommand?.lowercase()) {
//            SubCommand.MEMORY.name.lowercase() -> sendChatHistory(update)
//            SubCommand.FORGET.name.lowercase() -> clearChatHistory(update)
//            else -> processChat(update, prompt)
//        }
    }
}
