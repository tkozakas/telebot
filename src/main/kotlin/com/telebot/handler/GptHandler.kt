package com.telebot.handler

import com.telebot.enums.Command
import com.telebot.enums.SubCommand
import com.telebot.service.GptService
import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.exception.chat.ChatException
import io.github.dehuckakpyt.telegrambot.factory.input.input
import io.github.dehuckakpyt.telegrambot.handler.BotHandler


@HandlerComponent
class GptHandler(
    private val gptService: GptService
) : BotHandler({
    command(Command.GPT.command) {
        val chatId = message.chat.id
        val username = message.from?.username ?: "User"

        val args = message.text?.split(" ") ?: emptyList()
        val userPrompt = args.drop(1).joinToString(" ").takeIf { it.isNotBlank() } ?: run {
            throw ChatException(INVALID_PROMPT)
        }

        args.getOrNull(1)?.uppercase()?.let { subCommand ->
            when (subCommand) {
                SubCommand.MEMORY.name -> {
                    gptService.getChatHistory(chatId).toFile().takeIf { it.exists() && it.isFile && it.length() > 0 }?.let { file ->
                        sendDocument(document = input(file), caption = "Chat history")
                    } ?: sendMessage(CHAT_HISTORY_EMPTY)
                    return@command
                }
                SubCommand.CLEAR.name -> {
                    gptService.clearChatHistory(chatId)
                    sendMessage(CHAT_HISTORY_CLEARED)
                    return@command
                }
                else -> {}
            }
        }

        gptService.processPrompt(chatId, username, userPrompt)
            ?.let { botResponse -> sendMessage(botResponse) }
            ?: throw ChatException(NO_RESPONSE)
    }
}) {
    companion object Messages {
        const val INVALID_PROMPT = "Please provide a prompt after the /gpt command."
        const val NO_RESPONSE = "GPT did not provide a response. Please try again."
        const val CHAT_HISTORY_CLEARED = "Chat history cleared."
        const val CHAT_HISTORY_EMPTY = "Chat history is empty."
    }
}


