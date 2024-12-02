package com.telebot.service

import com.telebot.enums.SubCommand
import com.telebot.model.Sentence
import com.telebot.model.Stat
import com.telebot.properties.DailyMessageTemplate
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
    private val sentenceRepository: SentenceRepository,
    private val dailyMessageTemplate: DailyMessageTemplate
) {
    companion object Constants {
        var CURRENT_YEAR = Year.now().value
    }

    suspend fun handleDailyMessage(
        chatId: Long,
        userId: Long,
        username: String,
        subCommand: String?,
        year: Int,
        alias: String,
        sendMessage: suspend (String) -> Unit
    ) {
        if (subCommand.isNullOrBlank()) {
            chooseRandomWinner(chatId, sendMessage)
            return
        }

        val stats = when (subCommand.lowercase()) {
            SubCommand.REGISTER.name.lowercase() -> {
                register(chatId, userId, username, sendMessage)
                return
            }
            SubCommand.ALL.name.lowercase() -> {
                val stats = getStatByChatId(chatId)
                aggregateStats(stats)
            }
            SubCommand.STATS.name.lowercase() -> {
                val stats = getStatByChatIdAndYear(chatId, year)
                aggregateStats(stats)
            }
            else -> {
                val stats = getStatByChatIdAndYear(chatId, CURRENT_YEAR)
                aggregateStats(stats)
            }
        }

        val message = listStats(stats, year.toString())
        sendMessage(message)
    }

    private suspend fun register(
        chatId: Long,
        userId: Long,
        username: String,
        sendMessage: suspend (String) -> Unit
    ) {
        val stats = getStatByChatId(chatId)
        val userStats = stats.find { it.userId == userId }

        if (userStats != null) {
            sendMessage(dailyMessageTemplate.userAlreadyRegistered)
            return
        }

        registerUser(chatId, userId, username)
        sendMessage(dailyMessageTemplate.userRegistered.format(username))
    }

    private suspend fun listStats(
        stats: Map<Long, Stat>,
        year: String
    ): String {
        if (stats.isEmpty()) {
            return dailyMessageTemplate.noStats
        }

        val header = if (year == SubCommand.ALL.name) {
            dailyMessageTemplate.statsHeaderAll.format(year)
        } else {
            dailyMessageTemplate.statsHeader.format(year)
        }

        val body = stats.entries
            .sortedByDescending { it.value.score ?: 0L }
            .withIndex()
            .joinToString("\n") { (index, entry) ->
                val (_, stat) = entry
                "${index + 1}. " + dailyMessageTemplate.userStats.format(
                    stat.username,
                    stat.score ?: 0,
                    if (stat.isWinner == true) "👑" else ""
                )
            }

        val footer = dailyMessageTemplate.statsFooter.format(stats.size)
        return listOf(header, body, footer).joinToString("\n\n")
    }

    private fun aggregateStats(stats: List<Stat>): Map<Long, Stat> {
        return stats.groupBy { it.userId }
            .filterKeys { it != null }
            .mapValues { (_, userStats) ->
                val totalScore = userStats.sumOf { it.score ?: 0L }
                Stat().apply {
                    this.username = userStats.firstOrNull()?.username
                    this.userId = userStats.firstOrNull()?.userId
                    this.chatId = userStats.firstOrNull()?.chatId
                    this.score = totalScore
                    this.year = userStats.firstOrNull()?.year
                    this.isWinner = userStats.any { it.isWinner == true }
                }
            }.mapKeys { it.key!! }
    }

    suspend fun chooseRandomWinner(
        chatId: Long,
        sendMessage: suspend (String) -> Unit
    ) {
        val stats = getStatByChatIdAndYear(chatId, CURRENT_YEAR)
        if (stats.isEmpty()) {
            sendMessage(dailyMessageTemplate.noStats)
            return
        }

        val currentWinner = stats.find { it.isWinner == true && it.chatId == chatId && it.year == CURRENT_YEAR }
        if (currentWinner != null) {
            sendMessage(dailyMessageTemplate.winnerExists.format(currentWinner.username, currentWinner.score))
        } else {
            val randomWinner = stats.randomOrNull()
            randomWinner?.let {
                it.isWinner = true
                sendMessage(dailyMessageTemplate.winnerExists.format(it.username, it.score))
            }
        }

        val sentences = getRandomGroupSentences()
        runBlocking {
            sentences.forEachIndexed { index, sentence ->
                launch {
                    delay(1000L * (index + 1))
                    val formattedSentence =
                        if (index == sentences.lastIndex) sentence.text?.format(currentWinner?.userId ?: "")
                        else sentence.text
                    formattedSentence?.let { sendMessage(it) }
                }
            }
        }

        currentWinner?.let {
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

    fun getStatByChatId(chatId: Long): List<Stat> {
        return statRepository.findByChatId(chatId)
    }

    fun setWinnerByChatIdAndUserIdAndYear(chatId: Long, userId: Long, year: Int) {
        statRepository.setWinnerByChatIdAndUserIdAndYear(chatId, userId, year)
    }

    private fun registerUser(chatId: Long, userId: Long, username: String) {
        Stat().apply {
            this.username = username
            this.chatId = chatId
            this.userId = userId
            this.year = CURRENT_YEAR
            this.isWinner = false
            this.score = 0
        }.let { statRepository.save(it) }
    }
}