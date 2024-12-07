package com.telebot.handler

import io.github.dehuckakpyt.telegrambot.TelegramBot
import io.github.dehuckakpyt.telegrambot.model.telegram.StickerSet
import io.github.dehuckakpyt.telegrambot.model.telegram.input.ContentInput
import java.io.File

class TelegramBotActions(
    private val chatId: Long,
    private val bot: TelegramBot,
    val input: ((File) -> ContentInput)?
) : BotActions {

    override suspend fun sendAudio(audio: File, caption: String?) {
        val audioInput = input?.let { it(audio) }
        if (audioInput != null) {
            bot.sendAudio(chatId = chatId, audio = audioInput, caption = caption)
        }
    }

    override suspend fun sendDocument(file: File) {
        val documentInput = input?.let { it(file) }
        if (documentInput != null) {
            bot.sendDocument(chatId = chatId, document = documentInput)
        }
    }

    override suspend fun sendMediaGroup(media: List<Any>) {
        bot.sendMediaGroup(chatId = chatId, media = media)
    }

    override suspend fun sendSticker(sticker: String) {
        bot.sendSticker(chatId = chatId, sticker = sticker)
    }

    override suspend fun getStickerSet(stickerName: String): StickerSet {
        return bot.getStickerSet(stickerName)
    }

    override suspend fun sendMessage(text: String, parseMode: String?) {
        bot.sendMessage(chatId = chatId, text = text, parseMode = parseMode)
    }

}

