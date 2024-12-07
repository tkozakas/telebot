package com.telebot.service

import com.telebot.enums.SubCommand
import com.telebot.handler.TelegramBotActions
import com.telebot.model.Chat
import com.telebot.model.Sticker
import com.telebot.model.UpdateContext
import com.telebot.repository.ChatRepository
import com.telebot.util.PrinterUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class StickerService(
    private val chatRepository: ChatRepository,
    private val printerUtil: PrinterUtil
) : CommandService {

    companion object {
        private const val TELEGRAM_STICKER_URL = "https://t.me/addstickers/"
        private const val STICKER_SET_ALREADY_EXISTS = "Sticker set %s already exists"
        private const val NO_STICKERS_FOUND = "No stickers found"
        private const val INVALID_STICKER_NAME = "Invalid sticker name"
        private const val STICKER_ADDED = "Sticker %s added"
        private const val STICKER_REMOVED = "Sticker %s removed"
    }

    override suspend fun handle(chat: Chat, update: UpdateContext) {
        when (update.subCommand) {
            SubCommand.LIST.name.lowercase() -> listStickers(update.bot)
            SubCommand.ADD.name.lowercase() -> addSticker(chat, update.args, update.bot)
            SubCommand.REMOVE.name.lowercase() -> removeSticker(chat, update.args, update.bot)
            else -> sendRandomSticker(chat, update.bot)
        }
    }

    private suspend fun listStickers(bot: TelegramBotActions) {
        val stickers = chatRepository.findAll().flatMap { it.stickers }
        val message = if (stickers.isEmpty()) NO_STICKERS_FOUND else printerUtil.printStickers(stickers)
        bot.sendMessage(message, parseMode = if (stickers.isNotEmpty()) "Markdown" else null)
    }

    private suspend fun addSticker(chat: Chat, args: List<String>, bot: TelegramBotActions) {
        val stickerName = args.getOrNull(2)?.removePrefix(TELEGRAM_STICKER_URL)
        if (stickerName.isNullOrBlank()) {
            bot.sendMessage(INVALID_STICKER_NAME)
            return
        }
        if (chat.stickers.any { it.stickerSetName == stickerName }) {
            bot.sendMessage(STICKER_SET_ALREADY_EXISTS.format(stickerName))
            return
        }
        val stickers = bot.getStickerSet(stickerName).stickers.map {
            Sticker(fileId = it.fileId, stickerSetName = stickerName, emoji = it.emoji, chat = chat)
        }
        chat.stickers.addAll(stickers)
        withContext(Dispatchers.IO) { chatRepository.save(chat) }
        bot.sendMessage(STICKER_ADDED.format(stickerName))
    }

    private suspend fun removeSticker(chat: Chat, args: List<String>, bot: TelegramBotActions) {
        val stickerName = args.getOrNull(2)
        if (stickerName.isNullOrBlank()) {
            bot.sendMessage(INVALID_STICKER_NAME)
            return
        }
        val stickers = chat.stickers.filter { it.stickerSetName == stickerName }
        if (stickers.isEmpty()) {
            bot.sendMessage(NO_STICKERS_FOUND)
            return
        }
        chat.stickers.removeAll(stickers.toSet())
        withContext(Dispatchers.IO) { chatRepository.save(chat) }
        bot.sendMessage(STICKER_REMOVED.format(stickerName))
    }

    private suspend fun sendRandomSticker(chat: Chat, bot: TelegramBotActions) {
        val sticker = chat.stickers.randomOrNull()
        if (sticker == null) {
            bot.sendMessage(NO_STICKERS_FOUND)
        } else {
            sticker.fileId?.let { bot.sendSticker(it) }
        }
    }
}
