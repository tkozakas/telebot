package com.telebot.util

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.api.media.sendAnimation
import eu.vendeli.tgbot.api.media.sendMediaGroup
import eu.vendeli.tgbot.api.media.sendPhoto
import eu.vendeli.tgbot.types.internal.ImplicitFile
import eu.vendeli.tgbot.types.media.InputMedia
import org.springframework.stereotype.Component

@Component
class TelegramMediaSenderImpl : TelegramMediaSender {

    override suspend fun sendPhoto(bot: TelegramBot, chatId: Long, fileUrl: String, caption: String) {
        sendPhoto(ImplicitFile.Str(fileUrl))
            .caption { caption }
            .send(chatId, bot)
    }

    override suspend fun sendAnimation(bot: TelegramBot, chatId: Long, fileUrl: String, caption: String) {
        sendAnimation(ImplicitFile.Str(fileUrl))
            .caption { caption }
            .send(chatId, bot)
    }

    override suspend fun sendMediaGroup(bot: TelegramBot, chatId: Long, mediaGroup: List<InputMedia>) {
        if (mediaGroup.isNotEmpty()) {
            sendMediaGroup(mediaGroup).send(chatId, bot)
        }
    }
}