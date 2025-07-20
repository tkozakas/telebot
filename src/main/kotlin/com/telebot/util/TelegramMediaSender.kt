package com.telebot.util

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.types.internal.InputFile
import eu.vendeli.tgbot.types.media.InputMedia

interface TelegramMediaSender {
    suspend fun sendPhoto(bot: TelegramBot, chatId: Long, fileUrl: String, caption: String)
    suspend fun sendAnimation(bot: TelegramBot, chatId: Long, fileUrl: String, caption: String)
    suspend fun sendMediaGroup(bot: TelegramBot, chatId: Long, mediaGroup: List<InputMedia>)
    suspend fun sendDocument(bot: TelegramBot, chatId: Long, caption: String, file: InputFile)
    suspend fun sendSticker(bot: TelegramBot, chatId: Long, fileUrl: String)
}