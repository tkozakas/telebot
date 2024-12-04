package com.telebot.handler

import io.github.dehuckakpyt.telegrambot.exception.handler.ExceptionHandler
import io.github.dehuckakpyt.telegrambot.model.telegram.Chat
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CustomExceptionHandler : ExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun execute(chat: Chat, block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            logger.error("Unexpected exception: ${e.message}")
        }
    }
}
