package com.telebot.repository

import com.telebot.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface UserRepository : JpaRepository<User, Long> {

    @Query("""
        SELECT u.* FROM users u
        INNER JOIN chat_users cu ON u.user_id = cu.user_id
        WHERE cu.chat_id = :chatId
        ORDER BY random()
        LIMIT 1
        """,
        nativeQuery = true
    )
    fun findRandomUserByChatId(chatId: Long?): Optional<User>
}