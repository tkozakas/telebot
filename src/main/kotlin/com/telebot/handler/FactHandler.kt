package com.telebot.handler

import com.telebot.enums.Command
import com.telebot.service.FactService
import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.handler.BotHandler

@HandlerComponent
class FactHandler(
    private val factService: FactService
) : BotHandler({
    command(Command.Fact.command) {
        val fact = factService.getRandomFact()
        sendMessage(fact)
    }
})
