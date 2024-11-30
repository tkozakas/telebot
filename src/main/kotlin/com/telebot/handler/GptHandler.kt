package com.telebot.handler

import com.telebot.enums.Command
import com.telebot.enums.SubCommand
import com.telebot.service.GptService
import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.factory.input.input
import io.github.dehuckakpyt.telegrambot.handler.BotHandler


@HandlerComponent
class GptHandler(
    private val gptService: GptService
) : BotHandler({
    command(Command.GPT.command, next = "process_prompt") {
        val chatId = message.chat.id
        val username = message.from?.username ?: "User"
        val args = message.text?.split(" ") ?: emptyList()

        when (args.getOrNull(1)?.lowercase()) {
            SubCommand.MEMORY.name.lowercase() -> {
                gptService.getChatHistory(chatId).toFile().takeIf { it.exists() && it.isFile && it.length() > 0 }
                    ?.let { file ->
                        sendDocument(document = input(file), caption = "Chat history")
                    } ?: sendMessage(CHAT_HISTORY_EMPTY)
                return@command
            }

            SubCommand.CLEAR.name.lowercase() -> {
                gptService.clearChatHistory(chatId)
                sendMessage(CHAT_HISTORY_CLEARED)
                return@command
            }
        }

        val userPrompt = args.drop(1).joinToString(" ").takeIf { it.isNotBlank() } ?: run {
            sendMessage(INVALID_PROMPT)
            return@command
        }
        gptService.processPrompt(chatId, username, userPrompt)
            ?.let { botResponse -> sendMessage(botResponse) }
            ?: sendMessage(NO_RESPONSE)
    }
}) {
    companion object Messages {
        const val INVALID_PROMPT = "Please provide a valid argument or prompt after the /gpt command."
        const val NO_RESPONSE = "GPT did not provide a response. Please try again."
        const val CHAT_HISTORY_CLEARED = "Chat history cleared."
        const val CHAT_HISTORY_EMPTY = "Chat history is empty."
    }
}




