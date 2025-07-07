package com.telebot.handler

import com.telebot.enums.CommandConstants
import com.telebot.service.*
import com.telebot.util.PrinterUtil
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommonHandler
import eu.vendeli.tgbot.api.message.sendMessage
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
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
    private val gptService: GptService
) {

    @CommonHandler.Regex(".*${CommandConstants.GPT}.*")
    suspend fun handleGpt(update: ProcessedUpdate, bot: TelegramBot) {
        gptService.handle(updateContextFactory.create(update, bot))
    }

    @CommonHandler.Regex(".*${CommandConstants.MEME}.*")
    suspend fun handleMeme(update: ProcessedUpdate, bot: TelegramBot) {
        memeService.handle(updateContextFactory.create(update, bot))
    }

    @CommonHandler.Regex(".*${CommandConstants.STICKER}.*")
    suspend fun handleSticker(update: ProcessedUpdate, bot: TelegramBot) {
        stickerService.handle(updateContextFactory.create(update, bot))
    }

    @CommonHandler.Regex(".*${CommandConstants.FACT}.*")
    suspend fun handleFact(update: ProcessedUpdate, bot: TelegramBot) {
        factService.handle(updateContextFactory.create(update, bot))
    }

    @CommonHandler.Regex(".*${CommandConstants.TTS}.*")
    suspend fun handleTts(update: ProcessedUpdate, bot: TelegramBot) {
        ttsService.handle(updateContextFactory.create(update, bot))
    }

    @CommonHandler.Regex(".*${CommandConstants.DAILY_MESSAGE}.*")
    suspend fun handleDailyMessage(update: ProcessedUpdate, bot: TelegramBot) {
        dailyMessageService.handle(updateContextFactory.create(update, bot))
    }

    @CommonHandler.Regex(".*${CommandConstants.HELP}.*|.*${CommandConstants.START}.*")
    suspend fun handleHelp(user: User, bot: TelegramBot) {
        sendMessage { printerUtil.printHelp() }
            .options { parseMode = ParseMode.Markdown }
            .send(user.id, bot)
    }

}
