package com.telebot.repository

import com.telebot.model.Sentence
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SentenceRepository : JpaRepository<Sentence, Long> {
    @Query("SELECT s.groupId FROM Sentence s GROUP BY s.groupId, s.orderNumber")
    fun findGroupIdsByDailyMessageId(): List<Long>

    @Query(
        value = """
            SELECT * FROM sentence
            WHERE group_id IN :groupIds
            ORDER BY RANDOM()
            LIMIT 1
        """,
        nativeQuery = true
    )
    fun findRandomSentenceByGroupIds(groupIds: List<Long>): List<Sentence>
}
