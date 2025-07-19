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
    private val commandRegistry: CommandRegistry
) {
    @UpdateHandler([UpdateType.MESSAGE])
    suspend fun handle(update: ProcessedUpdate, bot: TelegramBot) {
        val text = update.text
        val command = text.split(" ").first()
        val context = updateContextFactory.create(update, bot)

        when (command) {
            commandRegistry.GPT -> gptService.handle(context)
            commandRegistry.MEME -> memeService.handle(context)
            commandRegistry.STICKER -> stickerService.handle(context)
            commandRegistry.FACT -> factService.handle(context)
            commandRegistry.TTS -> ttsService.handle(context)
            commandRegistry.DAILY_MESSAGE -> dailyMessageService.handle(context)
            commandRegistry.HELP, commandRegistry.START -> {
                sendMessage { printerUtil.printHelp() }
                    .options { parseMode = ParseMode.Markdown }
                    .send(context.user.userId, bot)
            }
        }
    }
}