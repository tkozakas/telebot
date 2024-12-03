package com.telebot.service

import com.telebot.enums.SubCommand
import com.telebot.model.Chat
import com.telebot.model.Fact
import com.telebot.repository.ChatRepository
import org.springframework.stereotype.Service

@Service
class FactService(
    private val chatRepository: ChatRepository
) {
    companion object Constants {
        const val NO_FACTS = "No facts available."
        const val FACT_ADDED = "Fact added."
    }

    suspend fun handleFactCommand(
        chat: Chat,
        args: List<String>,
        subCommand: String?,
        comment: String,
        sendMessage: suspend (String) -> Unit
    ) {
        when (subCommand) {
            SubCommand.ADD.name.lowercase() -> {
                addFact(chat, comment)
                sendMessage(FACT_ADDED)
            }

            else -> {
                val fact = getRandomFact().takeIf { it.isNotBlank() } ?: NO_FACTS
                sendMessage(fact)
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

    fun getRandomFact(): String {
        val facts = chatRepository.findAll().flatMap { it.facts }
        return facts.shuffled().firstOrNull()?.comment ?: ""
    }
}
