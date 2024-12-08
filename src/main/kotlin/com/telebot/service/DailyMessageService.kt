package com.telebot.service

import com.telebot.enums.SubCommand
import com.telebot.handler.TelegramBotActions
import com.telebot.model.Chat
import com.telebot.model.Sentence
import com.telebot.model.Stat
import com.telebot.model.UpdateContext
import com.telebot.properties.DailyMessageTemplate
import com.telebot.repository.SentenceRepository
import com.telebot.util.PrinterUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.time.Year

@Service
class DailyMessageService(
    private val sentenceRepository: SentenceRepository,
    private val dailyMessageTemplate: DailyMessageTemplate,
    private val printerUtil: PrinterUtil,
    private val chatService: ChatService
) : CommandService {

    companion object {
        private val CURRENT_YEAR = Year.now().value
        private val RANDOM_DELAY_RANGE = 700L..1200L
    }

    override suspend fun handle(chat: Chat, update: UpdateContext) {
        val bot = update.bot
        val year = update.args.getOrNull(2)?.toIntOrNull() ?: CURRENT_YEAR

        when (update.subCommand?.lowercase()) {
            null -> chooseRandomWinner(chat, bot)
            SubCommand.REGISTER.name.lowercase() -> registerUser(chat, update.userId, update.username, bot)
            SubCommand.ALL.name.lowercase() -> showAllStats(chat, year, bot)
            SubCommand.STATS.name.lowercase() -> showStatsForYear(chat, year, bot)
            else -> showCurrentStats(chat, bot)
        }
    }

    private suspend fun registerUser(chat: Chat, userId: Long, username: String, bot: TelegramBotActions) {
        if (chat.stats.any { it.userId == userId }) {
            bot.sendMessage(dailyMessageTemplate.userAlreadyRegistered)
            return
        }
        saveUser(chat, userId, username)
        bot.sendMessage(dailyMessageTemplate.userRegistered.format(username))
    }

    private fun saveUser(chat: Chat, userId: Long, username: String) {
        chat.stats.add(
            Stat(
                chat = chat,
                userId = userId,
                username = username,
                year = CURRENT_YEAR,
                score = 0,
                isWinner = false
            )
        )
        chatService.save(chat)
    }

    private suspend fun showAllStats(chat: Chat, year: Int, bot: TelegramBotActions) {
        val stats = aggregateStats(chat.stats)
        sendStatsMessage(stats, year, bot, dailyMessageTemplate.statsHeaderAll)
    }

    private suspend fun showStatsForYear(chat: Chat, year: Int, bot: TelegramBotActions) {
        val stats = aggregateStats(chat.stats.filter { it.year == year })
        sendStatsMessage(stats, year, bot, dailyMessageTemplate.statsHeader)
    }

    private suspend fun showCurrentStats(chat: Chat, bot: TelegramBotActions) {
        val stats = aggregateStats(chat.stats.filter { it.year == CURRENT_YEAR })
        sendStatsMessage(stats, CURRENT_YEAR, bot, dailyMessageTemplate.statsHeader)
    }

    private suspend fun sendStatsMessage(
        stats: Map<Long, Stat>,
        year: Int,
        bot: TelegramBotActions,
        headerTemplate: String
    ) {
        if (stats.isEmpty()) {
            bot.sendMessage(dailyMessageTemplate.noStats)
            return
        }
        val message = printerUtil.printStats(
            header = headerTemplate.format(year),
            stats = stats,
            year = year.toString(),
            footer = dailyMessageTemplate.statsFooter.format(stats.size),
            bodyTemplate = dailyMessageTemplate.userStats
        )
        bot.sendMessage(message, parseMode = "Markdown")
    }

    private fun aggregateStats(stats: Collection<Stat>): Map<Long, Stat> {
        return stats.groupBy { it.userId }.mapNotNull { (userId, userStats) ->
            userId?.let {
                it to Stat(
                    userId = it,
                    username = userStats.firstOrNull()?.username,
                    chat = userStats.firstOrNull()?.chat,
                    score = userStats.sumOf { stat -> stat.score ?: 0L },
                    year = userStats.firstOrNull()?.year,
                    isWinner = userStats.firstOrNull()?.isWinner
                )
            }
        }.toMap()
    }

    suspend fun chooseRandomWinner(chat: Chat, bot: TelegramBotActions) {
        val stats = chat.stats
        if (stats.isEmpty()) {
            bot.sendMessage(dailyMessageTemplate.noStats)
            return
        }

        val currentWinner = stats.find { it.isWinner == true && it.year == CURRENT_YEAR }
        if (currentWinner != null) {
            val metionedUser = "[" + currentWinner.username + "](tg://user?id=" + currentWinner.userId + ")";
            bot.sendMessage(
                dailyMessageTemplate.winnerExists.format(dailyMessageTemplate.alias, metionedUser),
                parseMode = "Markdown"
            )
            return
        }

        val sentences = getRandomGroupSentences()
        val winner = stats.filter { it.year == CURRENT_YEAR }.randomOrNull()?.apply { isWinner = true } ?: return
        sendWinnerMessages(sentences, winner, bot)
        updateWinner(chat, winner)
    }

    private suspend fun sendWinnerMessages(sentences: List<Sentence>, winner: Stat, bot: TelegramBotActions) {
        if (sentences.isEmpty()) {
            bot.sendMessage(
                dailyMessageTemplate.winnerExists.format(dailyMessageTemplate.alias, winner.username),
                parseMode = "Markdown"
            )
            return
        }
        runBlocking {
            sentences.sortedBy { it.orderNumber }.forEach { sentence ->
                delay(RANDOM_DELAY_RANGE.random())
                val metionedUser = "[" + winner.username + "](tg://user?id=" + winner.userId + ")";
                sentence.text?.format(dailyMessageTemplate.alias, metionedUser)?.let {
                    bot.sendMessage(it, parseMode = "Markdown")
                }
            }
        }
    }

    private fun updateWinner(chat: Chat, winner: Stat) {
        chat.stats.find { it.userId == winner.userId && it.year == CURRENT_YEAR }?.apply {
            this.score = this.score?.plus(1)
            this.isWinner = true
        }
        chatService.save(chat)
    }

    fun getRandomGroupSentences(): List<Sentence> {
        val groupId = sentenceRepository.findRandomGroupId() ?: return emptyList()
        return sentenceRepository.findSentencesByGroupId(groupId)
    }

    fun resetWinners() {
        val chats = chatService.findAll()
        chats.forEach { chat ->
            chat.stats.forEach { it.isWinner = false }
        }
        chatService.saveAll(chats)
    }
}
