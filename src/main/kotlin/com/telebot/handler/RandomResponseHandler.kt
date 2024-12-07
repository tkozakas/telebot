package com.telebot.handler

import com.telebot.enums.Command
import com.telebot.service.ChatService
import com.telebot.service.FactService
import com.telebot.service.StickerService
import io.github.dehuckakpyt.telegrambot.TelegramBot
import io.github.dehuckakpyt.telegrambot.model.telegram.Chat
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class RandomResponseHandler(
    @Value("\${schedule.random-response-chance}") private val randomResponseChance: Double,
    private val factService: FactService,
    private val stickerService: StickerService,
    private val chatService: ChatService
) {

    private fun shouldTriggerRandomResponse() = Random.nextDouble() < randomResponseChance

    private fun selectRandomHandler() = listOf(factService, stickerService).random()

    suspend fun handle(telegramChat: Chat, telegramBot: TelegramBot, text: String?) {
        if (Command.isCommand(text) || !shouldTriggerRandomResponse()) return

        val chat = chatService.saveChat(telegramChat.id, telegramChat.title)
        val bot = TelegramBotActions(chatId = chat.telegramChatId!!, bot = telegramBot, input = null)

        when (val handler = selectRandomHandler()) {
            is FactService -> handler.provideRandomFact(bot)
            is StickerService -> handler.sendRandomSticker(chat, bot)
        }
    }
}
