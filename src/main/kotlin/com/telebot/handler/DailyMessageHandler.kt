package com.telebot.handler

import com.telebot.enums.Command
import com.telebot.service.DailyMessageService
import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.handler.BotHandler
import org.springframework.beans.factory.annotation.Value

@HandlerComponent
class DailyMessageHandler(
    private val dailyMessageService: DailyMessageService,
    @Value("\${daily-message.alias}") private val alias: String
) : BotHandler({

    command(Command.DailyMessage.command + alias) {
        val chatId = message.chat.id

        dailyMessageService.handleDailyMessage(chatId, alias) { message ->
            sendMessage(message)
        }
    }

})
