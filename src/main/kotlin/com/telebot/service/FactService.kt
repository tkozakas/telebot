package com.telebot.service

import com.telebot.enums.SubCommand
import com.telebot.model.Chat
import com.telebot.model.Fact
import com.telebot.repository.ChatRepository
import io.github.dehuckakpyt.telegrambot.model.telegram.input.ContentInput
import org.springframework.stereotype.Service
import java.io.File

@Service
class FactService(
    private val chatRepository: ChatRepository,
    private val ttsService: TtsService
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
        sendMessage: suspend (String) -> Unit,
        sendAudio: suspend (ContentInput, String) -> Unit,
        input: (File) -> ContentInput
    ) {
        when (subCommand) {
            SubCommand.ADD.name.lowercase() -> {
                addFact(chat, comment)
                sendMessage(FACT_ADDED)
            }
            else -> {
                val fact = getRandomFact()
                if (fact.isEmpty()) {
                    sendMessage(NO_FACTS)
                    return
                }
                val file = ttsService.getAudio(fact)
                if (file != null) {
                    val contentInput = input(file)
                    sendAudio(contentInput, fact)
                } else {
                    sendMessage(fact)
                }
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
