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
    companion object Constants {
        const val NO_FACTS = "No facts available."
        const val FACT_ADDED = "Fact added."
    }

    override suspend fun handle(chat: Chat, update: UpdateContext) {
        val bot = update.bot
        val comment = update.args.drop(2).joinToString(" ")
        when (update.subCommand) {
            SubCommand.ADD.name.lowercase() -> {
                addFact(chat, comment)
                bot.sendMessage(FACT_ADDED)
            }
            else -> {
                handleDefaultCommand(bot)
            }
        }
    }

    private fun addFact(chat: Chat, fact: String) {
        chat.facts.add(Fact().apply {
            this.comment = fact
            this.chat = chat
        })
        chatRepository.save(chat)
    }

    suspend fun handleDefaultCommand(bot: TelegramBotActions) {
        val facts = chatRepository.findAll().flatMap { it.facts }
        val randomFact = facts.shuffled().firstOrNull()?.comment ?: ""
        if (randomFact.isEmpty()) {
            bot.sendMessage(NO_FACTS)
            return
        }
        val file = ttsService.getAudio(randomFact)
        if (file != null) {
            bot.sendAudio(file, randomFact)
        } else {
            bot.sendMessage(randomFact)
        }
    }

}
