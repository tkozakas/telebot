package com.telebot.handler

import com.telebot.enums.Command
import com.telebot.model.Chat
import com.telebot.repository.ChatRepository
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
    private val chatRepository: ChatRepository,
    private val printerUtil: PrinterUtil,
    @Value("\${telegram-bot.username}") private val botUsername: String,
    @Value("\${daily-message.alias}") private val alias: String
) : BotHandler({

    command(Command.GPT.command) {
        val chatName = message.chat.title
        val chatId = message.chat.id
        val username = message.from?.username ?: "User"
        val args = message.text?.split(" ") ?: emptyList()
        val subCommand = args.getOrNull(1)?.lowercase()
        val chat = getChat(chatRepository, chatId, chatName, botUsername)

        gptService.handleGptCommand(
            chat = chat,
            username = username,
            args = args,
            subCommand = subCommand, sendMessage = { text -> sendMessage(text = text, parseMode = "Markdown") },
            sendDocument = { filePath -> sendDocument(filePath) },
            input = { file -> input(file) }
        )
    }

    command(Command.DailyMessage.command.format(alias)) {
        val chatName = message.chat.title
        val chatId = message.chat.id
        val userId = message.from?.id ?: 0
        val username = message.from?.username ?: "User"
        val args = message.text?.split(" ") ?: emptyList()
        val subCommand = args.getOrNull(1)?.lowercase()
        val year = args.getOrNull(2)?.toIntOrNull() ?: DailyMessageService.CURRENT_YEAR
        val chat = getChat(chatRepository, chatId, chatName, botUsername)

        dailyMessageService.handleDailyMessage(
            chat = chat,
            userId = userId,
            username = username,
            subCommand = subCommand,
            year = year, sendMessage = { text -> sendMessage(text = text, parseMode = "Markdown") }
        )
    }

    command(Command.Meme.command) {
        val chatName = message.chat.title
        val chatId = message.chat.id
        val args = message.text?.split(" ") ?: emptyList()
        val subCommand = args.getOrNull(1)?.lowercase()
        val chat = getChat(chatRepository, chatId, chatName, botUsername)

        memeService.handleMemeCommand(
            chat = chat,
            args = args,
            subCommand = subCommand,
            sendMessage = { text -> sendMessage(text = text, parseMode = "Markdown") },
            sendMediaGroup = { media -> sendMediaGroup(media = media) },
            input = { file -> input(file) })
    }

    command(Command.Sticker.command) {
        val chatName = message.chat.title
        val chatId = message.chat.id
        val args = message.text?.split(" ") ?: emptyList()
        val subCommand = args.getOrNull(1)?.lowercase()
        val chat = getChat(chatRepository, chatId, chatName, botUsername)

        stickerService.handleStickerCommand(
            chat = chat,
            args = args,
            subCommand = subCommand,
            getStickerSet = { stickerSetName -> getStickerSet(stickerSetName) },
            sendMessage = { text -> sendMessage(text = text, parseMode = "Markdown") },
            sendSticker = { sticker -> sendSticker(sticker = sticker) },
            input = { file -> input(file) }
        )
    }

    command(Command.Fact.command) {
        val chatName = message.chat.title
        val chatId = message.chat.id
        val args = message.text?.split(" ") ?: emptyList()
        val subCommand = args.getOrNull(1)?.lowercase()
        val comment = args.drop(2).joinToString(" ")
        val chat = getChat(chatRepository, chatId, chatName, botUsername)

        factService.handleFactCommand(
            chat = chat,
            args = args,
            subCommand = subCommand,
            comment = comment,
            sendMessage = { text -> sendMessage(text = text) }
        )
    }

    command(Command.Tts.command) {
        val messageText = message.text?.substringAfter(" ") ?: ""

        ttsService.handleTtsCommand(
            messageText = messageText,
            sendAudio = { audio -> sendAudio(audio = audio) },
            sendMessage = { text -> sendMessage(text = text) },
            input = { file -> input(file) }
        )
    }

    command(Command.Help.command) {
        sendMessage(printerUtil.printHelp(), parseMode = "Markdown")
    }

    command(Command.Start.command) {
        sendMessage(printerUtil.printHelp(), parseMode = "Markdown")
    }
})

private fun getChat(chatRepository: ChatRepository, chatId: Long, chatName: String?, username: String): Chat {
    val chat = chatRepository.findByTelegramChatId(chatId) ?: Chat().apply {
        telegramChatId = chatId
    }
    chat.chatName = chatName?.takeIf { it.isNotBlank() } ?: username
    return chatRepository.save(chat)
}

