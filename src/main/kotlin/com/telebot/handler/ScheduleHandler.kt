package com.telebot.handler

import com.telebot.repository.ChatRepository
import com.telebot.service.DailyMessageService
import io.github.dehuckakpyt.telegrambot.TelegramBot
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScheduleHandler(
    private val dailyMessageService: DailyMessageService,
    private val chatRepository: ChatRepository,
    private val bot: TelegramBot
) {
    @OptIn(DelicateCoroutinesApi::class)
    @Scheduled(cron = "\${schedule.daily-message}")
    fun sendScheduledMessage() {
        GlobalScope.launch(Dispatchers.IO) {
            val chats = chatRepository.findAll()
            chats.forEach { chat ->
                chat?.let {
                    it.id?.let { it1 ->
                        TelegramBotActions(
                            chatId = it1, bot = bot,
                            input = null // TODO: Implement input
                        )
                    }?.let { it2 ->
                        dailyMessageService.chooseRandomWinner(
                            chat = it,
                            bot = it2
                        )
                    }
                }
            }
        }
    }

    @Scheduled(cron = "\${schedule.winner-reset}")
    fun resetWinner() {
        dailyMessageService.resetWinners()
    }
}
