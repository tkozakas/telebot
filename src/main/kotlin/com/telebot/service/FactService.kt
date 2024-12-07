package com.telebot.service

import com.telebot.enums.SubCommand
import com.telebot.handler.TelegramBotActions
import com.telebot.model.Chat
import com.telebot.model.Fact
import com.telebot.model.UpdateContext
import com.telebot.repository.ChatRepository
import org.springframework.stereotype.Service

@Service
class FactService(
    private val chatRepository: ChatRepository,
    private val ttsService: TtsService
) : CommandService {

    companion object {
        private const val NO_FACTS = "No facts available."
        private const val FACT_ADDED = "Fact added."
    }

    override suspend fun handle(chat: Chat, update: UpdateContext) {
        val factText = update.args.drop(2).joinToString(" ")

        when (update.subCommand) {
            SubCommand.ADD.name.lowercase() -> addFact(chat, factText, update.bot)
            else -> provideRandomFact(update.bot)
        }
    }

    private suspend fun addFact(chat: Chat, factText: String, bot: TelegramBotActions) {
        if (factText.isBlank()) {
            bot.sendMessage("Fact cannot be blank.")
            return
        }
        saveFact(chat, factText)
        bot.sendMessage(FACT_ADDED)
    }

    private fun saveFact(chat: Chat, factText: String) {
        val fact = Fact(comment = factText, chat = chat)
        chat.facts.add(fact)
        chatRepository.save(chat)
    }

    suspend fun provideRandomFact(bot: TelegramBotActions) {
        when (val randomFact = getRandomFact()) {
            null -> bot.sendMessage(NO_FACTS)
            else -> respondWithFact(bot, randomFact)
        }
    }

    private fun getRandomFact(): String? {
        return chatRepository.findAll()
            .flatMap { it.facts }
            .shuffled()
            .firstOrNull()
            ?.comment
    }

    private suspend fun respondWithFact(bot: TelegramBotActions, fact: String) {
        val audioFile = ttsService.generateAudioFile(fact)
        if (audioFile != null) {
            bot.sendAudio(audioFile, fact)
        } else {
            bot.sendMessage(fact)
        }
    }
}
