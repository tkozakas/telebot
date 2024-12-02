package com.telebot.repository

import com.telebot.model.Sentence
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SentenceRepository : JpaRepository<Sentence, Long> {
    @Query("SELECT s.groupId FROM Sentence s ORDER BY RANDOM() LIMIT 1")
    fun findRandomGroupId(): Long?

    fun findSentencesByGroupId(randomGroupId: Long): List<Sentence>
}
