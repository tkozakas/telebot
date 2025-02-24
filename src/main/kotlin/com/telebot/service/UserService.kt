package com.telebot.service

import com.telebot.model.User
import com.telebot.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun saveUser(user: User): User {
        return userRepository.save(user)
    }
}