package com.telebot.service

import com.telebot.enums.SubCommand
import com.telebot.model.Fact
import com.telebot.repository.FactRepository
import org.springframework.stereotype.Service

@Service
class FactService(
    private val factRepository: FactRepository
) {
    companion object Constants {
        const val NO_FACTS = "No facts available."
        const val FACT_ADDED = "Fact added."
    }

    suspend fun handleFactCommand(
        args: List<String>,
        subCommand: String?,
        comment: String,
        sendMessage: suspend (String) -> Unit
    ) {
        when (subCommand) {
            SubCommand.ADD.name.lowercase() -> {
                addFact(comment)
                sendMessage(FACT_ADDED)
            }

            else -> {
                val fact = getRandomFact().takeIf { it.isNotBlank() } ?: NO_FACTS
                sendMessage(fact)
            }
        }
    }

    private fun addFact(fact: String) {
        Fact().apply {
            this.comment = fact
        }.let { factRepository.save(it) }
    }

    fun getRandomFact(): String {
        return factRepository.findRandomFact()
    }
}
