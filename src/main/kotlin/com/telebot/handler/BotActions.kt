package com.telebot.handler

import io.github.dehuckakpyt.telegrambot.model.telegram.InputMedia
import io.github.dehuckakpyt.telegrambot.model.telegram.StickerSet
import java.io.File

interface BotActions {
    suspend fun sendMessage(text: String, parseMode: String? = null)
    suspend fun sendAudio(audio: File, caption: String? = null)
    suspend fun sendMediaGroup(media: List<InputMedia>)
    suspend fun sendDocument(file: File)
    suspend fun sendSticker(sticker: String)
    suspend fun getStickerSet(stickerName: String): StickerSet
}
