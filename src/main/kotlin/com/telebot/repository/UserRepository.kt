package com.telebot.repository

import com.telebot.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, Long> {
    fun findByUserId(userId: Long, chatId: Long): Optional<User>
}