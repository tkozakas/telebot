package com.telebot.service

import com.telebot.enums.SubCommand
import com.telebot.model.UpdateContext
import com.telebot.repository.StickerRepository
import com.telebot.util.PrinterUtil
import com.telebot.util.TelegramMediaSender
import com.telebot.util.TelegramMessageSender
import eu.vendeli.tgbot.api.stickerset.getStickerSet
import eu.vendeli.tgbot.types.internal.getOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StickerService(
    private val printerUtil: PrinterUtil,
    private val stickerRepository: StickerRepository,
    private val telegramMessageSender: TelegramMessageSender,
    private val telegramMediaSender: TelegramMediaSender
) : CommandService {

    companion object {
        private const val TELEGRAM_STICKER_URL = "https://t.me/addstickers/"
        private const val NO_STICKERS_FOUND = "No stickers found"
        private const val INVALID_STICKER_NAME = "Invalid sticker name"
        private const val STICKER_ADDED = "Sticker set `%s` added"
        private const val STICKER_REMOVED = "Sticker set `%s` removed"
        private const val STICKER_ALREADY_EXISTS = "Sticker set `%s` already exists"
    }

    @Transactional
    override suspend fun handle(update: UpdateContext) {
        when (update.subCommand) {
            SubCommand.LIST.name.lowercase() -> handleListStickers(update)
            SubCommand.ADD.name.lowercase() -> handleAddSticker(update)
            SubCommand.REMOVE.name.lowercase() -> handleRemoveSticker(update)
            else -> handleSendRandomSticker(update)
        }
    }

    private suspend fun handleListStickers(update: UpdateContext) {
        val stickers = stickerRepository.findByChat(update.chat)
        val message = if (stickers.isEmpty()) NO_STICKERS_FOUND else printerUtil.printStickers(stickers)
        telegramMessageSender.send(update.bot, update.chat.chatId, message)
    }

    private suspend fun handleAddSticker(update: UpdateContext) {
        val stickerName = update.args.getOrNull(2)?.removePrefix(TELEGRAM_STICKER_URL)
        if (stickerName.isNullOrBlank()) {
            telegramMessageSender.send(update.bot, update.chat.chatId, INVALID_STICKER_NAME)
            return
        }
        if (stickerRepository.existsStickerByChatAndStickerSetName(update.chat, stickerName)) {
            telegramMessageSender.send(update.bot, update.chat.chatId, STICKER_ALREADY_EXISTS.format(stickerName))
            return
        }
        val stickerSet = getStickerSet(stickerName).sendReturning(update.bot)
        val stickers = stickerSet.getOrNull()?.stickers?.map {
            com.telebot.model.Sticker(
                stickerSetName = stickerName,
                fileId = it.fileId,
                chat = update.chat
            )
        } ?: emptyList()
        stickerRepository.saveAll(stickers)
        telegramMessageSender.send(update.bot, update.chat.chatId, STICKER_ADDED.format(stickerName))
    }

    private suspend fun handleRemoveSticker(update: UpdateContext) {
        val stickerName = update.args.getOrNull(2)?.removePrefix(TELEGRAM_STICKER_URL)
        if (stickerName.isNullOrBlank()) {
            telegramMessageSender.send(update.bot, update.chat.chatId, INVALID_STICKER_NAME)
            return
        }
        if (!stickerRepository.existsStickerByChatAndStickerSetName(update.chat, stickerName)) {
            telegramMessageSender.send(update.bot, update.chat.chatId, INVALID_STICKER_NAME)
            return
        }
        val deletedCount = stickerRepository.deleteStickersByChatAndStickerSetName(update.chat, stickerName)
        val message = if (deletedCount > 0) {
            STICKER_REMOVED.format(stickerName)
        } else {
            INVALID_STICKER_NAME
        }
        telegramMessageSender.send(update.bot, update.chat.chatId, message)
    }

    suspend fun handleSendRandomSticker(update: UpdateContext) {
        val sticker = stickerRepository.findRandomStickerByChatId(update.chat.chatId)
        if (sticker == null) {
            telegramMessageSender.send(update.bot, update.chat.chatId, NO_STICKERS_FOUND)
            return
        }
        telegramMediaSender.sendSticker(
            update.bot,
            update.chat.chatId,
            sticker.fileId!!
        )
    }
}
