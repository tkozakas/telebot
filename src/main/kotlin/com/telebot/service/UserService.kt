package com.telebot.service

import com.telebot.model.User
import com.telebot.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun findOrCreate(userId: Long, username: String?): User {
        val user = User(
            userId = userId,
            username = username?.takeIf(String::isNotBlank) ?: "Unknown",
            isWinner = false
        )
        return userRepository.findById(userId).orElseGet { userRepository.save(user) }
    }

    fun saveUser(user: User) = userRepository.save(user)
}