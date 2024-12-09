package com.telebot.service

import com.telebot.enums.SubCommand
import com.telebot.model.UpdateContext
import com.telebot.util.PrinterUtil
import eu.vendeli.tgbot.api.media.sticker
import eu.vendeli.tgbot.api.message.sendMessage
import eu.vendeli.tgbot.api.stickerset.getStickerSet
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.getOrNull
import org.springframework.stereotype.Service

@Service
class StickerService(
    private val printerUtil: PrinterUtil,
    private val chatService: ChatService
) : CommandService {

    companion object {
        private const val TELEGRAM_STICKER_URL = "https://t.me/addstickers/"
        private const val STICKER_SET_ALREADY_EXISTS = "Sticker set %s already exists"
        private const val NO_STICKERS_FOUND = "No stickers found"
        private const val INVALID_STICKER_NAME = "Invalid sticker name"
        private const val STICKER_ADDED = "Sticker %s added"
        private const val STICKER_REMOVED = "Sticker %s removed"
    }

    override suspend fun handle(update: UpdateContext) {
        when (update.subCommand) {
            SubCommand.LIST.name.lowercase() -> listStickers(update)
            SubCommand.ADD.name.lowercase() -> addSticker(update)
            SubCommand.REMOVE.name.lowercase() -> removeSticker(update)
            else -> sendRandomSticker(update)
        }
    }

    private suspend fun listStickers(update: UpdateContext) {
        val stickers = chatService.findAll().flatMap { it.stickers }
        val message = if (stickers.isEmpty()) NO_STICKERS_FOUND else printerUtil.printStickers(stickers)
        sendMessage { message }.options { parseMode = ParseMode.Markdown }.send(update.chatId, update.bot)
    }

    private suspend fun addSticker(update: UpdateContext) {
        val stickerName = update.args.getOrNull(2)?.removePrefix(TELEGRAM_STICKER_URL)
        if (stickerName.isNullOrBlank()) {
            sendMessage { INVALID_STICKER_NAME }.send(update.chatId, update.bot)
            return
        }
        if (update.chat.stickers.any { it.stickerSetName == stickerName }) {
            sendMessage { STICKER_SET_ALREADY_EXISTS.format(stickerName) }.send(update.chatId, update.bot)
            return
        }
        val stickerSet = getStickerSet(stickerName).sendReturning(update.bot)
        val stickers = stickerSet.getOrNull()?.stickers?.map {
            com.telebot.model.Sticker(
                stickerSetName = stickerName,
                fileId = it.fileId,
                emoji = it.emoji,
                chat = update.chat
            )
        }
        if (stickers.isNullOrEmpty()) {
            sendMessage { NO_STICKERS_FOUND }.send(update.chatId, update.bot)
            return
        }
        update.chat.stickers.addAll(stickers)
        chatService.save(update.chat)
        sendMessage { STICKER_ADDED.format(stickerName) }.send(update.chatId, update.bot)
    }

    private suspend fun removeSticker(update: UpdateContext) {
        val stickerName = update.args.getOrNull(2)
        if (stickerName.isNullOrBlank()) {
            sendMessage { INVALID_STICKER_NAME }.send(update.chatId, update.bot)
            return
        }
        val stickers = update.chat.stickers.filter { it.stickerSetName == stickerName }
        if (stickers.isEmpty()) {
            sendMessage { NO_STICKERS_FOUND }.send(update.chatId, update.bot)
            return
        }
        update.chat.stickers.removeAll(stickers.toSet())
        chatService.save(update.chat)
        sendMessage { STICKER_REMOVED.format(stickerName) }.send(update.chatId, update.bot)
    }

    suspend fun sendRandomSticker(update: UpdateContext) {
        val sticker = update.chat.stickers.randomOrNull()
        if (sticker == null || sticker.fileId.isNullOrBlank()) {
            sendMessage { NO_STICKERS_FOUND }.send(update.chatId, update.bot)
        } else {
            sticker { sticker.fileId!! }.send(update.chatId, update.bot)
        }
    }
}
