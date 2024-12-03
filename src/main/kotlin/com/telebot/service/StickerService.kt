package com.telebot.service

import com.telebot.enums.SubCommand
import com.telebot.model.Chat
import com.telebot.model.Sticker
import com.telebot.repository.ChatRepository
import com.telebot.util.PrinterUtil
import io.github.dehuckakpyt.telegrambot.model.telegram.StickerSet
import io.github.dehuckakpyt.telegrambot.model.telegram.input.ContentInput
import jakarta.transaction.Transactional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.io.File

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
        getStickerSet: suspend (String) -> StickerSet,
        sendMessage: suspend (String) -> Unit,
        sendSticker: suspend (String) -> Unit,
        input: (File) -> ContentInput
    ) {
        when (subCommand) {
            SubCommand.LIST.name.lowercase() -> handleListStickers(sendMessage)
            SubCommand.ADD.name.lowercase() -> handleAddSticker(chat, args, sendMessage, getStickerSet)
            SubCommand.REMOVE.name.lowercase() -> handleRemoveSticker(chat, args, sendMessage)
            else -> handleDefaultCommand(chat, sendMessage, sendSticker)
        }
    }

    private suspend fun handleListStickers(sendMessage: suspend (String) -> Unit) {
        val stickers = chatRepository.findAll().flatMap { it.stickers }
        if (stickers.isEmpty()) {
            sendMessage(NO_STICKERS_FOUND)
            return
        }

        val stickerList = printerUtil.printStickers(stickers)
        sendMessage(stickerList)
    }

    private suspend fun handleAddSticker(chat: Chat, args: List<String>,
                                         sendMessage: suspend (String) -> Unit,
                                         getStickerSet: suspend (String) -> StickerSet) {
        val stickerName = args.getOrNull(2)?.removePrefix(TELEGRAM_STICKER_URL)
        if (stickerName.isNullOrBlank()) {
            sendMessage(INVALID_STICKER_NAME)
            return
        }
        if (chat.stickers.any { it.stickerSetName == stickerName }) {
            sendMessage(STICKER_SET_ALREADY_EXISTS.format(stickerName))
            return
        }
        val telegramStickers = getStickerSet(stickerName).stickers
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
        sendMessage(STICKER_ADDED.format(stickerName))
    }

    @Transactional
    suspend fun handleRemoveSticker(chat: Chat, args: List<String>, sendMessage: suspend (String) -> Unit) {
        val stickerName = args.getOrNull(2)
        if (stickerName.isNullOrBlank()) {
            sendMessage(INVALID_STICKER_NAME)
            return
        }
        val stickers = chat.stickers.filter { it.stickerSetName == stickerName }.toSet()
        if (stickers.isEmpty()) {
            sendMessage(NO_STICKERS_FOUND)
            return
        }
        chat.stickers.removeAll(stickers)
        withContext(Dispatchers.IO) {
            chatRepository.save(chat)
        }
        sendMessage(STICKER_REMOVED.format(stickerName))
    }

    private suspend fun handleDefaultCommand(
        chat: Chat,
        sendMessage: suspend (String) -> Unit,
        sendSticker: suspend (String) -> Unit
    ) {
        val sticker = chat.stickers.randomOrNull()
        if (sticker == null) {
            sendMessage(NO_STICKERS_FOUND)
            return
        }
        sticker.fileId?.let { sendSticker(it) }
    }
}
