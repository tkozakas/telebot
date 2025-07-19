package com.telebot.service

import com.telebot.model.UpdateContext
import com.telebot.repository.StickerRepository
import com.telebot.util.PrinterUtil
import org.springframework.stereotype.Service

@Service
class StickerService(
    private val printerUtil: PrinterUtil,
    private val stickerRepository: StickerRepository
) : CommandService {

    companion object {
        private const val TELEGRAM_STICKER_URL = "https://t.me/addstickers/"
        private const val NO_STICKERS_FOUND = "No stickers found"
        private const val INVALID_STICKER_NAME = "Invalid sticker name"
        private const val STICKER_ADDED = "Sticker set `%s` added"
        private const val STICKER_REMOVED = "Sticker set `%s` removed"
        private const val STICKER_ALREADY_EXISTS = "Sticker set `%s` already exists"
    }

    override suspend fun handle(update: UpdateContext) {
//        when (update.subCommand) {
//            SubCommand.LIST.name.lowercase() -> listStickers(update)
//            SubCommand.ADD.name.lowercase() -> addSticker(update)
//            SubCommand.REMOVE.name.lowercase() -> removeSticker(update)
//            else -> sendRandomSticker(update)
//        }
    }

}
