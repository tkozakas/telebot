package com.telebot.handler

import com.telebot.enums.Commands
import com.telebot.service.*
import com.telebot.util.PrinterUtil
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommonHandler
import eu.vendeli.tgbot.api.message.sendMessage
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.types.internal.UpdateType
import org.springframework.stereotype.Component

@Component
class CommandHandler(
    private val factService: FactService,
    private val memeService: MemeService,
    private val gptService: GptService,
    private val dailyMessageService: DailyMessageService,
    private val ttsService: TtsService,
    private val stickerService: StickerService,
    private val printerUtil: PrinterUtil,
    private val updateContextFactory: UpdateContextFactory
) {

    @CommonHandler.Regex("^${Commands.GPT}$", scope = [UpdateType.MESSAGE])
    suspend fun handleGpt(update: ProcessedUpdate, bot: TelegramBot) {
        gptService.handle(updateContextFactory.create(update, bot))
    }

    @CommonHandler.Regex("^${Commands.MEME}$", scope = [UpdateType.MESSAGE])
    suspend fun handleMeme(update: ProcessedUpdate, bot: TelegramBot) {
        memeService.handle(updateContextFactory.create(update, bot))
    }

    @CommonHandler.Regex("^${Commands.STICKER}$", scope = [UpdateType.MESSAGE])
    suspend fun handleSticker(update: ProcessedUpdate, bot: TelegramBot) {
        stickerService.handle(updateContextFactory.create(update, bot))
    }

    @CommonHandler.Regex("^${Commands.FACT}$", scope = [UpdateType.MESSAGE])
    suspend fun handleFact(update: ProcessedUpdate, bot: TelegramBot) {
        factService.handle(updateContextFactory.create(update, bot))
    }

    @CommonHandler.Regex("^${Commands.TTS}$", scope = [UpdateType.MESSAGE])
    suspend fun handleTts(update: ProcessedUpdate, bot: TelegramBot) {
        ttsService.handle(updateContextFactory.create(update, bot))
    }

    @CommonHandler.Regex("^${Commands.DAILY_MESSAGE}$", scope = [UpdateType.MESSAGE])
    suspend fun handleDailyMessage(update: ProcessedUpdate, bot: TelegramBot) {
        dailyMessageService.handle(updateContextFactory.create(update, bot))
    }

    @CommonHandler.Regex("^${Commands.HELP}|${Commands.START}$", scope = [UpdateType.MESSAGE])
    suspend fun handleHelp(user: User, bot: TelegramBot) {
        sendMessage { printerUtil.printHelp() }.options { parseMode = ParseMode.Markdown }.send(user, bot)
    }
}
