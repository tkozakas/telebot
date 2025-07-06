package com.telebot.repository

import com.telebot.model.Fact
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FactRepository : JpaRepository<Fact, Long> {

    @Query(
        value = """
        SELECT *
          FROM facts f
         WHERE f.chat_id = :chatId
            OR f.chat_id IS NULL
         ORDER BY random()
         LIMIT 1
      """,
        nativeQuery = true
    )
    fun findRandomFactByChatId(chatId: Long?): Fact?
}