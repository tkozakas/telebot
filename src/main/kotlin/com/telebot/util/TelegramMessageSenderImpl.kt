package com.telebot.util

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.api.message.sendMessage
import eu.vendeli.tgbot.types.ParseMode
import org.springframework.stereotype.Component

@Component
class TelegramMessageSenderImpl : TelegramMessageSender {
    override suspend fun send(bot: TelegramBot, chatId: Long, text: String) {
        sendMessage(text)
            .options { parseMode = ParseMode.Markdown }
            .send(chatId, bot)
    }
}