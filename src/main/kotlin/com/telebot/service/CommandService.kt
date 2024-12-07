package com.telebot.service

import com.telebot.model.Chat
import com.telebot.model.UpdateContext

interface CommandService {
    suspend fun handle(chat: Chat, update: UpdateContext)
}
