package com.telebot.handler

import io.github.dehuckakpyt.telegrambot.TelegramBot
import io.github.dehuckakpyt.telegrambot.exception.chat.ChatException
import io.github.dehuckakpyt.telegrambot.exception.handler.ExceptionHandler
import io.github.dehuckakpyt.telegrambot.model.telegram.Chat
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CustomExceptionHandler(
    private val bot: TelegramBot
) : ExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun execute(chat: Chat, block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            if (e is ChatException) {
                bot.sendMessage(
                    chat.id,
                    "An unknown command has been entered. You can view the possible actions by invoking the /help command."
                )
                return
            }
            logger.error("Unexpected exception in chat: ${chat.title}: ${e.printStackTrace()}")
        }
    }
}
