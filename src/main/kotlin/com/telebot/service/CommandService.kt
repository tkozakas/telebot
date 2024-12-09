package com.telebot.service

import com.telebot.model.UpdateContext

interface CommandService {
    suspend fun handle(update: UpdateContext)
}
