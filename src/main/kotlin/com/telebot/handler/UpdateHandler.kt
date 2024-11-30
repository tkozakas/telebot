package com.telebot.handler

import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.handler.BotUpdateHandler
import org.slf4j.LoggerFactory

@HandlerComponent
class UpdateHandler : BotUpdateHandler({
    val logger = LoggerFactory.getLogger(javaClass)

    message {
        logger.info("Received message with text \"$text\"")
    }
})
