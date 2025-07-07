package com.telebot.service

import com.telebot.enums.SubCommand
import com.telebot.model.Chat
import com.telebot.model.Stat
import com.telebot.model.UpdateContext
import com.telebot.model.User
import com.telebot.properties.DailyMessageTemplate
import com.telebot.repository.SentenceRepository
import com.telebot.repository.StatRepository
import com.telebot.util.PrinterUtil
import com.telebot.util.TelegramMessageSender
import kotlinx.coroutines.delay
import org.springframework.stereotype.Service
import java.time.Year

@Service
class DailyMessageService(
    private val statRepository: StatRepository,
    private val sentenceRepository: SentenceRepository,
    private val dailyMessageTemplate: DailyMessageTemplate,
    private val printerUtil: PrinterUtil,
    private val userService: UserService,
    private val telegramMessageSender: TelegramMessageSender
) : CommandService {

    private companion object {
        val CURRENT_YEAR: Int = Year.now().value
        val RANDOM_DELAY_RANGE: LongRange = 700L..1200L
    }

    data class WinnerResult(val user: User, val score: Long, val isNew: Boolean)

    override suspend fun handle(update: UpdateContext) {
        val year = update.args.getOrNull(2)?.toIntOrNull() ?: CURRENT_YEAR

        when (update.subCommand?.lowercase()) {
            null -> handleRandomWinnerSelection(update)
            SubCommand.ALL.name.lowercase() -> handleAllStatsCommand(update)
            SubCommand.STATS.name.lowercase() -> handleYearStatsCommand(update, year)
            else -> handleYearStatsCommand(update, year)
        }
    }

    private suspend fun handleAllStatsCommand(update: UpdateContext) {
        val stats = statRepository.findByChat(update.chat)
        val aggregatedStats = aggregateStats(stats, update.chat)
        displayStats(update, aggregatedStats, dailyMessageTemplate.statsHeaderAll)
    }

    private suspend fun handleYearStatsCommand(update: UpdateContext, year: Int) {
        val stats = statRepository.findByChatAndYear(update.chat, year)
        val aggregatedStats = aggregateStats(stats, update.chat)
        displayStats(update, aggregatedStats, dailyMessageTemplate.statsHeader.format(year))
    }

    private fun aggregateStats(statList: List<Stat>, chat: Chat): List<Stat> {
        return statList
            .groupBy { it.user }
            .map { (user, userStats) ->
                Stat(
                    user = user,
                    chat = chat,
                    score = userStats.sumOf { it.score },
                    year = 0,
                    isWinner = userStats.any { it.isWinner == true }
                )
            }
            .sortedByDescending { it.score }
    }

    private suspend fun handleRandomWinnerSelection(update: UpdateContext) {
        val winnerResult = determineWinnerForToday(update.chat)

        if (winnerResult.isNew) {
            displayNewWinnerSequence(update, winnerResult)
        } else {
            displayExistingWinner(update, winnerResult)
        }
    }

    private fun determineWinnerForToday(chat: Chat): WinnerResult {
        val existingWinner = statRepository.findByChatAndYearAndIsWinnerTrue(chat, CURRENT_YEAR)

        if (existingWinner.isPresent) {
            val stat = existingWinner.get()
            return WinnerResult(user = stat.user, score = stat.score, isNew = false)
        }

        val newUser = userService.findRandomUserByChat(chat)
        val newStat = statRepository.findByUserAndChatAndYear(newUser, chat, CURRENT_YEAR)
            .orElseGet { Stat(user = newUser, chat = chat, year = CURRENT_YEAR, score = 0L) }

        newStat.score++
        newStat.isWinner = true
        statRepository.save(newStat)

        return WinnerResult(user = newUser, score = newStat.score, isNew = true)
    }

    private suspend fun displayStats(update: UpdateContext, stats: List<Stat>, header: String) {
        if (stats.isEmpty()) {
            sendMarkdownMessage(update, dailyMessageTemplate.noStats)
            return
        }

        val formattedStats = printerUtil.printStats(
            header = header,
            stats = stats,
            footer = dailyMessageTemplate.statsFooter.format(stats.size),
            bodyTemplate = dailyMessageTemplate.userStats
        )

        sendMarkdownMessage(update, formattedStats)
    }

    private suspend fun displayExistingWinner(update: UpdateContext, winner: WinnerResult) {
        val message = dailyMessageTemplate.winnerExists.format(displayUser(winner.user), winner.score)
        sendMarkdownMessage(update, message)
    }

    private suspend fun displayNewWinnerSequence(update: UpdateContext, winner: WinnerResult) {
        val sentences = sentenceRepository.findRandomGroup()
        if (sentences.isEmpty()) {
            displayExistingWinner(update, winner)
            return
        }

        sentences.forEach { sentence ->
            delay(RANDOM_DELAY_RANGE.random())
            val message = sentence.text?.format(dailyMessageTemplate.alias, displayUser(winner.user)).orEmpty()
            sendMarkdownMessage(update, message)
        }
    }

    private suspend fun sendMarkdownMessage(update: UpdateContext, text: String) {
        telegramMessageSender.send(update.bot, update.chat.chatId, text)
    }

    private fun displayUser(user: User): String = "[${user.username}](tg://user?id=${user.userId})"
}