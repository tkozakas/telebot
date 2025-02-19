package com.telebot.service

import com.telebot.enums.SubCommand
import com.telebot.model.*
import com.telebot.properties.DailyMessageTemplate
import com.telebot.repository.SentenceRepository
import com.telebot.repository.UserRepository
import com.telebot.util.PrinterUtil
import eu.vendeli.tgbot.api.message.sendMessage
import eu.vendeli.tgbot.types.ParseMode
import kotlinx.coroutines.delay
import org.springframework.stereotype.Service
import java.time.Year

@Service
class DailyMessageService(
    private val sentenceRepository: SentenceRepository,
    private val dailyMessageTemplate: DailyMessageTemplate,
    private val printerUtil: PrinterUtil,
    private val chatService: ChatService,
    private val userRepository: UserRepository
) : CommandService {

    companion object {
        private val CURRENT_YEAR = Year.now().value
        private val RANDOM_DELAY_RANGE = 700L..1200L
    }

    override suspend fun handle(update: UpdateContext) {
        val year = update.args.getOrNull(2)?.toIntOrNull() ?: CURRENT_YEAR
        when (update.subCommand?.lowercase()) {
            null -> chooseRandomWinner(update)
            SubCommand.REGISTER.name.lowercase() -> registerUser(update)
            SubCommand.ALL.name.lowercase() -> showAllStats(year, update)
            SubCommand.STATS.name.lowercase() -> showStatsForYear(year, update)
            else -> showCurrentStats(update)
        }
    }

    private suspend fun registerUser(update: UpdateContext) {
        if (update.chat.stats.any { it.user.id == update.userId }) {
            sendMessage { dailyMessageTemplate.userAlreadyRegistered }
                .send(update.chatId, update.bot)
            return
        }
        saveUser(update.chat, update.userId, update.username)
        sendMessage { dailyMessageTemplate.userRegistered.format(update.username) }
            .send(update.chatId, update.bot)
    }

    private fun saveUser(chat: Chat, userId: Long, username: String) {
        val user = userRepository.findById(userId).orElseGet {
            User(id = userId, username = username, isWinner = false)
                .also { userRepository.save(it) }
        }
        chat.stats.add(
            Stat(
                chat = chat,
                user = user,
                year = CURRENT_YEAR,
                score = 0
            )
        )
        chatService.save(chat)
    }

    private suspend fun showAllStats(year: Int, update: UpdateContext) {
        val stats = aggregateStats(update.chat.stats)
        sendStatsMessage(stats, year, dailyMessageTemplate.statsHeaderAll, update)
    }

    private suspend fun showStatsForYear(year: Int, update: UpdateContext) {
        val stats = aggregateStats(update.chat.stats.filter { it.year == year })
        sendStatsMessage(stats, year, dailyMessageTemplate.statsHeader, update)
    }

    private suspend fun showCurrentStats(update: UpdateContext) {
        val stats = aggregateStats(update.chat.stats.filter { it.year == CURRENT_YEAR })
        sendStatsMessage(stats, CURRENT_YEAR, dailyMessageTemplate.statsHeader, update)
    }

    private fun aggregateStats(stats: Collection<Stat>): Map<Long, Stat> {
        return stats.groupBy { it.user.id }
            .map { (userId, userStats) ->
                val firstStat = userStats.first()
                userId to Stat(
                    chat = firstStat.chat,
                    user = firstStat.user,
                    score = userStats.sumOf { it.score ?: 0L },
                    year = firstStat.year
                )
            }
            .toMap()
    }

    private suspend fun sendStatsMessage(
        stats: Map<Long, Stat>,
        year: Int,
        headerTemplate: String,
        update: UpdateContext
    ) {
        if (stats.isEmpty()) {
            sendMessage { dailyMessageTemplate.noStats }
                .send(update.chatId, update.bot)
            return
        }
        val message = printerUtil.printStats(
            header = headerTemplate.format(year),
            stats = stats,
            year = year.toString(),
            footer = dailyMessageTemplate.statsFooter.format(stats.size),
            bodyTemplate = dailyMessageTemplate.userStats
        )
        sendMessage { message }
            .options { parseMode = ParseMode.Markdown }
            .send(update.chatId, update.bot)
    }

    suspend fun chooseRandomWinner(update: UpdateContext) {
        val stats = update.chat.stats
        if (stats.isEmpty()) {
            sendMessage { dailyMessageTemplate.noStats }
                .send(update.chatId, update.bot)
            return
        }

        val currentWinner = stats.find { it.user.isWinner == true && it.year == CURRENT_YEAR }
        if (currentWinner != null) {
            val mentionedUser = formatUsername(currentWinner)
            sendMessage { dailyMessageTemplate.winnerExists.format(dailyMessageTemplate.alias, mentionedUser) }
                .options { parseMode = ParseMode.Markdown }
                .send(update.chatId, update.bot)
            return
        }

        val sentences = getRandomGroupSentences()
        val winner = stats.filter { it.year == CURRENT_YEAR }
            .randomOrNull()
            ?.apply { user.isWinner = true } ?: return
        sendWinnerMessages(sentences, winner, update)
        updateWinner(update, winner)
    }

    private suspend fun sendWinnerMessages(sentences: List<Sentence>, winner: Stat, update: UpdateContext) {
        if (sentences.isEmpty()) {
            val mentionedUser = formatUsername(winner)
            sendMessage { dailyMessageTemplate.winnerExists.format(dailyMessageTemplate.alias, mentionedUser) }
                .options { parseMode = ParseMode.Markdown }
                .send(update.chatId, update.bot)
            return
        }
        sentences.sortedBy { it.orderNumber }.forEach { sentence ->
            delay(RANDOM_DELAY_RANGE.random())
            val mentionedUser = formatUsername(winner)
            sentence.text?.format(dailyMessageTemplate.alias, mentionedUser)?.let {
                sendMessage { it }
                    .options { parseMode = ParseMode.Markdown }
                    .send(update.chatId, update.bot)
            }
        }
    }

    private fun updateWinner(update: UpdateContext, winner: Stat) {
        update.chat.stats.find { it.user.id == winner.user.id && it.year == CURRENT_YEAR }?.apply {
            this.user.isWinner = true
        }
        chatService.save(update.chat)
        userRepository.save(winner.user)
    }

    fun getRandomGroupSentences(): List<Sentence> {
        val groupId = sentenceRepository.findRandomGroupId() ?: return emptyList()
        return sentenceRepository.findSentencesByGroupId(groupId)
    }

    fun resetWinners() {
        val chats = chatService.findAll()
        chats.forEach { chat ->
            chat.stats.forEach { it.user.isWinner = false }
        }
        chatService.saveAll(chats)
    }

    fun formatUsername(stat: Stat?): String {
        return "[${stat?.user?.username}](tg://user?id=${stat?.user?.id})"
    }

    suspend fun sendScheduledDailyMessage(userContext: UpdateContext) {
        chooseRandomWinner(userContext)
    }

    suspend fun sendYearEndMessage(update: UpdateContext) {
        val year = CURRENT_YEAR
        val stats = aggregateStats(update.chat.stats.filter { it.year == year })
        val winnerOfTheYear = stats.maxByOrNull { it.value.score ?: 0L }?.value
        sendMessage {
            dailyMessageTemplate.yearEndMessage.format(
                dailyMessageTemplate.alias,
                year,
                formatUsername(winnerOfTheYear),
                winnerOfTheYear?.score
            )
        }
            .options { parseMode = ParseMode.Markdown }
            .send(update.chatId, update.bot)
    }
}
