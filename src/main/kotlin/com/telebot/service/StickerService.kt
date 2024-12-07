package com.telebot.service

import com.telebot.enums.SubCommand
import com.telebot.handler.TelegramBotActions
import com.telebot.model.Chat
import com.telebot.model.Sticker
import com.telebot.repository.ChatRepository
import com.telebot.util.PrinterUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class StickerService(
    private val chatRepository: ChatRepository,
    private val printerUtil: PrinterUtil
) {
    companion object Constants {
        const val TELEGRAM_STICKER_URL = "https://t.me/addstickers/"
        const val STICKER_SET_ALREADY_EXISTS = "Sticker set %s already exists"
        const val NO_STICKERS_FOUND = "No stickers found"
        const val INVALID_STICKER_NAME = "Invalid sticker name"
        const val STICKER_ADDED = "Sticker %s added"
        const val STICKER_REMOVED = "Sticker %s removed"
    }

    suspend fun handleStickerCommand(
        chat: Chat,
        args: List<String>,
        subCommand: String?,
        bot: TelegramBotActions,
    ) {
        when (subCommand) {
            SubCommand.LIST.name.lowercase() -> handleListStickers(bot)
            SubCommand.ADD.name.lowercase() -> handleAddSticker(chat, args, bot)
            SubCommand.REMOVE.name.lowercase() -> handleRemoveSticker(chat, args, bot)
            else -> handleDefaultCommand(chat, bot)
        }
    }

    private suspend fun handleListStickers(bot: TelegramBotActions) {
        val stickers = chatRepository.findAll().flatMap { it.stickers }
        if (stickers.isEmpty()) {
            bot.sendMessage(NO_STICKERS_FOUND)
            return
        }

        val stickerList = printerUtil.printStickers(stickers)
        bot.sendMessage(stickerList)
    }

    private suspend fun handleAddSticker(chat: Chat, args: List<String>, bot: TelegramBotActions) {
        val stickerName = args.getOrNull(2)?.removePrefix(TELEGRAM_STICKER_URL)
        if (stickerName.isNullOrBlank()) {
            bot.sendMessage(INVALID_STICKER_NAME)
            return
        }
        if (chat.stickers.any { it.stickerSetName == stickerName }) {
            bot.sendMessage(STICKER_SET_ALREADY_EXISTS.format(stickerName))
            return
        }
        val telegramStickers = bot.getStickerSet(stickerName).stickers
        val stickers = telegramStickers.map { sticker ->
            Sticker().apply {
                this.fileId = sticker.fileId
                this.emoji = sticker.emoji
                this.stickerSetName = stickerName
                this.chat = chat
            }
        }
        chat.stickers.addAll(stickers)
        withContext(Dispatchers.IO) {
            chatRepository.save(chat)
        }
        bot.sendMessage(STICKER_ADDED.format(stickerName))
    }

    suspend fun handleRemoveSticker(chat: Chat, args: List<String>, bot: TelegramBotActions) {
        val stickerName = args.getOrNull(2)
        if (stickerName.isNullOrBlank()) {
            bot.sendMessage(INVALID_STICKER_NAME)
            return
        }
        val stickers = chat.stickers.filter { it.stickerSetName == stickerName }.toSet()
        if (stickers.isEmpty()) {
            bot.sendMessage(NO_STICKERS_FOUND)
            return
        }
        chat.stickers.removeAll(stickers)
        withContext(Dispatchers.IO) {
            chatRepository.save(chat)
        }
        bot.sendMessage(STICKER_REMOVED.format(stickerName))
    }

    private suspend fun handleDefaultCommand(
        chat: Chat,
        bot: TelegramBotActions
    ) {
        val sticker = chat.stickers.randomOrNull()
        if (sticker == null) {
            bot.sendMessage(NO_STICKERS_FOUND)
            return
        }
        sticker.fileId?.let { bot.sendSticker(it) }
    }
}
