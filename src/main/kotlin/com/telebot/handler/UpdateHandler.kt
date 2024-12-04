package com.telebot.handler

import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.handler.BotUpdateHandler
import org.slf4j.LoggerFactory

@HandlerComponent
class UpdateHandler(private val exceptionHandler: CustomExceptionHandler) : BotUpdateHandler({
    val logger = LoggerFactory.getLogger(javaClass)

    message {
        val chat = chat
        try {
            logger.info("Received message: $text")
        } catch (e: Exception) {
            exceptionHandler.execute(chat) {
                logger.error("Error occurred while processing message: $text", e)
            }
        }
    }

    inlineQuery {
        logger.info("Received query: $query")

        bot.answerInlineQuery(inlineQueryId = id, results = listOf())
    }
}) {
}
