package com.telebot.handler

import com.telebot.enums.Command
import com.telebot.service.ChatService
import com.telebot.service.DailyMessageService
import com.telebot.service.FactService
import com.telebot.service.StickerService
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.UpdateHandler
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.types.internal.UpdateType
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class UpdateMessageHandler(
    @Value("\${schedule.random-response-chance}") private val randomResponseChance: Double,
    private val factService: FactService,
    private val stickerService: StickerService,
    private val dailyMessageService: DailyMessageService,
    private val updateContextFactory: UpdateContextFactory,
    private val chatService: ChatService,
    private val telegramBot: TelegramBot
) {
    private val logger = LoggerFactory.getLogger(UpdateMessageHandler::class.java)
    private fun shouldTriggerRandomResponse() = Random.nextDouble() < randomResponseChance

    private fun selectRandomHandler() = listOf(factService, stickerService).random()

    @UpdateHandler([UpdateType.MESSAGE])
    suspend fun handleUpdate(update: ProcessedUpdate, bot: TelegramBot) {
        val context = updateContextFactory.create(update, bot)
        logger.info("Message received from chat: ${update.text}")

        if (Command.isCommand(update.text) || !shouldTriggerRandomResponse()) return

        when (val handler = selectRandomHandler()) {
            is FactService -> handler.provideRandomFact(context)
            is StickerService -> handler.sendRandomSticker(context)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Scheduled(cron = "\${schedule.daily-message}")
    fun sendDailyMessage() {
        val chats = chatService.findAll()
        chats.forEach { chat ->
            GlobalScope.launch {
                dailyMessageService.sendScheduledDailyMessage(updateContextFactory.create(chat, telegramBot))
            }
        }
    }

    @Scheduled(cron = "\${schedule.winner-reset}")
    fun resetWinner() {
        dailyMessageService.resetWinners()
    }
}
