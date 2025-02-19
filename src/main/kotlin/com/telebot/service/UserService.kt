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
            id = userId,
            username = username?.takeIf(String::isNotBlank) ?: "Unknown",
            isWinner = false
        )
        return userRepository.findById(userId).orElseGet { userRepository.save(user) }
    }

    fun chooseRandomWinner(): User? {
        return userRepository.findRandomUser()
    }

    fun update(user: User) {
        userRepository.save(user)
    }
}