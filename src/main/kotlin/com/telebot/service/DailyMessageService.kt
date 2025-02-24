package com.telebot.service

import com.telebot.enums.SubCommand
import com.telebot.model.Sentence
import com.telebot.model.Stat
import com.telebot.model.UpdateContext
import com.telebot.model.User
import com.telebot.properties.DailyMessageTemplate
import com.telebot.repository.SentenceRepository
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
    private val userService: UserService
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
        if (update.chat.stats.any { it.user.telegramUserId == update.telegramUserId }) {
            sendMessage { dailyMessageTemplate.userAlreadyRegistered }
                .send(update.telegramChatId, update.bot)
            return
        }
        val user = User(chat = update.chat, telegramUserId = update.telegramUserId, telegramUsername = update.telegramUsername)
        val newStat = Stat(chat = update.chat, user = user, score = 0, year = CURRENT_YEAR)
        user.stats.add(newStat)
        userService.saveUser(user)
        sendMessage { dailyMessageTemplate.userRegistered.format(formatUsername(update.telegramUsername, update.telegramUserId)) }
            .options { parseMode = ParseMode.Markdown }
            .send(update.telegramChatId, update.bot)
    }

    private suspend fun chooseRandomWinner(update: UpdateContext) {
        val stats = update.chat.stats
        if (stats.isEmpty()) {
            sendMessage { dailyMessageTemplate.noStats }
                .send(update.telegramChatId, update.bot)
            return
        }
        val currentWinner = stats.find { it.isWinner == true }
        if (currentWinner != null) {
            chosenWinnerMessage(currentWinner.user, update)
            return
        }
        if (stats.isEmpty()) {
            sendMessage { dailyMessageTemplate.noStats }
                .send(update.telegramChatId, update.bot)
            return
        }
        val users = userService.findUsersByChat(update.chat)
        val winner = users.random()
        val sentences = getRandomGroupSentences()
        sendWinnerMessages(sentences, winner, update)
        updateWinner(winner)
    }

    private suspend fun chosenWinnerMessage(winner: User, update: UpdateContext) {
        val mentionedUser =
            formatUsername(winner.telegramUsername ?: "Unknown", winner.telegramUserId)
        sendMessage { dailyMessageTemplate.winnerExists.format(dailyMessageTemplate.alias, mentionedUser) }
            .options { parseMode = ParseMode.Markdown }
            .send(update.telegramChatId, update.bot)
        return
    }

    private suspend fun sendWinnerMessages(sentences: List<Sentence>, winner: User, update: UpdateContext) {
        if (sentences.isEmpty()) {
            chosenWinnerMessage(winner, update)
        }
        sentences.sortedBy { it.orderNumber }.forEach { sentence ->
            delay(RANDOM_DELAY_RANGE.random())
            val mentionedUser = formatUsername(winner.telegramUsername ?: "Unknown", winner.telegramUserId)
            sentence.text?.format(dailyMessageTemplate.alias, mentionedUser)?.let {
                sendMessage { it }
                    .options { parseMode = ParseMode.Markdown }
                    .send(update.telegramChatId, update.bot)
            }
        }
    }

    private fun updateWinner(user: User) {
        user.stats.find { it.year == CURRENT_YEAR }?.let {
            it.score = (it.score ?: 0) + 1
            it.isWinner = true
        } ?: user.stats.add(
            Stat(
                chat = user.chat,
                user = user,
                score = 1,
                year = CURRENT_YEAR,
                isWinner = true
            )
        )
        userService.saveUser(user)
    }

    private fun aggregateStats(stats: Collection<Stat>): Map<Long, Stat> {
        return stats.groupBy { it.user.telegramUserId }
            .map { (userId, userStats) ->
                val firstStat = userStats.first()
                userId to Stat(
                    chat = firstStat.chat,
                    user = firstStat.user,
                    score = userStats.sumOf { it.score ?: 0L },
                    year = firstStat.year,
                    isWinner = userStats.any { it.isWinner == true && it.year == CURRENT_YEAR }
                )
            }
            .toMap()
    }

    private suspend fun showAllStats(year: Int, update: UpdateContext) {
        val stats = aggregateStats(update.chat.stats)
        sendStatsMessage(stats, year, dailyMessageTemplate.statsHeaderAll, update)
    }

    private suspend fun showStatsForYear(year: Int, update: UpdateContext) {
        val stats = aggregateStats(update.chat.stats.filter { year == it.year })
        sendStatsMessage(stats, year, dailyMessageTemplate.statsHeader, update)
    }

    private suspend fun showCurrentStats(update: UpdateContext) {
        val stats = aggregateStats(update.chat.stats.filter { CURRENT_YEAR == it.year })
        sendStatsMessage(stats, CURRENT_YEAR, dailyMessageTemplate.statsHeader, update)
    }

    private suspend fun sendStatsMessage(
        stats: Map<Long, Stat>,
        year: Int,
        headerTemplate: String,
        update: UpdateContext
    ) {
        if (stats.isEmpty()) {
            sendMessage { dailyMessageTemplate.noStats }
                .send(update.telegramChatId, update.bot)
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
            .send(update.telegramChatId, update.bot)
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

    fun formatUsername(username: String, userId: Long): String {
        return "[${username}](tg://user?id=${userId})"
    }

    suspend fun sendScheduledDailyMessage(userContext: UpdateContext) {
        chooseRandomWinner(userContext)
    }

    suspend fun sendYearEndMessage(update: UpdateContext) {
        val stats = aggregateStats(update.chat.stats)
        val winnerOfTheYear = stats.maxByOrNull { it.value.score!! }?.value
        if (winnerOfTheYear != null) {
            sendMessage {
                dailyMessageTemplate.yearEndMessage.format(
                    dailyMessageTemplate.alias,
                    CURRENT_YEAR,
                    formatUsername(winnerOfTheYear.user.telegramUsername ?: "Unknown", winnerOfTheYear.user.telegramUserId),
                    winnerOfTheYear.score
                )
            }
                .options { parseMode = ParseMode.Markdown }
                .send(update.telegramChatId, update.bot)
        }
    }
}
