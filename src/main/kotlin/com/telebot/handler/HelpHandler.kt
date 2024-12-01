package com.telebot.handler

import com.telebot.enums.Command
import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.handler.BotHandler

@HandlerComponent
class HelpHandler : BotHandler({
    command(Command.Help.command) {
        sendMessage(helpMessage())
    }

    command(Command.Start.command) {
        sendMessage(helpMessage())
    }
})

private fun helpMessage(): String {
    return """
        |Available commands:
        |${Command.values().joinToString("\n") { it.command }}
    """.trimMargin()
}
