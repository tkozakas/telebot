package com.telebot.repository

import com.telebot.model.Chat
import com.telebot.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findAllByChat(chat: Chat): MutableList<User>
}