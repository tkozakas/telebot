package com.telebot.handler

import com.telebot.enums.Command
import com.telebot.service.GptService
import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.factory.input.input
import io.github.dehuckakpyt.telegrambot.handler.BotHandler
import java.nio.file.Path

@HandlerComponent
class GptHandler(
    private val gptService: GptService
) : BotHandler({
    command(Command.GPT.command) {
        val chatId = message.chat.id
        val username = message.from?.username ?: "User"
        val args = message.text?.split(" ") ?: emptyList()
        val subCommand = args.getOrNull(1)?.lowercase()

        gptService.handleGptCommand(
            chatId = chatId,
            username = username,
            args = args,
            subCommand = subCommand,
            sendMessage = { text -> sendMessage(text = text) },
            sendDocument = { filePath -> sendDocument(filePath) },
            input = { file -> input(file) }
        )
    }
})
