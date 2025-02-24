package com.telebot.service

import com.telebot.enums.SubCommand
import com.telebot.model.Chat
import com.telebot.model.Fact
import com.telebot.model.UpdateContext
import com.telebot.repository.ChatRepository
import eu.vendeli.tgbot.api.media.sendAudio
import eu.vendeli.tgbot.api.message.sendMessage
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.ImplicitFile
import org.springframework.stereotype.Service

@Service
class FactService(
    private val chatRepository: ChatRepository,
    private val ttsService: TtsService
) : CommandService {

    companion object {
        private const val NO_FACTS = "No facts available."
        private const val FACT_ADDED = "Fact added."
        private const val FACT_BLANK = "Fact cannot be blank."
    }

    override suspend fun handle(update: UpdateContext) {
        val factText = update.args.drop(2).joinToString(" ")

        when (update.subCommand) {
            SubCommand.ADD.name.lowercase() -> addFact(factText, update)
            else -> provideRandomFact(update)
        }
    }

    private suspend fun addFact(factText: String, update: UpdateContext) {
        if (factText.isBlank()) {
            sendMessage { FACT_BLANK }.send(update.telegramChatId, update.bot)
            return
        }
        saveFact(update.chat, factText)
        sendMessage { FACT_ADDED }.send(update.telegramChatId, update.bot)
    }

    private fun saveFact(chat: Chat, factText: String) {
        val fact = Fact(comment = factText, chat = chat)
        chat.facts.add(fact)
        chatRepository.save(chat)
    }

    suspend fun provideRandomFact(update: UpdateContext) {
        val randomFact = getRandomFact()
        if (randomFact == null) {
            sendMessage { NO_FACTS }.send(update.telegramChatId, update.bot)
        } else {
            respondWithFact(randomFact, update)
        }
    }

    private fun getRandomFact(): String? {
        return chatRepository.findAll()
            .flatMap { it.facts }
            .shuffled()
            .firstOrNull()
            ?.comment
    }

    private suspend fun respondWithFact(fact: String, update: UpdateContext) {
        val audioFile = ttsService.generateAudioFile(fact)
        if (audioFile != null) {
            sendAudio(ImplicitFile.InpFile(audioFile))
                .options { parseMode = ParseMode.Markdown }
                .caption { fact }
                .send(update.telegramChatId, update.bot)
        } else {
            sendMessage { fact }
                .options { parseMode = ParseMode.Markdown }
                .send(update.telegramChatId, update.bot)
        }
    }
}
