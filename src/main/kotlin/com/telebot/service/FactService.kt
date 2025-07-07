package com.telebot.service

import com.telebot.enums.SubCommand
import com.telebot.model.Fact
import com.telebot.model.UpdateContext
import com.telebot.repository.FactRepository
import com.telebot.util.TelegramMessageSender
import org.springframework.stereotype.Service

@Service
class FactService(
    private val factRepository: FactRepository,
    private val telegramMessageSender: TelegramMessageSender
) : CommandService {

    companion object {
        private const val NO_FACTS = "No facts available."
        private const val FACT_ADDED = "Fact added."
        private const val FACT_BLANK = "Fact cannot be blank."
    }

    override suspend fun handle(update: UpdateContext) {
        when (update.subCommand) {
            SubCommand.ADD.name.lowercase() -> handleAddFact(update)
            else -> handleRandomFact(update)
        }
    }

    private suspend fun handleAddFact(update: UpdateContext) {
        val comment = update.args.drop(2).joinToString(" ").trim()
        if (comment.isBlank()) {
            sendMessage(update, FACT_BLANK)
            return
        }
        factRepository.save(Fact(chat = update.chat, comment = comment))
        sendMessage(update, FACT_ADDED)
    }

    private suspend fun handleRandomFact(update: UpdateContext) {
        val fact = factRepository.findRandomByChatId(update.chat.chatId) ?: run {
            sendMessage(update, NO_FACTS)
            return
        }
        sendMessage(update, fact.comment?: NO_FACTS)
    }

    private suspend fun sendMessage(update: UpdateContext, text: String) {
        telegramMessageSender.send(update.bot, update.chat.chatId, text)
    }
}
