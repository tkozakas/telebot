package com.telebot.repository

import com.telebot.model.Sentence
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SentenceRepository : JpaRepository<Sentence, Long> {
    @Query("""
        SELECT * FROM sentences 
        WHERE group_id = (SELECT group_id FROM sentences ORDER BY RANDOM() LIMIT 1)
        ORDER BY order_number
    """,
        nativeQuery = true
    )
    fun findRandomGroup() : List<Sentence>
}
