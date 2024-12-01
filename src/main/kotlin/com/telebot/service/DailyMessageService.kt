package com.telebot.service

import com.telebot.model.Sentence
import com.telebot.model.Stat
import com.telebot.repository.SentenceRepository
import com.telebot.repository.StatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.time.Year

@Service
class DailyMessageService(
    private val statRepository: StatRepository,
    private val sentenceRepository: SentenceRepository
) {
    companion object Constants {
        var CURRENT_YEAR = Year.now().value
        const val NO_STATS_AVAILABLE = "No stats available for this year."
        const val WINNER_ALREADY_EXISTS = "A winner has already been selected for this year."
    }

    suspend fun handleDailyMessage(
        chatId: Long,
        args: List<String>,
        alias: String,
        sendMessage: suspend (String) -> Unit
    ) {
        val sentences = getRandomGroupSentences()
        val stats = getStatByChatIdAndYear(chatId, CURRENT_YEAR)
        val randomWinner = stats.randomOrNull()

        if (stats.isEmpty()) {
            sendMessage(NO_STATS_AVAILABLE)
            return
        }

        if (randomWinner != null && randomWinner.isWinner == true) {
            sendMessage(WINNER_ALREADY_EXISTS)
            return
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
            it.userId?.let { it1 -> setWinnerByChatIdAndUserIdAndYear(chatId, it1, CURRENT_YEAR) }
        }
    }


    fun getRandomGroupSentences(): List<Sentence> {
        val groupIds = sentenceRepository.findGroupIdsByDailyMessageId()
        return sentenceRepository.findRandomSentenceByGroupIds(groupIds)
    }

    fun getStatByChatIdAndYear(chatId: Long, year: Int): List<Stat> {
        return statRepository.findStatByChatIdAndYear(chatId, year)
    }

    fun setWinnerByChatIdAndUserIdAndYear(chatId: Long, userId: Long, year: Int) {
        statRepository.setWinnerByChatIdAndUserIdAndYear(chatId, userId, year)
    }
}
