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
        val args = update.text.drop(1).split(" ")
        val subCommand = args.getOrNull(1)
        return UpdateContext(
            telegramChatId = chat.telegramChatId ?: 0,
            telegramUserId = update.origin.message?.from?.id ?: 0,
            telegramUsername = update.origin.message?.from?.firstName ?: "",
            args = args,
            subCommand = subCommand,
            chat = chat,
            bot = bot
        )
    }

    fun create(chat: Chat, bot: TelegramBot): UpdateContext {
        return UpdateContext(
            telegramChatId = chat.telegramChatId ?: 0,
            telegramUserId = 0,
            telegramUsername = "",
            args = emptyList(),
            subCommand = null,
            chat = chat,
            bot = bot
        )
    }
}
