package com.telebot.util

import com.telebot.dto.GptRequestDTO
import com.telebot.dto.GptResponseDTO
import com.telebot.handler.TelegramBotActions
import com.telebot.model.*
import io.github.dehuckakpyt.telegrambot.TelegramBot
import io.github.dehuckakpyt.telegrambot.model.telegram.Message
import io.github.dehuckakpyt.telegrambot.model.telegram.input.ContentInput
import java.io.File

object TestHelper {

    fun createUpdateContext(
        message: Message,
        telegramBot: TelegramBot,
        input: ((File) -> ContentInput)?
    ): UpdateContext {
        return UpdateContext(
            input = input,
            message = message,
            telegramBot = telegramBot
        )
    }

    fun createChat(
        chatId: Long = 123456L,
        chatName: String? = "Test Chat",
        stickers: MutableSet<Sticker> = mutableSetOf(),
        facts: MutableSet<Fact> = mutableSetOf()
    ): Chat {
        return Chat(
            id = chatId,
            chatName = chatName,
            telegramChatId = chatId,
            stickers = stickers,
            facts = facts
        )
    }

    fun createStat(
        userId: Long = 1L,
        username: String = "TestUser",
        year: Int = 2023,
        score: Long = 0,
        isWinner: Boolean = false
    ): Stat {
        return Stat(
            userId = userId,
            username = username,
            year = year,
            score = score,
            isWinner = isWinner
        )
    }


    fun createTelegramBotActions(
        chatId: Long = 123456L,
        telegramBot: TelegramBot
    ): TelegramBotActions {
        return TelegramBotActions(
            chatId = chatId,
            bot = telegramBot,
            input = null
        )
    }

    fun createGptMessages(): List<GptRequestDTO.Message> {
        return listOf(
            GptRequestDTO.Message(role = "user", content = "Hello!"),
            GptRequestDTO.Message(role = "assistant", content = "Hi there!")
        )
    }

    fun createSticker(
        fileId: String = "dummy-file-id",
        stickerSetName: String = "Test Sticker Set",
        emoji: String? = "😊",
        chat: Chat? = null
    ): Sticker {
        return Sticker(
            fileId = fileId,
            stickerSetName = stickerSetName,
            emoji = emoji,
            chat = chat
        )
    }

    fun createFact(
        comment: String = "This is a test fact.",
        chat: Chat? = null
    ): Fact {
        return Fact(
            comment = comment,
            chat = chat
        )
    }

    fun createGptResponse(): GptResponseDTO {
        val message = GptResponseDTO.Choice.Message(role = "assistant", content = "Generated response.")
        val choice = GptResponseDTO.Choice(message = message)
        return GptResponseDTO(choices = listOf(choice))
    }

}
