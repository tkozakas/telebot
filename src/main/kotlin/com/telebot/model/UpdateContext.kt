package com.telebot.model

import eu.vendeli.tgbot.TelegramBot

class UpdateContext(
    val telegramChatId: Long,
    val telegramUserId: Long,
    val telegramUsername: String,
    val args: List<String>,
    val subCommand: String?,
    val chat: Chat,
    val bot: TelegramBot
)
