package com.telebot.model

import eu.vendeli.tgbot.TelegramBot

class UpdateContext(
    val chatId: Long,
    val userId: Long,
    val username: String,
    val args: List<String>,
    val subCommand: String?,
    val chat: Chat,
    val bot: TelegramBot
)
