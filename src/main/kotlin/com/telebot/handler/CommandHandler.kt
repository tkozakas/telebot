package com.telebot.handler

import com.telebot.enums.Command
import com.telebot.service.*
import com.telebot.util.PrinterUtil
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.message.sendMessage
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
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

    @CommandHandler([Command.GPT])
    suspend fun handleGpt(update: ProcessedUpdate, bot: TelegramBot) {
        gptService.handle(updateContextFactory.create(update, bot))
    }

    @CommandHandler([Command.MEME])
    suspend fun handleMeme(update: ProcessedUpdate, bot: TelegramBot) {
        memeService.handle(updateContextFactory.create(update, bot))
    }

    @CommandHandler([Command.STICKER])
    suspend fun handleSticker(update: ProcessedUpdate, bot: TelegramBot) {
        stickerService.handle(updateContextFactory.create(update, bot))
    }

    @CommandHandler([Command.FACT])
    suspend fun handleFact(update: ProcessedUpdate, bot: TelegramBot) {
        factService.handle(updateContextFactory.create(update, bot))
    }

    @CommandHandler([Command.TTS])
    suspend fun handleTts(update: ProcessedUpdate, bot: TelegramBot) {
        ttsService.handle(updateContextFactory.create(update, bot))
    }

    @CommandHandler([Command.DAILY_MESSAGE])
    suspend fun handleDailyMessage(update: ProcessedUpdate, bot: TelegramBot) {
        dailyMessageService.handle(updateContextFactory.create(update, bot))
    }

    @CommandHandler([Command.HELP, Command.START])
    suspend fun handleHelp(user: User, bot: TelegramBot) {
        sendMessage { printerUtil.printHelp() }.options { parseMode = ParseMode.Markdown }.send(user, bot)
    }

}
