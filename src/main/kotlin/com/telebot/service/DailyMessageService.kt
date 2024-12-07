package com.telebot.service

import com.telebot.enums.SubCommand
import com.telebot.handler.TelegramBotActions
import com.telebot.model.Chat
import com.telebot.model.Sentence
import com.telebot.model.Stat
import com.telebot.properties.DailyMessageTemplate
import com.telebot.repository.ChatRepository
import com.telebot.repository.SentenceRepository
import com.telebot.util.PrinterUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.time.Year

@Service
class DailyMessageService(
    private val chatRepository: ChatRepository,
    private val sentenceRepository: SentenceRepository,
    private val dailyMessageTemplate: DailyMessageTemplate,
    private val printerUtil: PrinterUtil
) {
    companion object Constants {
        var CURRENT_YEAR = Year.now().value
        val randomDelayRange = 500L..1000L
    }

    suspend fun handleDailyMessage(
        chat: Chat,
        userId: Long,
        username: String,
        subCommand: String?,
        year: Int,
        bot: TelegramBotActions
    ) {
        if (subCommand.isNullOrBlank()) {
            chooseRandomWinner(chat, bot)
            return
        }

        val stats = when (subCommand.lowercase()) {
            SubCommand.REGISTER.name.lowercase() -> {
                register(chat, userId, username, bot)
                return
            }
            SubCommand.ALL.name.lowercase() -> {
                aggregateStats(chat.stats)
            }
            SubCommand.STATS.name.lowercase() -> {
                val stats = chat.stats.find { it.year == year }?.let { listOf(it) } ?: emptyList()
                aggregateStats(stats)
            }
            else -> {
                val stats = chat.stats.find { it.year == CURRENT_YEAR }?.let { listOf(it) } ?: emptyList()
                aggregateStats(stats)
            }
        }

        val message = listStats(stats, year.toString())
        bot.sendMessage(message, parseMode = "Markdown")
    }

    private suspend fun register(
        chat: Chat,
        userId: Long,
        username: String,
        bot: TelegramBotActions
    ) {
        val userStats = chat.stats.find { it.userId == userId }

        if (userStats != null) {
            bot.sendMessage(dailyMessageTemplate.userAlreadyRegistered)
            return
        }

        registerUser(chat, userId, username)
        bot.sendMessage(dailyMessageTemplate.userRegistered.format(username))
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
        val footer = dailyMessageTemplate.statsFooter.format(stats.size)

        return printerUtil.printStats(header, stats, year, footer)
    }

    private fun aggregateStats(stats: Collection<Stat>): Map<Long, Stat> {
        return stats.groupBy { it.userId }
            .filterKeys { it != null }
            .mapValues { (_, userStats) ->
                val totalScore = userStats.sumOf { it.score ?: 0L }
                Stat().apply {
                    this.username = userStats.firstOrNull()?.username
                    this.userId = userStats.firstOrNull()?.userId
                    this.chat = userStats.firstOrNull()?.chat
                    this.score = totalScore
                    this.year = userStats.firstOrNull()?.year
                    this.isWinner = userStats.any { it.isWinner == true }
                }
            }.mapKeys { it.key!! }
    }

    suspend fun chooseRandomWinner(
        chat: Chat,
        bot: TelegramBotActions
    ) {
        val stats = chat.stats
        if (stats.isEmpty()) {
            bot.sendMessage(dailyMessageTemplate.noStats)
            return
        }

        val currentWinner = stats.find { it.isWinner == true && it.year == CURRENT_YEAR }
        val winner = if (currentWinner != null) {
            bot.sendMessage(dailyMessageTemplate.winnerExists.format(currentWinner.username, currentWinner.score), parseMode = "Markdown")
            return
        } else {
            stats.randomOrNull()?.apply { isWinner = true }
        }

        val sentences = getRandomGroupSentences().sortedBy { it.orderNumber }
        runBlocking {
            sentences.forEachIndexed { _, sentence ->
                val delayTime = randomDelayRange.random()
                delay(delayTime)
                sentence.text?.format(dailyMessageTemplate.alias, winner?.username).let {
                    if (it != null) {
                        bot.sendMessage(it, parseMode = "Markdown")
                    }
                }
            }
        }
        setWinner(chat, winner)
    }

    private fun setWinner(chat: Chat, winner: Stat?) {
        chat.stats.find { it.userId == winner?.userId }?.let {
            it.score = it.score?.plus(1)
            it.isWinner = true
        }
    }

    fun getRandomGroupSentences(): List<Sentence> {
        val groupId = sentenceRepository.findRandomGroupId() ?: return emptyList()
        return sentenceRepository.findSentencesByGroupId(groupId)
    }

    private fun registerUser(chat: Chat, userId: Long, username: String) {
        chat.stats.add(Stat().apply {
            this.chat = chat
            this.userId = userId
            this.username = username
            this.year = CURRENT_YEAR
            this.score = 0
            this.isWinner = false
        })
        chatRepository.save(chat)
    }

    fun resetWinner() {
        val chats = chatRepository.findAll().firstOrNull()
        chats?.stats?.forEach {
            it.isWinner = false
        }
    }
}
