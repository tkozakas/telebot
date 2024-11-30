package com.telebot.handler

import com.telebot.enums.Command
import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.handler.BotHandler

@HandlerComponent
class HelpHandler : BotHandler({
    val help = """
            |Available commands:
            |${Command.values().joinToString("\n") { it.command }}
        """.trimMargin()

    command(Command.Help.command) {
        sendMessage(help)
    }

    command(Command.Start.command) {
        sendMessage(help)
    }

})
