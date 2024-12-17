package com.telebot.handler

import com.telebot.model.Chat
import com.telebot.model.UpdateContext
import com.telebot.service.ChatService
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import org.springframework.stereotype.Component

@Component
class UpdateContextFactory(
    private val chatService: ChatService
) {
    fun create(update: ProcessedUpdate, bot: TelegramBot): UpdateContext {
        val chat = chatService.findOrCreate(
            update.origin.message?.chat?.id ?: 0,
            update.origin.message?.chat?.title
        )
        val args = update.text.split(" ")
        val subCommand = args.getOrNull(1)
        return UpdateContext(
            chatId = chat.telegramChatId ?: 0,
            userId = update.origin.message?.from?.id ?: 0,
            username = update.origin.message?.from?.username ?: "",
            args = args,
            subCommand = subCommand,
            chat = chat,
            bot = bot
        )
    }

    fun create(chat: Chat, bot: TelegramBot): UpdateContext {
        return UpdateContext(
            chatId = chat.id ?: 0,
            userId = 0,
            username = "",
            args = emptyList(),
            subCommand = null,
            chat = chat,
            bot = bot
        )
    }
}
