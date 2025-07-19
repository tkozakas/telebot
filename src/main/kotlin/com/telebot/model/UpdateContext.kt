package com.telebot.model

import eu.vendeli.tgbot.TelegramBot

class UpdateContext(
    val chat: Chat,
    val user: User,
    val args: List<String>,
    val subCommand: String?,
    val bot: TelegramBot
)
