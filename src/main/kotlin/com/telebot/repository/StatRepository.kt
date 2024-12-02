package com.telebot.repository

import com.telebot.model.Stat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface StatRepository : JpaRepository<Stat, Long> {
    fun findStatByChatIdAndYear(chatId: Long, year: Int): List<Stat>

    @Modifying
    @Transactional
    @Query(
        "UPDATE Stat s SET s.isWinner = true, s.score = s.score + 1 " +
                "WHERE s.chatId = ?1 AND s.userId = ?2 AND s.year = ?3"
    )
    fun setWinnerByChatIdAndUserIdAndYear(chatId: Long, userId: Long, year: Int)
    fun findByChatId(chatId: Long): List<Stat>
}
