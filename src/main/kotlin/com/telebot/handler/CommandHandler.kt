package com.telebot.handler

import com.telebot.enums.CommandRegistry
import com.telebot.service.*
import com.telebot.util.PrinterUtil
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.UpdateHandler
import eu.vendeli.tgbot.api.message.sendMessage
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.types.internal.UpdateType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class CommandHandler(
    private val memeService: MemeService,
    private val stickerService: StickerService,
    private val factService: FactService,
    private val ttsService: TtsService,
    private val dailyMessageService: DailyMessageService,
    private val printerUtil: PrinterUtil,
    private val updateContextFactory: UpdateContextFactory,
    private val gptService: GptService,
    private val commandRegistry: CommandRegistry,
    @Value("\${ktgram.username}") private val botUsername: String
) {
    private val logger = LoggerFactory.getLogger(CommandHandler::class.java)

    @UpdateHandler([UpdateType.MESSAGE])
    suspend fun handle(update: ProcessedUpdate, bot: TelegramBot) {
        try {
            val text = update.text
            var command = text.split(" ").first()
            if (!command.startsWith("/")) {
                return
            }
            if (command.contains("@")) {
                val commandParts = command.split("@")
                if (commandParts.getOrNull(1) == botUsername) {
                    command = commandParts[0]
                }
            }
            val context by lazy { updateContextFactory.create(update, bot) }

            when (command) {
                commandRegistry.gpt -> gptService.handle(context)
                commandRegistry.meme -> memeService.handle(context)
                commandRegistry.sticker -> stickerService.handle(context)
                commandRegistry.fact -> factService.handle(context)
                commandRegistry.tts -> ttsService.handle(context)
                commandRegistry.alias -> dailyMessageService.handle(context)
                commandRegistry.help, commandRegistry.start -> {
                    sendMessage { printerUtil.printHelp() }
                        .options { parseMode = ParseMode.Markdown }
                        .send(context.chat.chatId, bot)
                }
            }
        } catch (e: IllegalStateException) {
            logger.error("Serialization failed for update ${update.updateId}: ${e.message}", e)
        } catch (e: Exception) {
            logger.warn("Failed to handle update: ${update.updateId}. Reason: ${e.message}", e)
        }
    }
}