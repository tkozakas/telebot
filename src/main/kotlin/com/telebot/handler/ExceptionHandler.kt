package com.telebot.handler

import io.github.dehuckakpyt.telegrambot.TelegramBot
import io.github.dehuckakpyt.telegrambot.exception.handler.ExceptionHandlerImpl
import io.github.dehuckakpyt.telegrambot.model.telegram.Chat
import io.github.dehuckakpyt.telegrambot.template.MessageTemplate
import io.github.dehuckakpyt.telegrambot.template.Templater

class ExceptionHandler(bot: TelegramBot, template: MessageTemplate, templater: Templater) :
    ExceptionHandlerImpl(bot, template, templater) {

    override suspend fun caught(chat: Chat, ex: Throwable) {
        when (ex) {
            is CustomException -> "Caught CustomException: ${ex.message}"
            else -> super.caught(chat, ex)
        }
    }
}

class CustomException(message: String) : RuntimeException(message)
