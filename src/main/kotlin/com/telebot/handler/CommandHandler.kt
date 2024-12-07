package com.telebot.handler

import com.telebot.enums.Command
import com.telebot.model.UpdateContext
import com.telebot.service.*
import com.telebot.util.PrinterUtil
import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.factory.input.input
import io.github.dehuckakpyt.telegrambot.handler.BotHandler
import org.springframework.beans.factory.annotation.Value

@HandlerComponent
class CommandHandler(
    private val factService: FactService,
    private val memeService: MemeService,
    private val gptService: GptService,
    private val dailyMessageService: DailyMessageService,
    private val ttsService: TtsService,
    private val stickerService: StickerService,
    private val chatService: ChatService,
    private val printerUtil: PrinterUtil,
    @Value("\${daily-message.alias}") private val alias: String
) : BotHandler({

    command(Command.GPT.command) {
        val update = UpdateContext(message, bot) { file -> input(file) }
        val chat = chatService.saveChat(update.chatId, update.chatName)

        gptService.handle(chat = chat, update = update)
    }

    command(Command.DailyMessage.command.format(alias)) {
        val update = UpdateContext(message, bot) { file -> input(file) }
        val chat = chatService.saveChat(update.chatId, update.chatName)

        dailyMessageService.handle(chat = chat, update = update)
    }

    command(Command.Meme.command) {
        val update = UpdateContext(message, bot) { file -> input(file) }
        val chat = chatService.saveChat(update.chatId, update.chatName)

        memeService.handle(chat = chat, update = update)
    }

    command(Command.Sticker.command) {
        val update = UpdateContext(message, bot) { file -> input(file) }
        val chat = chatService.saveChat(update.chatId, update.chatName)

        stickerService.handle(chat = chat, update = update)
    }

    command(Command.Fact.command) {
        val update = UpdateContext(message, bot) { file -> input(file) }
        val chat = chatService.saveChat(update.chatId, update.chatName)

        factService.handle(chat = chat, update = update)
    }

    command(Command.Tts.command) {
        val update = UpdateContext(message, bot) { file -> input(file) }
        val chat = chatService.saveChat(update.chatId, update.chatName)

        ttsService.handle(chat = chat, update = update)
    }

    command(Command.Help.command) {
        sendMessage(printerUtil.printHelp(), parseMode = "Markdown")
    }

    command(Command.Start.command) {
        sendMessage(printerUtil.printHelp(), parseMode = "Markdown")
    }
})
