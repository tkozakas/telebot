package com.telebot.handler

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
        return UpdateContext(update, chat, bot)
    }
}
