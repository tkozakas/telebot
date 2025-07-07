package com.telebot.repository

import com.telebot.model.Chat
import com.telebot.model.Stat
import com.telebot.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface StatRepository : JpaRepository<Stat, Long> {
    fun findByUserAndChatAndYear(user: User, chat: Chat, year: Int): Optional<Stat>
    fun findByChatAndYearAndIsWinnerTrue(chat: Chat, year: Int): Optional<Stat>
    fun findByChat(chat: Chat) : List<Stat>
    fun findByChatAndYear(chat: Chat, year: Int) : List<Stat>
}