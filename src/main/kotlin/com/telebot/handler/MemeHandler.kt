package com.telebot.handler

import com.telebot.enums.Command
import com.telebot.service.MemeService
import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.factory.input.input
import io.github.dehuckakpyt.telegrambot.handler.BotHandler

@HandlerComponent
class MemeHandler(
    private val memeService: MemeService
) : BotHandler({
    command(Command.Meme.command) {
        val chatId = message.chat.id
        val args = message.text?.split(" ") ?: emptyList()

        memeService.handleMemeCommand(
            args = args,
            chatId = chatId,
            sendMessage = { text -> sendMessage(text = text) },
            sendMediaGroup = { media -> sendMediaGroup(media = media) },
            input = { file -> input(file) }
        )
    }

})
