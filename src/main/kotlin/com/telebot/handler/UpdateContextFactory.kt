package com.telebot.handler

import com.telebot.model.Chat
import com.telebot.model.UpdateContext
import com.telebot.model.User
import com.telebot.service.ChatService
import com.telebot.service.UserService
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UpdateContextFactory(
    @Value("\${ktgram.username}") private val botUsername: String,
    private val chatService: ChatService,
    private val userService: UserService
) {
    @Transactional
    fun create(update: ProcessedUpdate, bot: TelegramBot): UpdateContext {
        val message = update.origin.message
        val messageChat = message?.chat
        val from = message?.from

        val chatId = messageChat?.id ?: 0
        val chatName = messageChat?.title ?: botUsername
        val userId = from?.id ?: 0
        val userName = from?.firstName ?: botUsername

        val chat = chatService.findOrCreate(chatId, chatName)
        val user = userService.findOrCreate(userId, userName)
        chat.users.add(user)

        val args = update.text.drop(1).split(" ")
        val subCommand = args.getOrNull(1)

        return createContext(chat, user, args, subCommand, bot)
    }

    private fun createContext(
        chat: Chat, user: User, args: List<String>, subCommand: String?, bot: TelegramBot
    ): UpdateContext = UpdateContext(chat, user, args, subCommand, bot)
}
