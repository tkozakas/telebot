package com.telebot.handler

import com.telebot.enums.Command
import com.telebot.service.ChatService
import com.telebot.service.CommandService
import com.telebot.service.FactService
import com.telebot.service.StickerService
import io.github.dehuckakpyt.telegrambot.TelegramBot
import io.github.dehuckakpyt.telegrambot.model.telegram.Chat
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class RandomResponseHandler(
    @Value("\${schedule.random-response-chance}") private val randomResponseChance: Double,
    private val factService: FactService,
    private val stickerService: StickerService,
    private val chatService: ChatService
) {
    private val random = Random()

    private fun shouldTriggerRandomResponse(): Boolean {
        return random.nextDouble() < randomResponseChance
    }

    private fun selectRandomHandler(): CommandService {
        val handlers = listOf(
            factService,
            stickerService
        )
        val index = random.nextInt(handlers.size)
        return handlers[index]
    }

    suspend fun handle(telegramChat: Chat, telegramBot: TelegramBot, text: String?) {
        if (Command.isCommand(text)) {
            return
        }
        if (!shouldTriggerRandomResponse()) {
            return
        }
        val chat = chatService.saveChat(telegramChat.id, telegramChat.title)
        if (chat.id == null) {
            return
        }

        val bot = TelegramBotActions(chatId = chat.telegramChatId!!, bot = telegramBot, input = null)
        when (selectRandomHandler()) {
            is FactService -> factService.handleDefaultCommand(bot);
            is StickerService -> stickerService.handleDefaultCommand(chat, bot);
        }
    }
}
