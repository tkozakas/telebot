package com.telebot.handler

import com.telebot.client.RedditClient
import com.telebot.enums.SubCommand
import io.github.dehuckakpyt.telegrambot.handler.BotHandler

class MemeHandler(
    private val redditClient: RedditClient
) : BotHandler({
    command("/meme") {
        val args = message.text?.split(" ") ?: emptyList()

        when (args.getOrNull(1)?.lowercase()) {
            SubCommand.LIST.name.lowercase() -> {
                next("list_memes")
            }
        }

        sendMessage("Meme")
    }

    step("list_memes") {

    }
})
