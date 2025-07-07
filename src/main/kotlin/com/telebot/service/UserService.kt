package com.telebot.service

import com.telebot.model.Chat
import com.telebot.model.User
import com.telebot.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional
    fun findOrCreate(userId: Long, username: String): User {
        return userRepository.findById(userId).orElseGet {
            val newUser = User(userId, username)
            userRepository.save(newUser)
        }
    }

    @Transactional(readOnly = true)
    fun findRandomUserByChat(chat: Chat): User {
        return userRepository.findRandomUserByChatId(chat.chatId).orElseGet {
            throw IllegalArgumentException("No users found in chat with id ${chat.chatId}")
        }
    }
}