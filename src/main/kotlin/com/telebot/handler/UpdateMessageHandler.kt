package com.telebot.handler

import com.telebot.enums.Command
import com.telebot.model.UpdateContext
import com.telebot.service.ChatService
import com.telebot.service.DailyMessageService
import com.telebot.service.FactService
import com.telebot.service.StickerService
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.UpdateHandler
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.types.internal.UpdateType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        try {
            logger.info("Message received from chat: ${update.text}")

            if (Command.isCommand(update.text) || !shouldTriggerRandomResponse()) return

            // triggerRandomResponse(context) // TODO find a correct way to implement this
        } catch (e: IllegalStateException) {
            logger.error("Serialization failed for update ${update.updateId}: ${e.message}", e)
        } catch (e: Exception) {
            logger.warn("Failed to handle update: ${update.updateId}. Reason: ${e.message}", e)
        }
    }

    private suspend fun triggerRandomResponse(context: UpdateContext) {
        when (val handler = selectRandomHandler()) {
            is StickerService -> handler.sendRandomSticker(context)
        }
    }

    @Scheduled(cron = "\${schedule.daily-message}")
    fun sendDailyMessage() {
        val chats = chatService.findAll()
        chats.forEach { chat ->
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    dailyMessageService.sendScheduledDailyMessage(updateContextFactory.create(chat, telegramBot))
                }
            } catch (e: Exception) {
                logger.error("Failed to send daily message to chat: ${chat.id}. Reason: ${e.message}", e)
            }
        }
    }

    @Scheduled(cron = "\${schedule.year-end-message}")
    fun sendYearEndMessage() {
        val chats = chatService.findAll()
        chats.forEach { chat ->
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    dailyMessageService.sendYearEndMessage(updateContextFactory.create(chat, telegramBot))
                }
            } catch (e: Exception) {
                logger.error("Failed to send year end message to chat: ${chat.id}. Reason: ${e.message}", e)
            }
        }
    }

    @Scheduled(cron = "\${schedule.winner-reset}")
    fun resetWinner() {
        dailyMessageService.resetWinners()
    }
}
