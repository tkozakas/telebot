package com.telebot.handler

import com.telebot.service.DailyMessageService
import io.github.dehuckakpyt.telegrambot.TelegramBot
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScheduleHandler(
    private val dailyMessageService: DailyMessageService,
    private val bot: TelegramBot
) {
    @Scheduled(cron = "\${schedule.daily-message}")
    fun sendScheduledMessage() {
        val chatIds = dailyMessageService.getChatIds()
        runBlocking {
            chatIds.forEach { chatId ->
                if (chatId != null) {
                    dailyMessageService.chooseRandomWinner(
                        chatId = chatId,
                        sendMessage = { text -> bot.sendMessage(chatId = chatId, text = text, parseMode = "Markdown") }
                    )
                }
            }
        }
    }

    @Scheduled(cron = "\${schedule.winner-reset}")
    fun resetWinner() {
        dailyMessageService.resetWinner()
    }
}
