package com.telebot.handler

import com.telebot.enums.Command
import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.factory.keyboard.inlineKeyboard
import io.github.dehuckakpyt.telegrambot.handler.BotHandler
import org.springframework.beans.factory.annotation.Value

@HandlerComponent
class MenuHandler(
    @Value("\${daily-message.alias}") private val dailyMessageAlias: String
) : BotHandler({

    command(Command.Menu.command) {
        val buttons = Command.values()
            .filter { !it.listExcluded }
            .map { command ->
                callbackButton(
                    command.command.removePrefix(Command.PREFIX).format(dailyMessageAlias),
                    CALLBACK_CONSTANT,
                    command.command
                )
            }
            .chunked(BUTTONS_PER_ROW)
            .flatten()
            .toTypedArray()

        sendMessage(
            text = MENU_TEXT,
            replyMarkup = inlineKeyboard(*buttons)
        )
    }

    callback(CALLBACK_CONSTANT) {
        val selectedCommandData = transferred<String>()
        val selectedCommand = Command.fromCommand(selectedCommandData.format(dailyMessageAlias))

        sendMessage(
            text = selectedCommand.command,
            parseMode = "HTML"
        )
    }
}) {
    companion object {
        private const val MENU_TEXT = "Choose an option:"
        private const val CALLBACK_CONSTANT = "button"
        private const val BUTTONS_PER_ROW = 2
    }
}
