package com.telebot.model

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.types.internal.ProcessedUpdate

class UpdateContext(update: ProcessedUpdate, val chat: Chat, val bot: TelegramBot) {

    val chatId: Long = update.origin.message?.chat?.id ?: 0
    val userId: Long = update.origin.message?.from?.id ?: 0
    val username: String = update.origin.message?.from?.username ?: ""
    val args: List<String> = update.text.split(" ")
    val subCommand = args.getOrNull(1)?.lowercase()

}
