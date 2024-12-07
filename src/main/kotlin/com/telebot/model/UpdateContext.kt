package com.telebot.model

import com.telebot.handler.TelegramBotActions
import io.github.dehuckakpyt.telegrambot.TelegramBot
import io.github.dehuckakpyt.telegrambot.model.telegram.Message
import io.github.dehuckakpyt.telegrambot.model.telegram.input.ContentInput
import java.io.File

class UpdateContext(message: Message, telegramBot: TelegramBot, input: ((File) -> ContentInput)?) {
    val chatName: String? = message.chat.title
    val chatId: Long = message.chat.id
    val userId: Long = message.from?.id ?: 0
    val username: String = message.from?.firstName ?: "User"
    val args: List<String> = message.text?.split(" ") ?: emptyList()
    val subCommand = args.getOrNull(1)?.lowercase()
    val bot = TelegramBotActions(chatId = chatId, bot = telegramBot, input = input)

}
