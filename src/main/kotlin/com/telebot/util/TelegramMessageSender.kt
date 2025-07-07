package com.telebot.util

import eu.vendeli.tgbot.TelegramBot

interface TelegramMessageSender {
    suspend fun send(bot: TelegramBot, chatId: Long, text: String)
}