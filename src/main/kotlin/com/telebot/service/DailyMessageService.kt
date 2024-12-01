package com.telebot.service

import com.telebot.model.Sentence
import com.telebot.model.Stat
import com.telebot.repository.SentenceRepository
import com.telebot.repository.StatRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class DailyMessageService(
    private val statRepository: StatRepository,
    private val sentenceRepository: SentenceRepository
) {
    @Transactional
    fun getRandomGroupSentences(): List<Sentence> {
        val groupIds = sentenceRepository.findGroupIdsByDailyMessageId()
        return sentenceRepository.findRandomSentenceByGroupIds(groupIds)
    }

    @Transactional
    fun getStatByChatIdAndYear(chatId: Long, year: Int): List<Stat> {
        return statRepository.findStatByChatIdAndYear(chatId, year)
    }

    @Transactional
    fun setWinnerByChatIdAndUserIdAndYear(chatId: Long, userId: Long, year: Int) {
        statRepository.setWinnerByChatIdAndUserIdAndYear(chatId, userId, year)
    }
}
