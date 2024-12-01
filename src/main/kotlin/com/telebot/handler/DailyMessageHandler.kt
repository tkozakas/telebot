package com.telebot.handler

import com.telebot.enums.Command
import com.telebot.service.DailyMessageService
import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.handler.BotHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import java.time.Year

@HandlerComponent
class DailyMessageHandler(
    private val dailyMessageService: DailyMessageService,
    @Value("\${daily-message.alias}") private val alias: String
) : BotHandler({

    command(Command.DailyMessage.command + alias) {
        val chatId = message.chat.id

        val sentences = dailyMessageService.getRandomGroupSentences()

        val stats = dailyMessageService.getStatByChatIdAndYear(chatId, CURRENT_YEAR)
        val randomWinner = stats.randomOrNull()

        if (stats.isEmpty()) {
            sendMessage(NO_STATS_AVAILABLE)
            return@command
        }

        if (randomWinner != null && randomWinner.isWinner == true) {
            sendMessage(WINNER_ALREADY_EXISTS)
            return@command
        }

        runBlocking {
            sentences.forEachIndexed { index, sentence ->
                launch {
                    delay(1000L * (index + 1))
                    val formattedSentence = if (index == sentences.lastIndex) {
                        sentence.text?.format(randomWinner?.userId ?: "")
                    } else {
                        sentence.text
                    }
                    formattedSentence?.let { sendMessage(it) }
                }
            }
        }

        randomWinner?.let {
            it.userId?.let { it1 -> dailyMessageService.setWinnerByChatIdAndUserIdAndYear(chatId, it1, CURRENT_YEAR) }
        }
    }

}) {
    companion object Constants {
        var CURRENT_YEAR = Year.now().value
        const val NO_STATS_AVAILABLE = "No stats available for this year."
        const val NO_RANDOM_WINNER = "No winner found."
        const val WINNER_ALREADY_EXISTS = "A winner has already been selected for this year."
    }
}

